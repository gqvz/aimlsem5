"""Characterisation tests: pin the HTTP contract the Android and web clients depend on.

These run against the real handler. Phase 3 of REWRITE_PLAN.md (FastAPI) must keep every
test here passing unchanged.
"""
from sahay import db as sahay_db
from sahay.services import accounts


def register(api, contact="asha@example.test", name="Asha Demo", language="en"):
    status, body = api.call("POST", "/api/auth/register",
                            {"name": name, "contact": contact, "password": "PatientPass123", "language": language})
    assert status == 201, body
    return body["token"], body["user"]


def make_clinician(api, contact="meera@example.test"):
    with sahay_db.session_scope() as db:
        user = accounts.create_user(db, "Dr. Meera Rao", contact, "ClinicianPass123", "clinician")
        user_id = user.id
    status, body = api.call("POST", "/api/auth/login", {"contact": contact, "password": "ClinicianPass123"})
    assert status == 200
    return body["token"], user_id


def test_health_shape(api):
    status, body = api.call("GET", "/api/health")
    assert status == 200
    assert {"ok", "mode", "model_config", "languages", "agents"} <= body.keys()
    assert body["mode"] == "demo" and set(body["languages"]) == {"en", "hi", "bn", "ta"}


def test_cards_shape(api):
    status, body = api.call("GET", "/api/cards")
    assert status == 200 and body["cards"]
    assert {"id", "title", "url", "summary"} <= body["cards"][0].keys()


def test_unknown_api_path_is_404_json(api):
    assert api.call("GET", "/api/nope")[0] == 404
    assert api.call("POST", "/api/nope", {})[0] == 404


def test_message_ordinary_demo_shape(api):
    status, body = api.call("POST", "/api/message", {"text": "I felt lonely today", "language": "en"})
    assert status == 200
    assert {"route", "action", "text", "card", "mode", "translation", "agent_trace", "orchestration"} <= body.keys()
    assert body["route"] == "ordinary" and body["mode"] in ("demo", "fixed", "fallback")


def test_message_crisis_routes_to_help_without_a_model(api):
    status, body = api.call("POST", "/api/message", {"text": "I want to kill myself", "language": "en"})
    assert status == 200 and body["route"] == "crisis" and body["action"] == "help"
    assert "14416" in body["text"] and "112" in body["text"]


def test_message_validation(api):
    assert api.call("POST", "/api/message", {"text": "   "})[0] == 400
    assert api.call("POST", "/api/message", ["not", "an", "object"])[0] == 400


def test_activity_plan_shape(api):
    status, body = api.call("POST", "/api/message", {"text": "plan", "action": "activities", "energy": "low", "language": "en"})
    assert status == 200 and body["action"] == "activities"
    assert body["activity_plan"] and {"id", "title", "minutes", "energy"} <= body["activity_plan"][0].keys()


def test_register_login_me_logout(api):
    token, user = register(api)
    assert {"id", "name", "contact", "role", "language"} <= user.keys() and user["role"] == "patient"
    assert api.call("GET", "/api/me", token=token)[1]["user"]["contact"] == "asha@example.test"
    status, login = api.call("POST", "/api/auth/login", {"contact": "ASHA@example.test", "password": "PatientPass123"})
    assert status == 200 and login["token"]
    assert api.call("POST", "/api/auth/logout", {}, token=login["token"])[0] == 200
    assert api.call("GET", "/api/me", token=login["token"])[0] == 401


def test_auth_failures(api):
    assert api.call("GET", "/api/me")[0] == 401
    assert api.call("POST", "/api/auth/login", {"contact": "x@y.test", "password": "wrongpassword1"})[0] == 401
    assert api.call("POST", "/api/auth/register", {"name": "A", "contact": "a@b.test", "password": "short"})[0] == 400


def test_role_matrix(api):
    patient, _ = register(api)
    clinician, _ = make_clinician(api)
    checks = [
        ("GET", "/api/clinician/queue", {"patient": 403, "clinician": 200, "anon": 401}),
        ("GET", "/api/admin/users", {"patient": 403, "clinician": 403, "anon": 401}),
        ("GET", "/api/admin/audit", {"patient": 403, "clinician": 403, "anon": 401}),
        ("GET", "/api/clinicians", {"patient": 200, "clinician": 403, "anon": 401}),
        ("GET", "/api/shares/mine", {"patient": 200, "clinician": 403, "anon": 401}),
        ("GET", "/api/care/threads", {"patient": 200, "clinician": 200, "anon": 401}),
    ]
    tokens = {"patient": patient, "clinician": clinician, "anon": ""}
    for method, path, expected in checks:
        for who, status in expected.items():
            assert api.call(method, path, token=tokens[who])[0] == status, (method, path, who)


def test_share_requires_consent_and_clinician_loses_access_on_revoke(api):
    patient, _ = register(api)
    clinician, clinician_id = make_clinician(api)
    payload = {"clinician_id": clinician_id, "summary": "Travel concerns.", "questions": ["When is the next visit?"]}
    assert api.call("POST", "/api/shares", payload, token=patient)[0] == 400
    status, created = api.call("POST", "/api/shares", {**payload, "consent": True}, token=patient)
    assert status == 201
    share_id = created["share"]["id"]
    assert api.call("GET", f"/api/clinician/shares/{share_id}", token=clinician)[0] == 200
    assert api.call("DELETE", f"/api/shares/{share_id}", token=patient)[0] == 200
    assert api.call("GET", f"/api/clinician/shares/{share_id}", token=clinician)[0] == 404


def test_care_thread_isolated_between_users(api):
    patient, _ = register(api)
    outsider, _ = register(api, contact="other@example.test", name="Other")
    clinician, clinician_id = make_clinician(api)
    status, created = api.call("POST", "/api/care/threads", {"partner_id": clinician_id}, token=patient)
    assert status == 201
    thread = created["thread"]["id"]
    assert api.call("POST", f"/api/care/threads/{thread}/messages", {"text": "Hello"}, token=patient)[0] == 201
    messages = api.call("GET", f"/api/care/threads/{thread}/messages", token=clinician)[1]["messages"]
    assert [m["text"] for m in messages] == ["Hello"]
    assert api.call("GET", f"/api/care/threads/{thread}/messages", token=outsider)[0] == 404
    assert api.call("POST", f"/api/care/threads/{thread}/messages", {"text": "x", "kind": "file_request"}, token=patient)[0] == 403


def test_body_size_limit(api):
    assert api.call("POST", "/api/message", {"text": "x" * 20000})[0] == 413


def test_model_test_rejects_unlisted_endpoint(api):
    status, body = api.call("POST", "/api/model/test", {"model_config": {"url": "http://169.254.169.254/x", "model": "m"}})
    assert status == 400 and "not allowed" in body["error"]


def test_message_rate_limit_returns_429_with_retry_after(api):
    codes = [api.call("POST", "/api/message", {"text": "hello"})[0] for _ in range(32)]
    assert codes[:30] == [200] * 30 and 429 in codes[30:]


def test_login_throttle_after_repeated_failures_and_reset_on_success(api):
    register(api)
    for _ in range(8):
        assert api.call("POST", "/api/auth/login", {"contact": "asha@example.test", "password": "badpassword11"})[0] == 401
    status, body = api.call("POST", "/api/auth/login", {"contact": "asha@example.test", "password": "PatientPass123"})
    assert status == 429 and body["retry_after"] > 0


def test_expired_sessions_are_pruned(api):
    token, user = register(api)
    from sqlalchemy import text
    with sahay_db.session_scope() as db:
        db.execute(text("UPDATE sessions SET expires_at='2000-01-01T00:00:00+00:00'"))
    api.call("POST", "/api/auth/login", {"contact": "asha@example.test", "password": "PatientPass123"})
    with sahay_db.session_scope() as db:
        assert db.execute(text("SELECT count(*) FROM sessions WHERE expires_at<'2001'")).scalar() == 0

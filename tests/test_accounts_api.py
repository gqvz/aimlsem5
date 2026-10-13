"""Account, sharing and admin behaviour through the HTTP API (ported from the module tests)."""
from sqlalchemy import text
from test_api_contract import make_clinician, register

from sahay import db as sahay_db


def test_queue_hides_content_and_other_clinicians_cannot_open_a_share(api):
    patient, _ = register(api)
    doctor, doctor_id = make_clinician(api)
    other, _ = make_clinician(api, "anita@example.test")
    payload = {"clinician_id": doctor_id, "summary": "Travel after my visit.", "questions": ["When should I plan?"], "consent": True}
    share_id = api.call("POST", "/api/shares", payload, token=patient)[1]["share"]["id"]
    queue = api.call("GET", "/api/clinician/queue", token=doctor)[1]["shares"]
    assert [row["id"] for row in queue] == [share_id]
    assert "summary" not in queue[0] and queue[0]["brief_preview"].startswith("Travel")
    assert api.call("GET", f"/api/clinician/shares/{share_id}", token=other)[0] == 404
    status, reviewed = api.call("POST", f"/api/clinician/shares/{share_id}/review",
                                {"status": "reviewed", "note": "Discuss at the next visit."}, token=doctor)
    assert status == 200 and reviewed["share"]["status"] == "reviewed" and reviewed["share"]["reviewed_at"]
    assert api.call("POST", f"/api/clinician/shares/{share_id}/review", {"status": "bogus"}, token=doctor)[0] == 400
    api.call("DELETE", f"/api/shares/{share_id}", token=patient)
    assert api.call("GET", "/api/clinician/queue", token=doctor)[1]["shares"] == []


def test_share_validation(api):
    patient, _ = register(api)
    _, doctor_id = make_clinician(api)
    base = {"clinician_id": doctor_id, "consent": True}
    assert api.call("POST", "/api/shares", {**base, "questions": []}, token=patient)[0] == 400
    assert api.call("POST", "/api/shares", {**base, "questions": ["x"] * 11}, token=patient)[0] == 400
    assert api.call("POST", "/api/shares", {**base, "questions": ["ok"], "language": "xx"}, token=patient)[0] == 400
    assert api.call("POST", "/api/shares", {"clinician_id": "nobody", "questions": ["ok"], "consent": True}, token=patient)[0] == 404


def test_profile_and_password_flow(api):
    token, _ = register(api)
    assert api.call("POST", "/api/me/profile", {"name": "Asha Sharma"}, token=token)[1]["user"]["name"] == "Asha Sharma"
    assert api.call("GET", "/api/me", token=token)[1]["user"]["name"] == "Asha Sharma"
    wrong = api.call("POST", "/api/me/password", {"current_password": "wrong", "new_password": "NewStrongPass123"}, token=token)
    assert wrong[0] == 403
    weak = api.call("POST", "/api/me/password", {"current_password": "PatientPass123", "new_password": "short"}, token=token)
    assert weak[0] == 400
    assert api.call("POST", "/api/me/password", {"current_password": "PatientPass123", "new_password": "NewStrongPass123"}, token=token)[0] == 200
    assert api.call("GET", "/api/me", token=token)[0] == 401  # all sessions end on password change
    assert api.call("POST", "/api/auth/login", {"contact": "asha@example.test", "password": "NewStrongPass123"})[0] == 200


def test_duplicate_contact_is_conflict(api):
    register(api)
    status, body = api.call("POST", "/api/auth/register", {"name": "Again", "contact": "ASHA@example.test", "password": "PatientPass123"})
    assert status == 409 and "already registered" in body["error"]


def test_admin_invite_accept_and_deactivate(api):
    from sahay.services import accounts
    with sahay_db.session_scope() as db:
        accounts.create_user(db, "Admin", "admin@example.test", "AdminPass12345", "admin")
    admin = api.call("POST", "/api/auth/login", {"contact": "admin@example.test", "password": "AdminPass12345"})[1]["token"]
    invite = api.call("POST", "/api/admin/invitations", {"name": "Dr. Kumar", "contact": "kumar@example.test"}, token=admin)
    assert invite[0] == 201
    accepted = api.call("POST", "/api/auth/accept-invite", {"code": invite[1]["invite_code"], "password": "ClinicianPass123"})
    assert accepted[0] == 201 and accepted[1]["user"]["role"] == "clinician"
    assert api.call("POST", "/api/auth/accept-invite", {"code": invite[1]["invite_code"], "password": "ClinicianPass123"})[0] == 400
    clinician_id = accepted[1]["user"]["id"]
    assert api.call("POST", f"/api/admin/users/{clinician_id}/deactivate", {}, token=admin)[0] == 200
    assert api.call("GET", "/api/me", token=accepted[1]["token"])[0] == 401
    admin_id = api.call("GET", "/api/me", token=admin)[1]["user"]["id"]
    assert api.call("POST", f"/api/admin/users/{admin_id}/deactivate", {}, token=admin)[0] == 400
    events = {e["event"] for e in api.call("GET", "/api/admin/audit", token=admin)[1]["events"]}
    assert {"invite_clinician", "accept_invite", "deactivate"} <= events


def test_audit_never_stores_message_text(api):
    token, _ = register(api)
    api.call("POST", "/api/message", {"text": "SECRET-PHRASE-123 I felt low"})
    with sahay_db.session_scope() as db:
        dump = " ".join(str(r) for r in db.execute(text("SELECT * FROM audit")).all())
        assert "SECRET-PHRASE-123" not in dump

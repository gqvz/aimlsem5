"""Write real backend responses to android/app/src/test/resources/contract/.

The Android parsers are tested against these files, so a backend change that breaks a
client shows up in `./gradlew test`. Re-run after any API change:
    python tools/export_contract_fixtures.py
"""
import base64
import json
import os
import sys
import tempfile
import threading
import time
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))
OUT = ROOT / "android" / "app" / "src" / "test" / "resources" / "contract"

os.environ["SAHAY_DB_PATH"] = str(Path(tempfile.mkdtemp()) / "fixtures.sqlite3")
for name in ("LLM_API_URL", "LLM_MODEL", "LLM_API_KEY", "STT_API_URL", "TTS_API_URL"):
    os.environ.pop(name, None)

import uvicorn

from sahay import db
from sahay.api.app import create_app
from sahay.services import accounts

server = uvicorn.Server(uvicorn.Config(create_app(), host="127.0.0.1", port=0, log_level="error"))
threading.Thread(target=server.run, daemon=True).start()
while not server.started:
    time.sleep(0.05)
BASE = f"http://127.0.0.1:{server.servers[0].sockets[0].getsockname()[1]}"


def call(method, path, body=None, token=None, raw=False):
    request = urllib.request.Request(BASE + path, None if body is None else json.dumps(body).encode(), method=method)
    if body is not None:
        request.add_header("Content-Type", "application/json")
    if token:
        request.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(request, timeout=15) as response:
            return response.status, response.read()
    except urllib.error.HTTPError as error:
        return error.code, error.read()


def save(name, payload):
    (OUT / f"{name}.json").write_text(json.dumps(json.loads(payload), indent=2, ensure_ascii=False, sort_keys=True) + "\n", encoding="utf-8")


def must(result, expected=200):
    status, payload = result
    assert status == expected, (status, payload)
    return payload


OUT.mkdir(parents=True, exist_ok=True)
save("health", must(call("GET", "/api/health")))
save("cards", must(call("GET", "/api/cards")))
save("message_ordinary", must(call("POST", "/api/message", {"text": "I felt lonely today", "language": "en"})))
save("message_crisis", must(call("POST", "/api/message", {"text": "I want to kill myself", "language": "en"})))
save("message_medical_boundary", must(call("POST", "/api/message", {"text": "Should I double my medicine?", "language": "en"})))
save("message_activity_plan", must(call("POST", "/api/message", {"text": "plan", "action": "activities", "energy": "low", "language": "en"})))
save("error_validation", call("POST", "/api/message", {"text": "  "})[1])

registered = json.loads(must(call("POST", "/api/auth/register", {"name": "Asha Demo", "contact": "asha@example.test", "password": "PatientPass123", "language": "en"}, None), 201))
save("session", json.dumps(registered))
token = registered["token"]
save("me", must(call("GET", "/api/me", token=token)))
save("error_unauthorised", call("GET", "/api/me")[1])

with db.session_scope() as session:
    doctor = accounts.create_user(session, "Dr. Meera Rao", "meera@example.test", "ClinicianPass123", "clinician")
    doctor_id = doctor.id
doctor_token = json.loads(must(call("POST", "/api/auth/login", {"contact": "meera@example.test", "password": "ClinicianPass123"})))["token"]
save("clinicians", must(call("GET", "/api/clinicians", token=token)))
share = json.loads(must(call("POST", "/api/shares", {"clinician_id": doctor_id, "summary": "Travel concerns.", "questions": ["When is my next visit?"], "consent": True}, token), 201))
save("share", json.dumps(share))
save("shares_mine", must(call("GET", "/api/shares/mine", token=token)))
save("clinician_queue", must(call("GET", "/api/clinician/queue", token=doctor_token)))
save("clinician_share", must(call("GET", "/api/clinician/shares/" + share["share"]["id"], token=doctor_token)))

thread = json.loads(must(call("POST", "/api/care/threads", {"partner_id": doctor_id}, token), 201))["thread"]
request_id = json.loads(must(call("POST", f"/api/care/threads/{thread['id']}/messages", {"kind": "file_request", "text": "Please send the report"}, doctor_token), 201))["message_id"]
must(call("POST", f"/api/care/threads/{thread['id']}/messages",
          {"text": "Here it is", "request_id": request_id, "files": [{"name": "report.txt", "mime": "text/plain", "base64": base64.b64encode(b"hello").decode()}]}, token), 201)
save("care_threads", must(call("GET", "/api/care/threads", token=token)))
save("care_messages", must(call("GET", f"/api/care/threads/{thread['id']}/messages", token=token)))
save("care_contacts", must(call("GET", "/api/care/contacts", token=token)))
print("wrote", len(list(OUT.glob("*.json"))), "fixtures to", OUT)
os._exit(0)

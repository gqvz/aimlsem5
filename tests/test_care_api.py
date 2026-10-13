"""Participant isolation, uploads and requested-file fulfilment through the HTTP API."""
import base64

import pytest
from sqlalchemy import text
from test_api_contract import make_clinician, register

from sahay import db as sahay_db
from sahay.services.care import MAX_FILE


def file(content=b"Fictional course document", mime="text/plain", name="demo.txt"):
    return {"name": name, "mime": mime, "base64": base64.b64encode(content).decode()}


@pytest.fixture()
def world(api):
    patient, patient_user = register(api)
    outsider, _ = register(api, "other@example.test", "Other")
    doctor, doctor_id = make_clinician(api)
    status, created = api.call("POST", "/api/care/threads", {"partner_id": doctor_id}, token=patient)
    assert status == 201
    thread = created["thread"]["id"]
    return {"api": api, "patient": patient, "patient_id": patient_user["id"], "outsider": outsider,
            "doctor": doctor, "doctor_id": doctor_id, "thread": thread, "path": f"/api/care/threads/{thread}/messages"}


def test_either_side_can_initiate_and_upload_without_request(world):
    api = world["api"]
    status, same = api.call("POST", "/api/care/threads", {"partner_id": world["patient_id"]}, token=world["doctor"])
    assert same["thread"]["id"] == world["thread"]
    for who in ("patient", "doctor"):
        assert api.call("POST", world["path"], {"files": [file(who.encode())]}, token=world[who])[0] == 201
    rows = api.call("GET", world["path"], token=world["doctor"])[1]["messages"]
    assert len(rows) == 2
    for row, who in zip(rows, ("patient", "doctor")):
        assert "base64" not in row["files"][0]
        status, content = api.call("GET", f"/api/care/files/{row['files'][0]['id']}", token=world["patient"], raw=True)
        assert status == 200 and content == who.encode()


def test_request_requires_file_and_fulfils_only_once(world):
    api = world["api"]
    request = api.call("POST", world["path"], {"kind": "file_request", "text": "Please attach the report"}, token=world["doctor"])[1]["message_id"]
    assert api.call("POST", world["path"], {"text": "here", "request_id": request}, token=world["patient"])[0] == 400
    assert api.call("POST", world["path"], {"request_id": request, "files": [file()]}, token=world["patient"])[0] == 201
    rows = api.call("GET", world["path"], token=world["patient"])[1]["messages"]
    assert rows[0]["fulfilled_at"] is not None
    assert api.call("POST", world["path"], {"request_id": request, "files": [file()]}, token=world["patient"])[0] == 400


def test_outsider_cannot_read_write_or_download(world):
    api = world["api"]
    api.call("POST", world["path"], {"files": [file()]}, token=world["patient"])
    file_id = api.call("GET", world["path"], token=world["patient"])[1]["messages"][0]["files"][0]["id"]
    assert api.call("GET", world["path"], token=world["outsider"])[0] == 404
    assert api.call("POST", world["path"], {"text": "intrusion"}, token=world["outsider"])[0] == 404
    assert api.call("GET", f"/api/care/files/{file_id}", token=world["outsider"])[0] == 404
    assert api.call("GET", f"/api/care/files/{file_id}")[0] == 401


def test_invalid_files_do_not_create_message_or_fulfil_request(world):
    api = world["api"]
    request = api.call("POST", world["path"], {"kind": "file_request", "text": "Report please"}, token=world["doctor"])[1]["message_id"]
    bad = [file(b"not PDF", "application/pdf"), file(b"x" * (MAX_FILE + 1)), file(name=".."), file(mime="application/x-executable")]
    for item in bad:
        status, _ = api.call("POST", world["path"], {"files": [item], "request_id": request}, token=world["patient"])
        assert status in (400, 413)
    rows = api.call("GET", world["path"], token=world["patient"])[1]["messages"]
    assert len(rows) == 1 and rows[0]["fulfilled_at"] is None


def test_patient_cannot_request_and_inactive_partner_is_rejected(world):
    api = world["api"]
    assert api.call("POST", world["path"], {"kind": "file_request", "text": "Report"}, token=world["patient"])[0] == 403
    with sahay_db.session_scope() as db:
        db.execute(text("UPDATE users SET active=0 WHERE id=:i"), {"i": world["doctor_id"]})
    assert api.call("GET", world["path"], token=world["patient"])[0] in (401, 403)


def test_threads_list_has_preview_and_open_request_count(world):
    api = world["api"]
    api.call("POST", world["path"], {"kind": "file_request", "text": "Please send the scan"}, token=world["doctor"])
    threads = api.call("GET", "/api/care/threads", token=world["patient"])[1]["threads"]
    assert threads[0]["preview"] == "Please send the scan" and threads[0]["open_requests"] == 1

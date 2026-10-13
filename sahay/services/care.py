"""Two-way patient/clinician conversations and small attachments."""
from __future__ import annotations

import base64
from pathlib import Path
from typing import Any

from sqlalchemy import select, text
from sqlalchemy.orm import Session

from ..errors import ApiError
from ..models import CareFile, CareMessage, CareThread, User
from ..security import new_id
from .accounts import clean, record, utc_now

MAX_FILE = 5 * 1024 * 1024
ALLOWED = {"application/pdf", "image/png", "image/jpeg", "text/plain"}
_MAGIC = {"application/pdf": (b"%PDF-", "This is not a PDF file"),
          "image/png": (b"\x89PNG\r\n\x1a\n", "This is not a PNG file"),
          "image/jpeg": (b"\xff\xd8\xff", "This is not a JPEG file")}


def _thread(db: Session, thread_id: str, user: User) -> CareThread:
    thread = db.execute(select(CareThread).where(
        CareThread.id == thread_id, (CareThread.patient_id == user.id) | (CareThread.clinician_id == user.id)
    )).scalar_one_or_none()
    if thread is None:
        raise ApiError(404, "Conversation unavailable")
    partner = thread.clinician_id if user.id == thread.patient_id else thread.patient_id
    if db.execute(select(User.id).where(User.id == partner, User.active == 1)).first() is None:
        raise ApiError(403, "This care-team account is inactive")
    return thread


def _file_data(item: Any) -> tuple[str, str, bytes]:
    if not isinstance(item, dict):
        raise ApiError(400, "Invalid attachment")
    name = Path(clean(item.get("name"), "file name", 120)).name
    if not name or name in (".", ".."):
        raise ApiError(400, "Invalid file name")
    mime = str(item.get("mime", ""))
    if mime not in ALLOWED:
        raise ApiError(400, "Use PDF, PNG, JPEG or plain text files")
    try:
        payload = base64.b64decode(item.get("base64", ""), validate=True)
    except (ValueError, TypeError):
        raise ApiError(400, "Invalid file encoding") from None
    if not payload or len(payload) > MAX_FILE:
        raise ApiError(413, "Each file must be between 1 byte and 5 MB")
    if mime in _MAGIC and not payload.startswith(_MAGIC[mime][0]):
        raise ApiError(400, _MAGIC[mime][1])
    if mime == "text/plain":
        try:
            payload.decode("utf-8")
        except UnicodeDecodeError:
            raise ApiError(400, "Use UTF-8 text") from None
    return name, mime, payload


def contacts(db: Session, user: User) -> list[dict[str, str]]:
    role = "clinician" if user.role == "patient" else "patient"
    rows = db.execute(select(User.id, User.name, User.role).where(User.role == role, User.active == 1).order_by(User.name)).all()
    return [{"id": r.id, "name": r.name, "role": r.role} for r in rows]


def threads(db: Session, user: User) -> list[dict[str, Any]]:
    rows = db.execute(text("""
        SELECT t.*, p.name patient_name, c.name clinician_name,
          (SELECT text FROM care_messages WHERE thread_id=t.id ORDER BY created_at DESC, rowid DESC LIMIT 1) preview,
          (SELECT count(*) FROM care_messages WHERE thread_id=t.id AND kind='file_request' AND fulfilled_at IS NULL) open_requests
        FROM care_threads t JOIN users p ON p.id=t.patient_id JOIN users c ON c.id=t.clinician_id
        WHERE t.patient_id=:u OR t.clinician_id=:u ORDER BY t.created_at DESC"""), {"u": user.id}).mappings().all()
    return [dict(r) for r in rows]


def open_thread(db: Session, user: User, data: dict[str, Any]) -> dict[str, Any]:
    partner = clean(data.get("partner_id"), "care contact", 40)
    opposite = "clinician" if user.role == "patient" else "patient"
    if db.execute(select(User.id).where(User.id == partner, User.role == opposite, User.active == 1)).first() is None:
        raise ApiError(404, "Care contact unavailable")
    patient, doctor = (user.id, partner) if user.role == "patient" else (partner, user.id)
    thread = db.execute(select(CareThread).where(CareThread.patient_id == patient, CareThread.clinician_id == doctor)).scalar_one_or_none()
    if thread is None:
        thread = CareThread(id=new_id(), patient_id=patient, clinician_id=doctor, created_at=utc_now())
        db.add(thread)
        db.flush()
    record(db, user.id, "open_conversation", "care_thread", thread.id)
    return {"id": thread.id, "patient_id": thread.patient_id, "clinician_id": thread.clinician_id, "created_at": thread.created_at}


def messages(db: Session, user: User, thread_id: str) -> list[dict[str, Any]]:
    _thread(db, thread_id, user)
    rows = db.execute(text("""
        SELECT m.*, u.name sender_name, u.role sender_role FROM care_messages m JOIN users u ON u.id=m.sender_id
        WHERE thread_id=:t ORDER BY m.created_at, m.rowid"""), {"t": thread_id}).mappings().all()
    out = []
    for row in rows:
        files = db.execute(select(CareFile.id, CareFile.name, CareFile.mime, CareFile.size).where(CareFile.message_id == row["id"])).all()
        out.append({**dict(row), "files": [{"id": f.id, "name": f.name, "mime": f.mime, "size": f.size} for f in files]})
    return out


def post_message(db: Session, user: User, thread_id: str, data: dict[str, Any]) -> str:
    _thread(db, thread_id, user)
    body = clean(data.get("text"), "message", 2000, required=False)
    kind = data.get("kind", "note")
    if kind not in ("note", "file_request"):
        raise ApiError(400, "Invalid message kind")
    if kind == "file_request" and user.role != "clinician":
        raise ApiError(403, "Only clinicians can request medical files")
    items = data.get("files", [])
    if not isinstance(items, list) or len(items) > 2:
        raise ApiError(400, "Attach up to two files per message")
    files = [_file_data(f) for f in items]
    if not body and not files:
        raise ApiError(400, "Add a message or file")
    if kind == "file_request" and not body:
        raise ApiError(400, "Describe the file you are requesting")
    request_id = clean(data.get("request_id"), "request", 40, required=False)
    if request_id:
        request = db.execute(select(CareMessage).where(
            CareMessage.id == request_id, CareMessage.thread_id == thread_id, CareMessage.kind == "file_request",
            CareMessage.sender_id != user.id, CareMessage.fulfilled_at.is_(None))).scalar_one_or_none()
        if request is None or not files:
            raise ApiError(400, "Choose an open request and attach a file")
        request.fulfilled_at = utc_now()
    message = CareMessage(id=new_id(), thread_id=thread_id, sender_id=user.id, text=body, kind=kind,
                          request_id=request_id or None, created_at=utc_now())
    db.add(message)
    db.flush()
    for name, mime, payload in files:
        db.add(CareFile(id=new_id(), message_id=message.id, name=name, mime=mime, bytes=payload, size=len(payload)))
    record(db, user.id, "send_" + kind, "care_thread", thread_id)
    return message.id


def attachment(db: Session, user: User, file_id: str) -> tuple[bytes, str, str]:
    row = db.execute(select(CareFile, CareMessage.thread_id).join(CareMessage, CareMessage.id == CareFile.message_id)
                     .where(CareFile.id == file_id)).first()
    if row is None:
        raise ApiError(404, "File unavailable")
    _thread(db, row[1], user)
    record(db, user.id, "download_file", "care_file", file_id)
    return bytes(row[0].bytes), row[0].mime, row[0].name

"""Synthetic-account and patient-consented clinician review storage for Sahay.

This is a local course-demo workflow. It contains no clinical integration or
automatic emergency notification. Do not enter real patient information.
"""

from __future__ import annotations

import hashlib
import hmac
import json
import os
import secrets
import sqlite3
import sys
from contextlib import contextmanager
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any, Iterator


DB_PATH = Path(os.getenv("SAHAY_DB_PATH", str(Path(__file__).with_name("sahay_demo.sqlite3"))))
SESSION_HOURS = 12


class ApiError(Exception):
    def __init__(self, status: int, message: str):
        self.status = status
        self.message = message
        super().__init__(message)


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


@contextmanager
def connection() -> Iterator[sqlite3.Connection]:
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    db = sqlite3.connect(DB_PATH, timeout=10)
    db.row_factory = sqlite3.Row
    db.execute("PRAGMA foreign_keys=ON")
    try:
        yield db
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


def init_db() -> None:
    with connection() as db:
        db.executescript("""
            CREATE TABLE IF NOT EXISTS users (
              id TEXT PRIMARY KEY, name TEXT NOT NULL, contact TEXT NOT NULL UNIQUE,
              password_hash TEXT NOT NULL, role TEXT NOT NULL,
              language TEXT NOT NULL DEFAULT 'en', active INTEGER NOT NULL DEFAULT 1,
              created_at TEXT NOT NULL, last_sign_in TEXT
            );
            CREATE TABLE IF NOT EXISTS sessions (
              token_hash TEXT PRIMARY KEY, user_id TEXT NOT NULL REFERENCES users(id),
              expires_at TEXT NOT NULL, created_at TEXT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS invitations (
              code_hash TEXT PRIMARY KEY, name TEXT NOT NULL, contact TEXT NOT NULL,
              role TEXT NOT NULL, created_by TEXT NOT NULL REFERENCES users(id),
              expires_at TEXT NOT NULL, used_at TEXT
            );
            CREATE TABLE IF NOT EXISTS shares (
              id TEXT PRIMARY KEY, patient_id TEXT NOT NULL REFERENCES users(id),
              clinician_id TEXT NOT NULL REFERENCES users(id),
              summary TEXT NOT NULL, questions_json TEXT NOT NULL,
              note TEXT NOT NULL DEFAULT '', language TEXT NOT NULL,
              status TEXT NOT NULL DEFAULT 'new', clinician_note TEXT NOT NULL DEFAULT '',
              created_at TEXT NOT NULL, updated_at TEXT NOT NULL,
              reviewed_at TEXT, revoked_at TEXT
            );
            CREATE INDEX IF NOT EXISTS shares_clinician ON shares(clinician_id, revoked_at, created_at);
            CREATE TABLE IF NOT EXISTS audit (
              id INTEGER PRIMARY KEY AUTOINCREMENT, actor_id TEXT NOT NULL,
              event TEXT NOT NULL, object_type TEXT NOT NULL, object_id TEXT NOT NULL,
              at TEXT NOT NULL
            );
        """)


def _string(value: Any, label: str, limit: int, required: bool = True) -> str:
    result = str(value or "").strip()
    if (required and not result) or len(result) > limit:
        raise ApiError(400, f"Invalid {label}")
    return result


def _password_hash(password: str) -> str:
    salt = secrets.token_bytes(16)
    digest = hashlib.scrypt(password.encode("utf-8"), salt=salt, n=2**14, r=8, p=1)
    return f"{salt.hex()}:{digest.hex()}"


def _password_matches(password: str, stored: str) -> bool:
    try:
        salt_hex, digest_hex = stored.split(":", 1)
        digest = hashlib.scrypt(password.encode("utf-8"), salt=bytes.fromhex(salt_hex), n=2**14, r=8, p=1)
        return hmac.compare_digest(digest, bytes.fromhex(digest_hex))
    except (ValueError, TypeError):
        return False


def _token_hash(token: str) -> str:
    return hashlib.sha256(token.encode("utf-8")).hexdigest()


def _public_user(row: sqlite3.Row) -> dict[str, Any]:
    return {key: row[key] for key in ("id", "name", "contact", "role", "language", "created_at", "last_sign_in")}


def _audit(db: sqlite3.Connection, actor: str, event: str, object_type: str, object_id: str) -> None:
    db.execute("INSERT INTO audit(actor_id,event,object_type,object_id,at) VALUES(?,?,?,?,?)", (actor, event, object_type, object_id, utc_now()))


def _create_user(db: sqlite3.Connection, name: str, contact: str, password: str, role: str, language: str = "en") -> sqlite3.Row:
    name = _string(name, "name", 100)
    contact = _string(contact, "contact", 160).lower()
    password = _string(password, "password", 128)
    if len(password) < 10:
        raise ApiError(400, "Password must have at least 10 characters")
    if role not in ("patient", "clinician", "admin") or language not in ("en", "hi", "bn", "ta"):
        raise ApiError(400, "Invalid account settings")
    user_id = secrets.token_hex(10)
    try:
        db.execute("INSERT INTO users(id,name,contact,password_hash,role,language,created_at) VALUES(?,?,?,?,?,?,?)",
                   (user_id, name, contact, _password_hash(password), role, language, utc_now()))
    except sqlite3.IntegrityError:
        raise ApiError(409, "This contact is already registered") from None
    return db.execute("SELECT * FROM users WHERE id=?", (user_id,)).fetchone()


def _session(db: sqlite3.Connection, user: sqlite3.Row) -> dict[str, Any]:
    db.execute("DELETE FROM sessions WHERE expires_at<=?", (utc_now(),))  # prune expired sessions
    token = secrets.token_urlsafe(32)
    expires = (datetime.now(timezone.utc) + timedelta(hours=SESSION_HOURS)).isoformat(timespec="seconds")
    db.execute("INSERT INTO sessions(token_hash,user_id,expires_at,created_at) VALUES(?,?,?,?)",
               (_token_hash(token), user["id"], expires, utc_now()))
    db.execute("UPDATE users SET last_sign_in=? WHERE id=?", (utc_now(), user["id"]))
    return {"token": token, "expires_at": expires, "user": _public_user(user)}


def _auth(db: sqlite3.Connection, authorization: str, roles: tuple[str, ...] = ()) -> sqlite3.Row:
    if not authorization.startswith("Bearer "):
        raise ApiError(401, "Sign in required")
    token = authorization[7:].strip()
    row = db.execute("SELECT u.* FROM sessions s JOIN users u ON u.id=s.user_id WHERE s.token_hash=? AND s.expires_at>? AND u.active=1",
                     (_token_hash(token), utc_now())).fetchone()
    if row is None:
        raise ApiError(401, "Session expired; please sign in again")
    if roles and row["role"] not in roles:
        raise ApiError(403, "This account cannot access this area")
    return row


def _share(row: sqlite3.Row, include_content: bool = True) -> dict[str, Any]:
    result = {key: row[key] for key in ("id", "patient_id", "clinician_id", "language", "status", "created_at", "updated_at", "reviewed_at", "revoked_at")}
    if include_content:
        result.update(summary=row["summary"], questions=json.loads(row["questions_json"]), note=row["note"], clinician_note=row["clinician_note"])
    return result


def handle(method: str, path: str, data: dict[str, Any], authorization: str) -> tuple[int, dict[str, Any]] | None:
    """Dispatch account/share API paths; return None for existing chat paths."""
    if path != "/api/me" and path != "/api/clinicians" and not path.startswith(("/api/me/", "/api/auth/", "/api/shares", "/api/clinician/", "/api/admin/")):
        return None
    with connection() as db:
        if method == "POST" and path == "/api/auth/register":
            user = _create_user(db, data.get("name"), data.get("contact"), data.get("password"), "patient", str(data.get("language") or "en"))
            _audit(db, user["id"], "register", "user", user["id"])
            return 201, _session(db, user)
        if method == "POST" and path == "/api/auth/login":
            contact = _string(data.get("contact"), "contact", 160).lower()
            password = _string(data.get("password"), "password", 128)
            user = db.execute("SELECT * FROM users WHERE contact=? AND active=1", (contact,)).fetchone()
            if user is None or not _password_matches(password, user["password_hash"]):
                raise ApiError(401, "Incorrect contact or password")
            _audit(db, user["id"], "login", "user", user["id"])
            return 200, _session(db, user)
        if method == "POST" and path == "/api/auth/accept-invite":
            code = _string(data.get("code"), "invitation code", 200)
            invitation = db.execute("SELECT * FROM invitations WHERE code_hash=? AND used_at IS NULL AND expires_at>?", (_token_hash(code), utc_now())).fetchone()
            if invitation is None:
                raise ApiError(400, "Invitation is invalid or expired")
            user = _create_user(db, invitation["name"], invitation["contact"], data.get("password"), invitation["role"])
            db.execute("UPDATE invitations SET used_at=? WHERE code_hash=?", (utc_now(), invitation["code_hash"]))
            _audit(db, user["id"], "accept_invite", "user", user["id"])
            return 201, _session(db, user)
        if method == "POST" and path == "/api/auth/logout":
            user = _auth(db, authorization)
            db.execute("DELETE FROM sessions WHERE token_hash=?", (_token_hash(authorization[7:].strip()),))
            _audit(db, user["id"], "logout", "user", user["id"])
            return 200, {"ok": True}
        if method == "GET" and path == "/api/me":
            return 200, {"user": _public_user(_auth(db, authorization))}
        if method == "POST" and path == "/api/me/profile":
            user = _auth(db, authorization)
            name = _string(data.get("name"), "name", 100)
            db.execute("UPDATE users SET name=? WHERE id=?", (name, user["id"]))
            _audit(db, user["id"], "update_profile", "user", user["id"])
            updated = db.execute("SELECT * FROM users WHERE id=?", (user["id"],)).fetchone()
            return 200, {"user": _public_user(updated)}
        if method == "POST" and path == "/api/me/password":
            user = _auth(db, authorization)
            current = _string(data.get("current_password"), "current password", 128)
            replacement = _string(data.get("new_password"), "new password", 128)
            if not _password_matches(current, user["password_hash"]):
                raise ApiError(403, "Current password is incorrect")
            if len(replacement) < 10 or replacement == current:
                raise ApiError(400, "Use a different password with at least 10 characters")
            db.execute("UPDATE users SET password_hash=? WHERE id=?", (_password_hash(replacement), user["id"]))
            db.execute("DELETE FROM sessions WHERE user_id=?", (user["id"],))
            _audit(db, user["id"], "change_password", "user", user["id"])
            return 200, {"ok": True}
        if method == "GET" and path == "/api/clinicians":
            _auth(db, authorization, ("patient",))
            rows = db.execute("SELECT id,name FROM users WHERE role='clinician' AND active=1 ORDER BY name").fetchall()
            return 200, {"clinicians": [dict(row) for row in rows]}
        if method == "POST" and path == "/api/shares":
            patient = _auth(db, authorization, ("patient",))
            clinician_id = _string(data.get("clinician_id"), "clinician", 40)
            clinician = db.execute("SELECT id FROM users WHERE id=? AND role='clinician' AND active=1", (clinician_id,)).fetchone()
            if clinician is None:
                raise ApiError(404, "Clinician is unavailable")
            summary = _string(data.get("summary"), "visit brief", 1200, required=False)
            note = _string(data.get("note"), "personal note", 500, required=False)
            questions = data.get("questions")
            if not isinstance(questions, list) or len(questions) > 10 or any(not isinstance(q, str) or not q.strip() or len(q) > 300 for q in questions):
                raise ApiError(400, "Choose up to ten short questions")
            if not summary and not questions:
                raise ApiError(400, "Add a brief or at least one question")
            if data.get("consent") is not True:
                raise ApiError(400, "Confirm consent before sharing")
            language = str(data.get("language") or patient["language"])
            if language not in ("en", "hi", "bn", "ta"):
                raise ApiError(400, "Invalid language")
            share_id, now = secrets.token_hex(10), utc_now()
            db.execute("INSERT INTO shares(id,patient_id,clinician_id,summary,questions_json,note,language,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?)",
                       (share_id, patient["id"], clinician_id, summary, json.dumps([q.strip() for q in questions], ensure_ascii=False), note, language, now, now))
            _audit(db, patient["id"], "share", "share", share_id)
            return 201, {"share": _share(db.execute("SELECT * FROM shares WHERE id=?", (share_id,)).fetchone())}
        if method == "GET" and path == "/api/shares/mine":
            patient = _auth(db, authorization, ("patient",))
            rows = db.execute("SELECT * FROM shares WHERE patient_id=? ORDER BY created_at DESC", (patient["id"],)).fetchall()
            return 200, {"shares": [_share(row) for row in rows]}
        if method == "DELETE" and path.startswith("/api/shares/"):
            patient = _auth(db, authorization, ("patient",))
            share_id = path.removeprefix("/api/shares/")
            row = db.execute("SELECT * FROM shares WHERE id=? AND patient_id=?", (share_id, patient["id"])).fetchone()
            if row is None:
                raise ApiError(404, "Share not found")
            if row["revoked_at"] is None:
                now = utc_now()
                db.execute("UPDATE shares SET revoked_at=?,updated_at=? WHERE id=?", (now, now, share_id))
                _audit(db, patient["id"], "revoke", "share", share_id)
            return 200, {"ok": True}
        if method == "GET" and path == "/api/clinician/queue":
            clinician = _auth(db, authorization, ("clinician",))
            rows = db.execute("SELECT s.*,u.name AS patient_name FROM shares s JOIN users u ON u.id=s.patient_id WHERE s.clinician_id=? AND s.revoked_at IS NULL ORDER BY s.created_at DESC", (clinician["id"],)).fetchall()
            return 200, {"shares": [{**_share(row, include_content=False), "patient_name": row["patient_name"],
                                     "brief_preview": row["summary"][:120]} for row in rows]}
        if method == "GET" and path.startswith("/api/clinician/shares/"):
            clinician = _auth(db, authorization, ("clinician",))
            share_id = path.removeprefix("/api/clinician/shares/")
            row = db.execute("SELECT s.*,u.name AS patient_name FROM shares s JOIN users u ON u.id=s.patient_id WHERE s.id=? AND s.clinician_id=? AND s.revoked_at IS NULL", (share_id, clinician["id"])).fetchone()
            if row is None:
                raise ApiError(404, "Share not found or access revoked")
            _audit(db, clinician["id"], "view", "share", share_id)
            return 200, {"share": {**_share(row), "patient_name": row["patient_name"]}}
        if method == "POST" and path.startswith("/api/clinician/shares/") and path.endswith("/review"):
            clinician = _auth(db, authorization, ("clinician",))
            share_id = path.removeprefix("/api/clinician/shares/").removesuffix("/review")
            row = db.execute("SELECT * FROM shares WHERE id=? AND clinician_id=? AND revoked_at IS NULL", (share_id, clinician["id"])).fetchone()
            if row is None:
                raise ApiError(404, "Share not found or access revoked")
            status = str(data.get("status") or "")
            if status not in ("in_review", "reviewed"):
                raise ApiError(400, "Invalid review status")
            note = _string(data.get("note"), "clinician note", 1000, required=False)
            now = utc_now()
            db.execute("UPDATE shares SET status=?,clinician_note=?,updated_at=?,reviewed_at=? WHERE id=?", (status, note, now, now if status == "reviewed" else None, share_id))
            _audit(db, clinician["id"], status, "share", share_id)
            return 200, {"share": _share(db.execute("SELECT * FROM shares WHERE id=?", (share_id,)).fetchone())}
        if method == "POST" and path == "/api/admin/invitations":
            admin = _auth(db, authorization, ("admin",))
            name = _string(data.get("name"), "name", 100)
            contact = _string(data.get("contact"), "contact", 160).lower()
            code = secrets.token_urlsafe(24)
            expires = (datetime.now(timezone.utc) + timedelta(days=7)).isoformat(timespec="seconds")
            db.execute("INSERT INTO invitations(code_hash,name,contact,role,created_by,expires_at) VALUES(?,?,?,?,?,?)",
                       (_token_hash(code), name, contact, "clinician", admin["id"], expires))
            _audit(db, admin["id"], "invite_clinician", "invitation", contact)
            return 201, {"invite_code": code, "expires_at": expires, "name": name, "contact": contact}
        if method == "GET" and path == "/api/admin/users":
            _auth(db, authorization, ("admin",))
            rows = db.execute("SELECT * FROM users ORDER BY created_at DESC").fetchall()
            return 200, {"users": [{**_public_user(row), "active": bool(row["active"])} for row in rows]}
        if method == "GET" and path == "/api/admin/audit":
            _auth(db, authorization, ("admin",))
            rows = db.execute("SELECT actor_id,event,object_type,object_id,at FROM audit ORDER BY id DESC LIMIT 100").fetchall()
            return 200, {"events": [dict(row) for row in rows]}
        if method == "POST" and path.startswith("/api/admin/users/") and path.endswith("/deactivate"):
            admin = _auth(db, authorization, ("admin",))
            user_id = path.removeprefix("/api/admin/users/").removesuffix("/deactivate")
            if user_id == admin["id"]:
                raise ApiError(400, "Cannot deactivate your own admin account")
            if db.execute("SELECT 1 FROM users WHERE id=?", (user_id,)).fetchone() is None:
                raise ApiError(404, "Account not found")
            db.execute("UPDATE users SET active=0 WHERE id=?", (user_id,))
            db.execute("DELETE FROM sessions WHERE user_id=?", (user_id,))
            _audit(db, admin["id"], "deactivate", "user", user_id)
            return 200, {"ok": True}
    raise ApiError(404, "Not found")


def seed_demo() -> None:
    init_db()
    with connection() as db:
        for role, name, contact in (("admin", "Demo Admin", "admin@sahay.demo"), ("clinician", "Dr. Meera Rao", "clinician@sahay.demo")):
            if db.execute("SELECT 1 FROM users WHERE contact=?", (contact,)).fetchone():
                print(f"{role}: already exists ({contact})")
                continue
            password = secrets.token_urlsafe(14)
            _create_user(db, name, contact, password, role)
            print(f"{role}: {contact} / {password}")
    print("These are fictional local demo accounts. Save the generated passwords before closing this terminal.")


if __name__ == "__main__":
    if sys.argv[1:] == ["seed-demo"]:
        seed_demo()
    else:
        raise SystemExit("Usage: python3 bookkeeping.py seed-demo")

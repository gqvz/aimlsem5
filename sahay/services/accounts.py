"""Accounts, sessions and administration. Every function takes the acting user explicitly."""
from __future__ import annotations

from datetime import UTC, datetime, timedelta
from typing import Any

from sqlalchemy import delete, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from ..config import Settings
from ..errors import ApiError
from ..models import Audit, Invitation, User, UserSession
from ..security import hash_password, hash_token, new_id, new_token, verify_password

LANGUAGES = ("en", "hi", "bn", "ta")
ROLES = ("patient", "clinician", "admin")
MIN_PASSWORD = 10


def utc_now() -> str:
    return datetime.now(UTC).isoformat(timespec="seconds")


def clean(value: Any, label: str, limit: int, required: bool = True) -> str:
    result = str(value or "").strip()
    if (required and not result) or len(result) > limit:
        raise ApiError(400, f"Invalid {label}")
    return result


def record(db: Session, actor_id: str, event: str, object_type: str, object_id: str) -> None:
    db.add(Audit(actor_id=actor_id, event=event, object_type=object_type, object_id=object_id, at=utc_now()))


def public_user(user: User) -> dict[str, Any]:
    return {"id": user.id, "name": user.name, "contact": user.contact, "role": user.role,
            "language": user.language, "created_at": user.created_at, "last_sign_in": user.last_sign_in}


def create_user(db: Session, name: Any, contact: Any, password: Any, role: str, language: str = "en") -> User:
    name = clean(name, "name", 100)
    contact = clean(contact, "contact", 160).lower()
    password = clean(password, "password", 128)
    if len(password) < MIN_PASSWORD:
        raise ApiError(400, f"Password must have at least {MIN_PASSWORD} characters")
    if role not in ROLES or language not in LANGUAGES:
        raise ApiError(400, "Invalid account settings")
    user = User(id=new_id(), name=name, contact=contact, password_hash=hash_password(password),
                role=role, language=language, active=1, created_at=utc_now())
    db.add(user)
    try:
        db.flush()
    except IntegrityError:
        db.rollback()
        raise ApiError(409, "This contact is already registered") from None
    return user


def open_session(db: Session, user: User) -> dict[str, Any]:
    db.execute(delete(UserSession).where(UserSession.expires_at <= utc_now()))  # prune expired
    token = new_token(32)
    expires = (datetime.now(UTC) + timedelta(hours=Settings.load().session_hours)).isoformat(timespec="seconds")
    db.add(UserSession(token_hash=hash_token(token), user_id=user.id, expires_at=expires, created_at=utc_now()))
    user.last_sign_in = utc_now()
    return {"token": token, "expires_at": expires, "user": public_user(user)}


def authenticate(db: Session, authorization: str, roles: tuple[str, ...] = ()) -> User:
    if not authorization.startswith("Bearer "):
        raise ApiError(401, "Sign in required")
    token = authorization[7:].strip()
    row = db.execute(
        select(User).join(UserSession, UserSession.user_id == User.id)
        .where(UserSession.token_hash == hash_token(token), UserSession.expires_at > utc_now(), User.active == 1)
    ).scalar_one_or_none()
    if row is None:
        raise ApiError(401, "Session expired; please sign in again")
    if roles and row.role not in roles:
        raise ApiError(403, "This account cannot access this area")
    return row


def register_patient(db: Session, data: dict[str, Any]) -> dict[str, Any]:
    user = create_user(db, data.get("name"), data.get("contact"), data.get("password"), "patient", str(data.get("language") or "en"))
    record(db, user.id, "register", "user", user.id)
    return open_session(db, user)


def login(db: Session, data: dict[str, Any]) -> dict[str, Any]:
    contact = clean(data.get("contact"), "contact", 160).lower()
    password = clean(data.get("password"), "password", 128)
    user = db.execute(select(User).where(User.contact == contact, User.active == 1)).scalar_one_or_none()
    if user is None or not verify_password(password, user.password_hash):
        raise ApiError(401, "Incorrect contact or password")
    record(db, user.id, "login", "user", user.id)
    return open_session(db, user)


def accept_invite(db: Session, data: dict[str, Any]) -> dict[str, Any]:
    code = clean(data.get("code"), "invitation code", 200)
    invitation = db.execute(select(Invitation).where(
        Invitation.code_hash == hash_token(code), Invitation.used_at.is_(None), Invitation.expires_at > utc_now()
    )).scalar_one_or_none()
    if invitation is None:
        raise ApiError(400, "Invitation is invalid or expired")
    user = create_user(db, invitation.name, invitation.contact, data.get("password"), invitation.role)
    invitation.used_at = utc_now()
    record(db, user.id, "accept_invite", "user", user.id)
    return open_session(db, user)


def logout(db: Session, user: User, authorization: str) -> None:
    db.execute(delete(UserSession).where(UserSession.token_hash == hash_token(authorization[7:].strip())))
    record(db, user.id, "logout", "user", user.id)


def update_profile(db: Session, user: User, name: Any) -> dict[str, Any]:
    user.name = clean(name, "name", 100)
    record(db, user.id, "update_profile", "user", user.id)
    return public_user(user)


def change_password(db: Session, user: User, current: Any, replacement: Any) -> None:
    current = clean(current, "current password", 128)
    replacement = clean(replacement, "new password", 128)
    if not verify_password(current, user.password_hash):
        raise ApiError(403, "Current password is incorrect")
    if len(replacement) < MIN_PASSWORD or replacement == current:
        raise ApiError(400, f"Use a different password with at least {MIN_PASSWORD} characters")
    user.password_hash = hash_password(replacement)
    db.execute(delete(UserSession).where(UserSession.user_id == user.id))
    record(db, user.id, "change_password", "user", user.id)


def list_clinicians(db: Session) -> list[dict[str, str]]:
    rows = db.execute(select(User.id, User.name).where(User.role == "clinician", User.active == 1).order_by(User.name)).all()
    return [{"id": r.id, "name": r.name} for r in rows]


# --- administration ---------------------------------------------------------------------

def invite_clinician(db: Session, admin: User, data: dict[str, Any]) -> dict[str, Any]:
    name = clean(data.get("name"), "name", 100)
    contact = clean(data.get("contact"), "contact", 160).lower()
    code = new_token(24)
    expires = (datetime.now(UTC) + timedelta(days=7)).isoformat(timespec="seconds")
    db.add(Invitation(code_hash=hash_token(code), name=name, contact=contact, role="clinician",
                      created_by=admin.id, expires_at=expires))
    record(db, admin.id, "invite_clinician", "invitation", contact)
    return {"invite_code": code, "expires_at": expires, "name": name, "contact": contact}


def admin_users(db: Session) -> list[dict[str, Any]]:
    rows = db.execute(select(User).order_by(User.created_at.desc())).scalars().all()
    return [{**public_user(u), "active": bool(u.active)} for u in rows]


def admin_audit(db: Session, limit: int = 100) -> list[dict[str, Any]]:
    rows = db.execute(select(Audit).order_by(Audit.id.desc()).limit(limit)).scalars().all()
    return [{"actor_id": a.actor_id, "event": a.event, "object_type": a.object_type, "object_id": a.object_id, "at": a.at} for a in rows]


def deactivate_user(db: Session, admin: User, user_id: str) -> None:
    if user_id == admin.id:
        raise ApiError(400, "Cannot deactivate your own admin account")
    target = db.get(User, user_id)
    if target is None:
        raise ApiError(404, "Account not found")
    target.active = 0
    db.execute(delete(UserSession).where(UserSession.user_id == user_id))
    record(db, admin.id, "deactivate", "user", user_id)

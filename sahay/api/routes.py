"""HTTP routes. URLs, methods, status codes and JSON shapes are the public contract."""
from __future__ import annotations

from typing import Any

from fastapi import APIRouter, Depends, Header, Request, Response
from sqlalchemy.orm import Session

from sahay.domain.content import bundle
from engine import LANGS, SOURCES, ModelClient, public_model_configuration, respond, test_model_connection

from ..db import get_session
from ..errors import ApiError
from ..models import User
from ..providers import speech
from ..ratelimit import LIMITS
from ..services import accounts, care, shares
from .deps import LIMITER, client_ip, current_user, json_body, throttle

router = APIRouter(prefix="/api")

AGENTS = ["Supervisor", "Language", "Safety", "Emotional support", "Knowledge", "Activity", "Visit preparation", "Review"]
# scope="function": commit (or roll back) before the response is sent, so a client never sees stale data.
Db = Depends(get_session, scope="function")
Body = Depends(json_body)
Patient = Depends(current_user("patient"))
Clinician = Depends(current_user("clinician"))
Admin = Depends(current_user("admin"))
Member = Depends(current_user("patient", "clinician"))
Anyone = Depends(current_user())


def no_store(response: Response) -> None:
    response.headers["Cache-Control"] = "no-store"


# --- public ------------------------------------------------------------------------------

@router.get("/health")
def health() -> dict[str, Any]:
    client = ModelClient()
    return {"ok": True, "mode": "live" if client.ready else "demo", "model_config": public_model_configuration(),
            "languages": LANGS, "agents": AGENTS, "speech": speech.status()}


@router.get("/cards")
def cards() -> dict[str, Any]:
    return {"cards": [{"id": key, **value} for key, value in SOURCES.items()]}


@router.get("/content")
def content_bundle() -> dict[str, Any]:
    return bundle()


@router.post("/message", dependencies=[Depends(throttle("message"))])
def message(data: dict[str, Any] = Body) -> dict[str, Any]:
    return respond(data)


@router.post("/model/test", dependencies=[Depends(throttle("model_test"))])
def model_test(data: dict[str, Any] = Body) -> dict[str, Any]:
    return test_model_connection(data)


@router.get("/speech/status")
def speech_status() -> dict[str, Any]:
    return speech.status()


@router.post("/speech/transcribe", dependencies=[Depends(throttle("speech"))])
def transcribe(data: dict[str, Any] = Body) -> dict[str, Any]:
    return speech.transcribe(data)


@router.post("/speech/synthesize", dependencies=[Depends(throttle("speech"))])
def synthesize(data: dict[str, Any] = Body) -> Response:
    return Response(speech.synthesize(data), media_type="audio/mpeg")


# --- auth and profile --------------------------------------------------------------------

@router.post("/auth/register", status_code=201, dependencies=[Depends(throttle("register"))])
def register(data: dict[str, Any] = Body, db: Session = Db) -> dict[str, Any]:
    return accounts.register_patient(db, data)


@router.post("/auth/login")
def login(request: Request, data: dict[str, Any] = Body, db: Session = Db) -> dict[str, Any]:
    key = f"login_failures:{client_ip(request)}:{str(data.get('contact', '')).lower()[:160]}"
    limit, window = LIMITS["login_failures"]
    wait = LIMITER.peek(key, limit, window)
    if wait:
        raise ApiError(429, "Too many sign-in attempts. Please wait before trying again.", int(wait) + 1)
    try:
        result = accounts.login(db, data)
    except ApiError as error:
        if error.status == 401:
            LIMITER.record(key)
        raise
    LIMITER.reset(key)
    return result


@router.post("/auth/accept-invite", status_code=201)
def accept_invite(data: dict[str, Any] = Body, db: Session = Db) -> dict[str, Any]:
    return accounts.accept_invite(db, data)


@router.post("/auth/logout")
def logout(authorization: str = Header(default=""), user: User = Anyone, db: Session = Db) -> dict[str, Any]:
    accounts.logout(db, user, authorization)
    return {"ok": True}


@router.get("/me")
def me(user: User = Anyone) -> dict[str, Any]:
    return {"user": accounts.public_user(user)}


@router.post("/me/profile")
def profile(data: dict[str, Any] = Body, user: User = Anyone, db: Session = Db) -> dict[str, Any]:
    return {"user": accounts.update_profile(db, user, data.get("name"))}


@router.post("/me/password")
def password(data: dict[str, Any] = Body, user: User = Anyone, db: Session = Db) -> dict[str, Any]:
    accounts.change_password(db, user, data.get("current_password"), data.get("new_password"))
    return {"ok": True}


# --- visit-brief sharing -----------------------------------------------------------------

@router.get("/clinicians")
def clinicians(user: User = Patient, db: Session = Db) -> dict[str, Any]:
    return {"clinicians": accounts.list_clinicians(db)}


@router.post("/shares", status_code=201)
def create_share(data: dict[str, Any] = Body, user: User = Patient, db: Session = Db) -> dict[str, Any]:
    return {"share": shares.create(db, user, data)}


@router.get("/shares/mine")
def my_shares(user: User = Patient, db: Session = Db) -> dict[str, Any]:
    return {"shares": shares.mine(db, user)}


@router.delete("/shares/{share_id}")
def revoke_share(share_id: str, user: User = Patient, db: Session = Db) -> dict[str, Any]:
    shares.revoke(db, user, share_id)
    return {"ok": True}


@router.get("/clinician/queue")
def queue(user: User = Clinician, db: Session = Db) -> dict[str, Any]:
    return {"shares": shares.queue(db, user)}


@router.get("/clinician/shares/{share_id}")
def clinician_share(share_id: str, user: User = Clinician, db: Session = Db) -> dict[str, Any]:
    return {"share": shares.clinician_view(db, user, share_id)}


@router.post("/clinician/shares/{share_id}/review")
def review_share(share_id: str, data: dict[str, Any] = Body, user: User = Clinician, db: Session = Db) -> dict[str, Any]:
    return {"share": shares.review(db, user, share_id, data)}


# --- care conversations ------------------------------------------------------------------

@router.get("/care/contacts")
def care_contacts(user: User = Member, db: Session = Db) -> dict[str, Any]:
    return {"contacts": care.contacts(db, user)}


@router.get("/care/threads")
def care_threads(user: User = Member, db: Session = Db) -> dict[str, Any]:
    return {"threads": care.threads(db, user)}


@router.post("/care/threads", status_code=201)
def care_open(data: dict[str, Any] = Body, user: User = Member, db: Session = Db) -> dict[str, Any]:
    return {"thread": care.open_thread(db, user, data)}


@router.get("/care/threads/{thread_id}/messages")
def care_messages(thread_id: str, user: User = Member, db: Session = Db) -> dict[str, Any]:
    return {"messages": care.messages(db, user, thread_id)}


@router.post("/care/threads/{thread_id}/messages", status_code=201)
def care_post(thread_id: str, data: dict[str, Any] = Body, user: User = Member, db: Session = Db) -> dict[str, Any]:
    return {"ok": True, "message_id": care.post_message(db, user, thread_id, data)}


@router.get("/care/files/{file_id}")
def care_file(file_id: str, user: User = Member, db: Session = Db) -> Response:
    payload, mime, _name = care.attachment(db, user, file_id)
    return Response(payload, media_type=mime)


# --- administration ----------------------------------------------------------------------

@router.post("/admin/invitations", status_code=201)
def invite(data: dict[str, Any] = Body, user: User = Admin, db: Session = Db) -> dict[str, Any]:
    return accounts.invite_clinician(db, user, data)


@router.get("/admin/users")
def admin_users(user: User = Admin, db: Session = Db) -> dict[str, Any]:
    return {"users": accounts.admin_users(db)}


@router.get("/admin/audit")
def admin_audit(user: User = Admin, db: Session = Db) -> dict[str, Any]:
    return {"events": accounts.admin_audit(db)}


@router.post("/admin/users/{user_id}/deactivate")
def deactivate(user_id: str, user: User = Admin, db: Session = Db) -> dict[str, Any]:
    accounts.deactivate_user(db, user, user_id)
    return {"ok": True}

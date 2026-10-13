"""Patient-consented visit briefs and clinician review."""
from __future__ import annotations

import json
from typing import Any

from sqlalchemy import select
from sqlalchemy.orm import Session

from ..errors import ApiError
from ..models import Share, User
from ..security import new_id
from .accounts import LANGUAGES, clean, record, utc_now


def view(share: Share, include_content: bool = True) -> dict[str, Any]:
    result = {k: getattr(share, k) for k in ("id", "patient_id", "clinician_id", "language", "status",
                                              "created_at", "updated_at", "reviewed_at", "revoked_at")}
    if include_content:
        result.update(summary=share.summary, questions=json.loads(share.questions_json),
                      note=share.note, clinician_note=share.clinician_note)
    return result


def create(db: Session, patient: User, data: dict[str, Any]) -> dict[str, Any]:
    clinician_id = clean(data.get("clinician_id"), "clinician", 40)
    clinician = db.execute(select(User).where(User.id == clinician_id, User.role == "clinician", User.active == 1)).scalar_one_or_none()
    if clinician is None:
        raise ApiError(404, "Clinician is unavailable")
    summary = clean(data.get("summary"), "visit brief", 1200, required=False)
    note = clean(data.get("note"), "personal note", 500, required=False)
    questions = data.get("questions")
    if (not isinstance(questions, list) or len(questions) > 10
            or any(not isinstance(q, str) or not q.strip() or len(q) > 300 for q in questions)):
        raise ApiError(400, "Choose up to ten short questions")
    if not summary and not questions:
        raise ApiError(400, "Add a brief or at least one question")
    if data.get("consent") is not True:
        raise ApiError(400, "Confirm consent before sharing")
    language = str(data.get("language") or patient.language)
    if language not in LANGUAGES:
        raise ApiError(400, "Invalid language")
    now = utc_now()
    share = Share(id=new_id(), patient_id=patient.id, clinician_id=clinician.id, summary=summary,
                  questions_json=json.dumps([q.strip() for q in questions], ensure_ascii=False),
                  note=note, language=language, created_at=now, updated_at=now)
    db.add(share)
    record(db, patient.id, "share", "share", share.id)
    db.flush()
    return view(share)


def mine(db: Session, patient: User) -> list[dict[str, Any]]:
    rows = db.execute(select(Share).where(Share.patient_id == patient.id).order_by(Share.created_at.desc())).scalars().all()
    return [view(s) for s in rows]


def revoke(db: Session, patient: User, share_id: str) -> None:
    share = db.execute(select(Share).where(Share.id == share_id, Share.patient_id == patient.id)).scalar_one_or_none()
    if share is None:
        raise ApiError(404, "Share not found")
    if share.revoked_at is None:
        now = utc_now()
        share.revoked_at, share.updated_at = now, now
        record(db, patient.id, "revoke", "share", share_id)


def queue(db: Session, clinician: User) -> list[dict[str, Any]]:
    rows = db.execute(
        select(Share, User.name).join(User, User.id == Share.patient_id)
        .where(Share.clinician_id == clinician.id, Share.revoked_at.is_(None)).order_by(Share.created_at.desc())
    ).all()
    return [{**view(s, include_content=False), "patient_name": name, "brief_preview": s.summary[:120]} for s, name in rows]


def _owned(db: Session, clinician: User, share_id: str) -> tuple[Share, str]:
    row = db.execute(
        select(Share, User.name).join(User, User.id == Share.patient_id)
        .where(Share.id == share_id, Share.clinician_id == clinician.id, Share.revoked_at.is_(None))
    ).first()
    if row is None:
        raise ApiError(404, "Share not found or access revoked")
    return row[0], row[1]


def clinician_view(db: Session, clinician: User, share_id: str) -> dict[str, Any]:
    share, patient_name = _owned(db, clinician, share_id)
    record(db, clinician.id, "view", "share", share_id)
    return {**view(share), "patient_name": patient_name}


def review(db: Session, clinician: User, share_id: str, data: dict[str, Any]) -> dict[str, Any]:
    share, _ = _owned(db, clinician, share_id)
    status = str(data.get("status") or "")
    if status not in ("in_review", "reviewed"):
        raise ApiError(400, "Invalid review status")
    note = clean(data.get("note"), "clinician note", 1000, required=False)
    now = utc_now()
    share.status, share.clinician_note, share.updated_at = status, note, now
    share.reviewed_at = now if status == "reviewed" else None
    record(db, clinician.id, status, "share", share_id)
    return view(share)

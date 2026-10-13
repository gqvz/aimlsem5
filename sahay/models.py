"""Database tables. Column names and types match the original sqlite schema exactly, so an
existing database file is adopted as-is (see alembic/versions/0001_baseline.py)."""
from __future__ import annotations

from sqlalchemy import ForeignKey, Index, Integer, LargeBinary, String, Text, UniqueConstraint
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class User(Base):
    __tablename__ = "users"
    id: Mapped[str] = mapped_column(String, primary_key=True)
    name: Mapped[str] = mapped_column(String)
    contact: Mapped[str] = mapped_column(String, unique=True)
    password_hash: Mapped[str] = mapped_column(String)
    role: Mapped[str] = mapped_column(String)
    language: Mapped[str] = mapped_column(String, default="en", server_default="en")
    active: Mapped[int] = mapped_column(Integer, default=1, server_default="1")
    created_at: Mapped[str] = mapped_column(String)
    last_sign_in: Mapped[str | None] = mapped_column(String, nullable=True)


class UserSession(Base):
    __tablename__ = "sessions"
    token_hash: Mapped[str] = mapped_column(String, primary_key=True)
    user_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    expires_at: Mapped[str] = mapped_column(String)
    created_at: Mapped[str] = mapped_column(String)


class Invitation(Base):
    __tablename__ = "invitations"
    code_hash: Mapped[str] = mapped_column(String, primary_key=True)
    name: Mapped[str] = mapped_column(String)
    contact: Mapped[str] = mapped_column(String)
    role: Mapped[str] = mapped_column(String)
    created_by: Mapped[str] = mapped_column(ForeignKey("users.id"))
    expires_at: Mapped[str] = mapped_column(String)
    used_at: Mapped[str | None] = mapped_column(String, nullable=True)


class Share(Base):
    __tablename__ = "shares"
    __table_args__ = (Index("shares_clinician", "clinician_id", "revoked_at", "created_at"),)
    id: Mapped[str] = mapped_column(String, primary_key=True)
    patient_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    clinician_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    summary: Mapped[str] = mapped_column(Text)
    questions_json: Mapped[str] = mapped_column(Text)
    note: Mapped[str] = mapped_column(Text, default="", server_default="")
    language: Mapped[str] = mapped_column(String)
    status: Mapped[str] = mapped_column(String, default="new", server_default="new")
    clinician_note: Mapped[str] = mapped_column(Text, default="", server_default="")
    created_at: Mapped[str] = mapped_column(String)
    updated_at: Mapped[str] = mapped_column(String)
    reviewed_at: Mapped[str | None] = mapped_column(String, nullable=True)
    revoked_at: Mapped[str | None] = mapped_column(String, nullable=True)


class Audit(Base):
    __tablename__ = "audit"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    actor_id: Mapped[str] = mapped_column(String)
    event: Mapped[str] = mapped_column(String)
    object_type: Mapped[str] = mapped_column(String)
    object_id: Mapped[str] = mapped_column(String)
    at: Mapped[str] = mapped_column(String)


class CareThread(Base):
    __tablename__ = "care_threads"
    __table_args__ = (UniqueConstraint("patient_id", "clinician_id"),)
    id: Mapped[str] = mapped_column(String, primary_key=True)
    patient_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    clinician_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    created_at: Mapped[str] = mapped_column(String)


class CareMessage(Base):
    __tablename__ = "care_messages"
    id: Mapped[str] = mapped_column(String, primary_key=True)
    thread_id: Mapped[str] = mapped_column(ForeignKey("care_threads.id"))
    sender_id: Mapped[str] = mapped_column(ForeignKey("users.id"))
    text: Mapped[str] = mapped_column(Text)
    kind: Mapped[str] = mapped_column(String)
    request_id: Mapped[str | None] = mapped_column(String, nullable=True)
    fulfilled_at: Mapped[str | None] = mapped_column(String, nullable=True)
    created_at: Mapped[str] = mapped_column(String)


class CareFile(Base):
    __tablename__ = "care_files"
    id: Mapped[str] = mapped_column(String, primary_key=True)
    message_id: Mapped[str] = mapped_column(ForeignKey("care_messages.id"))
    name: Mapped[str] = mapped_column(String)
    mime: Mapped[str] = mapped_column(String)
    bytes: Mapped[bytes] = mapped_column(LargeBinary)
    size: Mapped[int] = mapped_column(Integer)

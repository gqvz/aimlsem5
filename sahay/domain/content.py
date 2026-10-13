"""Curated, reviewed content, loaded from the versioned JSON files in `content/`.

Edit the JSON, not this module. `CONTENT_VERSION` changes whenever any file does, so
clients can tell when their bundled copy is stale. Copy that a clinician or native speaker
should review lives only in these files.
"""
from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path
from typing import Any

CONTENT_DIR = Path(__file__).resolve().parents[2] / "content"
LANGS = {"en": "English", "hi": "Hindi", "bn": "Bengali", "ta": "Tamil"}


def _load(name: str) -> Any:
    return json.loads((CONTENT_DIR / name).read_text(encoding="utf-8"))


_sources = _load("sources.json")
SOURCES: dict[str, dict[str, str]] = {k: {f: v[f] for f in ("title", "url", "summary")} for k, v in _sources.items()}
SOURCE_SUMMARIES: dict[str, dict[str, str]] = {
    lang: {k: v["summary_translations"][lang] for k, v in _sources.items() if lang in v.get("summary_translations", {})}
    for lang in ("hi", "bn", "ta")
}
FALLBACK: dict[str, dict[str, str]] = _load("fallback.json")
ACTIVITIES: dict[str, dict[str, Any]] = {a["id"]: {k: v for k, v in a.items() if k != "id"} for a in _load("activities.json")}
_intros = _load("intros.json")
BRIEF_INTRO: dict[str, str] = _intros["brief"]
ACTIVITY_INTRO: dict[str, str] = _intros["activity"]

CONTENT_VERSION = hashlib.sha256(
    b"".join((CONTENT_DIR / n).read_bytes() for n in sorted(p.name for p in CONTENT_DIR.glob("*.json")))).hexdigest()[:12]


def bundle() -> dict[str, Any]:
    """Everything a client may cache: served at /api/content."""
    return {"version": CONTENT_VERSION, "languages": LANGS, "sources": SOURCES, "source_summaries": SOURCE_SUMMARIES,
            "activities": [{"id": k, **v} for k, v in ACTIVITIES.items()],
            "safety_copy": {lang: {k: FALLBACK[lang][k] for k in ("crisis", "medical", "medical_boundary")} for lang in FALLBACK}}


def choose_card(text: str) -> tuple[str | None, dict[str, str] | None]:
    if re.search(r"\b(fever|infection|temperature)\b", text, re.IGNORECASE):
        key = "infection"
    elif re.search(r"\b(relax|meditat|mindful|ground)\b", text, re.IGNORECASE):
        key = "relaxation"
    elif re.search(r"\b(doctor|visit|question|appointment)\b", text, re.IGNORECASE):
        key = "communication"
    elif re.search(r"\b(anxiety|stress|feelings?|emotions?|fear|sad|worr\w*|lonel\w*)\b", text, re.IGNORECASE):
        key = "feelings"
    else:
        return None, None
    return key, SOURCES[key]


def fallback_response(lang: str, action: str, card: dict[str, str] | None, card_key: str | None) -> str:
    if action == "coping":
        return FALLBACK[lang]["coping"]
    if action == "info" and card and card_key:
        summary = SOURCE_SUMMARIES.get(lang, {}).get(card_key, card["summary"])
        return f"{FALLBACK[lang]['info']}\n\n{summary}"
    if action == "info":
        return FALLBACK[lang]["info_unavailable"]
    return FALLBACK[lang].get(action, FALLBACK[lang]["reflect"])

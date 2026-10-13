"""Cheap, deterministic routing used when no model is available and as the model's hint."""
from __future__ import annotations

import re

from sahay.domain.safety import assess


def choose_action(text: str, requested: str | None) -> str:
    if assess(text).medical_request:
        return "medical_boundary"
    if requested in {"reflect", "coping", "info", "questions"}:
        return requested
    if re.search(r"\b(questions? (?:for|to ask)|what (?:should|can) i ask|prepare (?:for|questions)|ask my doctor|travel costs?)\b", text, re.IGNORECASE):
        return "questions"
    if re.search(r"\b(what is|information|source|evidence|learn about|read about)\b", text, re.IGNORECASE):
        return "info"
    return "reflect"

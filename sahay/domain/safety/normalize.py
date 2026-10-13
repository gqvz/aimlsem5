"""Text normalisation and typo repair used before any safety rule runs."""
from __future__ import annotations

import difflib
import re
import unicodedata

# Words whose misspelling would hide a crisis or urgent-medical phrase. Kept small on purpose:
# every entry widens the net for false positives.
_VOCAB = (
    "kill", "myself", "suicide", "suicidal", "die", "dead", "alive", "harm", "hurt", "life",
    "end", "want", "wake", "live", "fever", "breathe", "breath", "bleeding", "chest", "pain",
    "medicine", "medication", "tablets", "dose", "dosage", "stop", "double", "increase", "decrease",
)
_VOCAB_SET = frozenset(_VOCAB)
_INVISIBLE = dict.fromkeys(map(ord, "\u200b‌‍⁠﻿"), None)
_APOSTROPHES = dict.fromkeys(map(ord, "'’‘`´"), None)
_PUNCT = re.compile(r"[^\w\sऀ-ॿঀ-৿஀-௿]+", re.UNICODE)
_REPEAT = re.compile(r"(.)\1{2,}")
_ASCII_WORD = re.compile(r"^[a-z]{4,}$")


def normalize(text: str) -> str:
    text = unicodedata.normalize("NFC", text).translate(_INVISIBLE).translate(_APOSTROPHES).lower()
    text = _REPEAT.sub(r"\1\1", text)  # "kiiiill" -> "kiill"
    text = _PUNCT.sub(" ", text)
    return re.sub(r"\s+", " ", text).strip()


def repair_typos(text: str) -> str:
    """Snap near-misses of safety vocabulary ("kil", "sucide") back to the real word."""
    fixed: list[str] = []
    for word in text.split(" "):
        if _ASCII_WORD.match(word) and word not in _VOCAB_SET:
            match = difflib.get_close_matches(word, _VOCAB, n=1, cutoff=0.84)
            word = match[0] if match else word
        elif len(word) == 3 and word.isascii() and word.isalpha() and word not in _VOCAB_SET:
            # three-letter slips like "kil" only when they are one deletion from a vocabulary word
            candidates = [v for v in ("kill", "die", "end") if v != word and difflib.SequenceMatcher(None, word, v).ratio() >= 0.85]
            word = candidates[0] if candidates and word.startswith(candidates[0][:2]) else word
        fixed.append(word)
    return " ".join(fixed)

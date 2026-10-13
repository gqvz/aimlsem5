"""Safety gate regression corpus. Known gaps are strict xfails: fixing one makes the
test fail until its 'known_gap' marker is removed from the corpus."""
import json
from pathlib import Path

import pytest

from sahay.domain.safety import assess as _assess

CASES = json.loads((Path(__file__).parent / "data" / "safety_corpus.json").read_text(encoding="utf-8"))["cases"]


def assess(text: str) -> tuple[str, bool]:
    result = _assess(text)
    return result.risk, result.medical_request


def _param(case):
    marks = [pytest.mark.xfail(strict=True, reason=case["known_gap"])] if case.get("known_gap") else []
    return pytest.param(case, id=f'{case["lang"]}-{case["text"][:40]}', marks=marks)


@pytest.mark.parametrize("case", [_param(c) for c in CASES])
def test_safety_gate(case):
    risk, medical_request = assess(case["text"])
    assert risk == case["risk"]
    assert medical_request == case.get("medical_request", False)


def test_every_language_is_covered():
    assert {c["lang"] for c in CASES} == {"en", "hi", "bn", "ta"}


def test_patterns_use_only_their_own_script():
    """S1 guard: an alternative must not mix Latin/Devanagari/Bengali/Tamil letters."""
    import unicodedata
    from sahay.domain.safety import CRISIS_PATTERNS, DEPENDENCY_PATTERNS, MEDICAL_PATTERNS, MEDICAL_REQUEST_PATTERNS
    def script(ch):
        name = unicodedata.name(ch, "")
        for s in ("DEVANAGARI", "BENGALI", "TAMIL"):
            if name.startswith(s):
                return s
        return None
    for pattern in (*CRISIS_PATTERNS, *MEDICAL_PATTERNS, *DEPENDENCY_PATTERNS, *MEDICAL_REQUEST_PATTERNS):
        for alt in pattern.split("|"):
            scripts = {script(c) for c in alt if script(c)}
            assert len(scripts) <= 1, f"mixed scripts in {alt!r}: {scripts}"

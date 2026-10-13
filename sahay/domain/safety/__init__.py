"""Safety assessment: the only place that decides how urgent a message is.

`assess` is deterministic, offline and never raises. A model may raise the assessed risk
(see `more_cautious`) but nothing may lower it.
"""
from __future__ import annotations

import re
from dataclasses import dataclass

from .normalize import normalize, repair_typos
from .rules import CRISIS_PATTERNS, DEPENDENCY_PATTERNS, MEDICAL_PATTERNS, MEDICAL_REQUEST_PATTERNS

RISK_ORDER = {"ordinary": 0, "concerning": 1, "urgent_medical": 2, "crisis": 3}

__all__ = [
    "CRISIS_PATTERNS",
    "DEPENDENCY_PATTERNS",
    "MEDICAL_PATTERNS",
    "MEDICAL_REQUEST_PATTERNS",
    "RISK_ORDER",
    "RiskAssessment",
    "assess",
    "matches",
    "more_cautious",
]


@dataclass(frozen=True)
class RiskAssessment:
    risk: str = "ordinary"
    medical_request: bool = False
    dependency: bool = False
    rules: tuple[str, ...] = ()  # rule-group names only; never the user's text

    @property
    def needs_human_help(self) -> bool:
        return self.risk in ("crisis", "urgent_medical")


def matches(text: str, patterns: list[str]) -> bool:
    return any(re.search(p, text, re.IGNORECASE) for p in patterns)


def _variants(text: str) -> list[str]:
    base = normalize(text)
    repaired = repair_typos(base)
    return [text, base] if repaired == base else [text, base, repaired]


def assess(text: str) -> RiskAssessment:
    views = _variants(text or "")
    hit = lambda patterns: any(matches(v, patterns) for v in views)
    rules: list[str] = []
    risk = "ordinary"
    if hit(CRISIS_PATTERNS):
        risk = "crisis"; rules.append("crisis")
    elif hit(MEDICAL_PATTERNS):
        risk = "urgent_medical"; rules.append("urgent_medical")
    dependency = hit(DEPENDENCY_PATTERNS)
    if dependency:
        rules.append("dependency")
        if risk == "ordinary":
            risk = "concerning"
    request = hit(MEDICAL_REQUEST_PATTERNS)
    if request:
        rules.append("medical_request")
    return RiskAssessment(risk, request, dependency, tuple(rules))


def more_cautious(*risks: str) -> str:
    return max((r for r in risks if r in RISK_ORDER), key=lambda r: RISK_ORDER[r], default="concerning")

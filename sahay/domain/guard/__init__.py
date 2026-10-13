"""Output guard: every model-written string passes here before a person sees it.

Layers, cheapest first, all must pass:
  1. deterministic  - forbidden claims, unapproved phone numbers/links, dose-like quantities
  2. similarity     - offline char-n-gram match against prohibited-claim exemplars (hi/bn/ta/en)
  3. judge          - an independent model review (fail-closed; skipped only when no model exists)

The caller substitutes curated fixed copy whenever `allowed` is False. The guard never
edits or "repairs" model text.
"""
from __future__ import annotations

import re
from dataclasses import dataclass, field

from .judge import Completer, judge
from .similarity import SimilarityGuard

APPROVED_NUMBERS = frozenset({"112", "14416"})
_NUMBER = re.compile(r"(?<![\w.])\+?\d[\d\s\-]{2,}\d(?![\w])")
_QUANTITY = re.compile(
    r"\d+(?:[.,]\d+)?\s*(?:mg|mcg|µg|ml|iu|units?|tablets?|pills?|capsules?|drops?|"
    r"मिलीग्राम|मि\.?\s?ग्रा|गोल(?:ी|ियाँ|ियां|ियों)|बूँद|"
    r"মিলিগ্রাম|ট্যাবলেট|ফোঁটা|"
    r"மில்லிகிராம்|மாத்திரை(?:கள்)?|துளி)", re.IGNORECASE)
_URL = re.compile(r"https?://[^\s)]+")

_similarity = SimilarityGuard()


@dataclass
class GuardVerdict:
    allowed: bool
    layer: str = ""           # which layer blocked, empty when allowed
    categories: list[str] = field(default_factory=list)
    judge: str = "skipped"    # skipped | judged | unavailable | unparsable


def check_deterministic(text: str, *, allowed_urls: set[str], extra_numbers: set[str] = frozenset()) -> str | None:
    """Return a reason string when the text breaks a hard rule, else None."""
    approved = APPROVED_NUMBERS | set(extra_numbers)
    for found in _NUMBER.findall(text):
        digits = re.sub(r"\D", "", found)
        if digits not in approved and len(digits) >= 3:
            return "unapproved_number"
    if _QUANTITY.search(text):
        return "dose_like_quantity"
    for url in (u.rstrip(".,;:!") for u in _URL.findall(text)):
        if url not in allowed_urls:
            return "unapproved_url"
    return None


def check(text: str, *, lang: str = "en", allowed_urls: set[str] | None = None,
          judge_client: Completer | None = None, extra_numbers: set[str] = frozenset(),
          require_judge: bool = False) -> GuardVerdict:
    """Evaluate one model-written string.

    `require_judge=True` blocks when no judge can run; use it when a model produced the text.
    """
    reason = check_deterministic(text, allowed_urls=allowed_urls or set(), extra_numbers=extra_numbers)
    if reason:
        return GuardVerdict(False, "deterministic", [reason])
    hits = _similarity.scan(text)
    if hits:
        return GuardVerdict(False, "similarity", sorted({h.category for h in hits}))
    if judge_client is not None and getattr(judge_client, "ready", False):
        allowed, categories, status = judge(judge_client, text, lang)
        if not allowed:
            return GuardVerdict(False, "judge" if status == "judged" else f"judge_{status}", categories, status)
        return GuardVerdict(True, judge="judged")
    if require_judge:
        return GuardVerdict(False, "judge_unavailable", [], "unavailable")
    return GuardVerdict(True)

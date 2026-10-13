"""Offline similarity layer: flags reply sentences that closely restate a prohibited claim.

Character n-grams make this work across English, Hindi, Bengali and Tamil with no model
or download. It catches restatements and close paraphrases; it does not understand
meaning, which is why the judge layer exists. Treat it as a floor, not the ceiling.

Calibration (tests/test_guard.py): 0.65 passes ordinary helpful sentences such as "You might
talk with your care team about your worries" (0.58) while blocking restatements such as
"I have informed your doctor about your symptoms" (0.83). Lower thresholds start blocking
the assistant's own safe advice, so do not tune this down without re-running the benign set.
"""
from __future__ import annotations

import math
import re
from collections import Counter
from dataclasses import dataclass

from sahay.domain.safety.normalize import normalize

from .exemplars import BANK

_SENTENCE_SPLIT = re.compile(r"(?<=[.!?।॥])\s+|\n+")
N = 3


def _grams(text: str) -> Counter[str]:
    padded = f" {normalize(text)} "
    return Counter(padded[i:i + N] for i in range(len(padded) - N + 1))


def _norm(vec: Counter[str]) -> float:
    return math.sqrt(sum(v * v for v in vec.values()))


def _cosine(a: Counter[str], an: float, b: Counter[str], bn: float) -> float:
    if not an or not bn:
        return 0.0
    small, big = (a, b) if len(a) < len(b) else (b, a)
    return sum(v * big.get(k, 0) for k, v in small.items()) / (an * bn)


@dataclass(frozen=True)
class Hit:
    category: str
    score: float


class SimilarityGuard:
    def __init__(self, threshold: float = 0.65):
        self.threshold = threshold
        self._index = [(category, g, _norm(g)) for category, langs in BANK.items()
                       for sentences in langs.values() for sentence in sentences for g in (_grams(sentence),)]

    def scan(self, text: str) -> list[Hit]:
        hits: list[Hit] = []
        for sentence in (s.strip() for s in _SENTENCE_SPLIT.split(text) if s.strip()):
            grams = _grams(sentence)
            norm = _norm(grams)
            best: dict[str, float] = {}
            for category, exemplar, exemplar_norm in self._index:
                score = _cosine(grams, norm, exemplar, exemplar_norm)
                if score > best.get(category, 0.0):
                    best[category] = score
            hits.extend(Hit(c, s) for c, s in best.items() if s >= self.threshold)
        return hits

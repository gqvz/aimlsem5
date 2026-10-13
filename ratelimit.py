"""Small in-memory rate limiter for the single-process demo server.

Sliding window per key. State is lost on restart and is not shared between processes;
a multi-process deployment needs a shared store (see REWRITE_PLAN.md, phase 3).
"""
from __future__ import annotations

import threading
import time
from collections import defaultdict, deque


class RateLimiter:
    def __init__(self, clock=time.monotonic):
        self._hits: dict[str, deque[float]] = defaultdict(deque)
        self._lock = threading.Lock()
        self._clock = clock

    def check(self, key: str, limit: int, window: float) -> float:
        """Record one hit. Returns 0 when allowed, else seconds until a slot frees."""
        now = self._clock()
        with self._lock:
            hits = self._hits[key]
            while hits and now - hits[0] >= window:
                hits.popleft()
            if len(hits) >= limit:
                return max(hits[0] + window - now, 0.001)
            hits.append(now)
            if len(self._hits) > 10_000:  # bound memory under key-spraying
                for stale in [k for k, v in self._hits.items() if not v or now - v[-1] >= window][:5_000]:
                    self._hits.pop(stale, None)
            return 0.0

    def peek(self, key: str, limit: int, window: float) -> float:
        """Like check() but does not record a hit."""
        now = self._clock()
        with self._lock:
            hits = self._hits.get(key)
            if not hits:
                return 0.0
            recent = [h for h in hits if now - h < window]
            if len(recent) < limit:
                return 0.0
            return max(recent[0] + window - now, 0.001)

    def record(self, key: str) -> None:
        with self._lock:
            self._hits[key].append(self._clock())

    def reset(self, key: str) -> None:
        with self._lock:
            self._hits.pop(key, None)


# (limit, window seconds). Generous for a person, hostile to a script.
LIMITS = {
    "message": (30, 60.0),
    "model_test": (5, 60.0),
    "speech": (20, 60.0),
    "login_failures": (8, 900.0),
    "register": (10, 3600.0),
}

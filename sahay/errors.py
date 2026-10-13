"""One error type for the whole API. The JSON envelope is `{"error": "<message>"}`."""
from __future__ import annotations


class ApiError(Exception):
    def __init__(self, status: int, message: str, retry_after: int | None = None):
        self.status = status
        self.message = message
        self.retry_after = retry_after
        super().__init__(message)

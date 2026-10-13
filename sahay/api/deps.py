"""Request-scoped dependencies: auth, rate limits, JSON bodies."""
from __future__ import annotations

import json
from collections.abc import Callable
from typing import Any

from fastapi import Depends, Header, Request
from sqlalchemy.orm import Session

from ..config import Settings
from ..db import get_session
from ..errors import ApiError
from ..models import User
from ..ratelimit import LIMITS, RateLimiter
from ..services import accounts

LIMITER = RateLimiter()


def client_ip(request: Request) -> str:
    if Settings.load().trust_proxy:
        forwarded = request.headers.get("x-forwarded-for", "").split(",")[0].strip()
        if forwarded:
            return forwarded
    return request.client.host if request.client else "unknown"


def throttle(bucket: str) -> Callable[..., None]:
    limit, window = LIMITS[bucket]

    def dependency(request: Request) -> None:
        wait = LIMITER.check(f"{bucket}:{client_ip(request)}", limit, window)
        if wait:
            raise ApiError(429, "Too many requests. Please wait a moment and try again.", int(wait) + 1)
    return dependency


async def json_body(request: Request) -> dict[str, Any]:
    try:
        data = json.loads(await request.body())
    except (ValueError, UnicodeDecodeError) as error:
        raise ApiError(400, str(error)[:160]) from None
    if not isinstance(data, dict):
        raise ApiError(400, "Expected a JSON object")
    return data


def current_user(*roles: str) -> Callable[..., User]:
    """Dependency factory. `Depends(current_user("clinician"))` is the only way to reach a route."""
    def dependency(authorization: str = Header(default=""), db: Session = Depends(get_session, scope="function")) -> User:
        return accounts.authenticate(db, authorization, roles)
    return dependency

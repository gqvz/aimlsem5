"""Application factory."""
from __future__ import annotations

import json
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from starlette.types import ASGIApp, Receive, Scope, Send

from ..config import Settings
from ..db import init_db
from ..errors import ApiError
from .routes import router

SMALL_BODY = 16_000
LARGE_BODY = 15 * 1024 * 1024
LARGE_PREFIXES = ("/api/care/", "/api/speech/")
SERVER_ERROR = "The assistant is unavailable. Use the Help panel for urgent support."


class ApiGuards:
    """Bound request size up front and keep API responses out of caches."""

    def __init__(self, app: ASGIApp):
        self.app = app

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if scope["type"] != "http" or not scope["path"].startswith("/api/"):
            return await self.app(scope, receive, send)
        if scope["method"] == "POST":
            headers = dict(scope["headers"])
            try:
                length = int(headers.get(b"content-length", b"0"))
            except ValueError:
                length = 0
            limit = LARGE_BODY if scope["path"].startswith(LARGE_PREFIXES) else SMALL_BODY
            if length < 1 or length > limit:
                response = JSONResponse({"error": "Message is too large or empty"}, status_code=413)
                return await response(scope, receive, send)

        async def send_with_headers(message):
            if message["type"] == "http.response.start":
                headers = message.setdefault("headers", [])
                headers.append((b"cache-control", b"no-store"))
                headers.append((b"x-content-type-options", b"nosniff"))
            await send(message)
        await self.app(scope, receive, send_with_headers)


def create_app() -> FastAPI:
    @asynccontextmanager
    async def lifespan(_: FastAPI) -> AsyncIterator[None]:
        init_db()
        yield

    app = FastAPI(title="Sahay API", version="2.0", lifespan=lifespan, docs_url=None, redoc_url=None)
    app.add_middleware(ApiGuards)

    @app.exception_handler(ApiError)
    async def api_error(_: Request, error: ApiError) -> JSONResponse:
        body: dict = {"error": error.message}
        headers = {}
        if error.retry_after is not None:
            body["retry_after"] = error.retry_after
            headers["Retry-After"] = str(error.retry_after)
        return JSONResponse(body, status_code=error.status, headers=headers)

    @app.exception_handler(RequestValidationError)
    async def validation_error(_: Request, error: RequestValidationError) -> JSONResponse:
        return JSONResponse({"error": "Invalid request"}, status_code=400)

    @app.exception_handler(ValueError)
    async def value_error(_: Request, error: ValueError) -> JSONResponse:
        return JSONResponse({"error": str(error)[:160]}, status_code=400)

    @app.exception_handler(json.JSONDecodeError)
    async def json_error(_: Request, error: json.JSONDecodeError) -> JSONResponse:
        return JSONResponse({"error": str(error)[:160]}, status_code=400)

    @app.exception_handler(Exception)
    async def unexpected(_: Request, error: Exception) -> JSONResponse:
        return JSONResponse({"error": SERVER_ERROR}, status_code=500)

    app.include_router(router)

    @app.api_route("/api/{rest:path}", methods=["GET", "POST", "PUT", "PATCH", "DELETE"], include_in_schema=False)
    async def not_found(rest: str) -> JSONResponse:
        return JSONResponse({"error": "Not found"}, status_code=404)

    web = Settings.load().web_root
    if web.is_dir():
        app.mount("/", StaticFiles(directory=web, html=True), name="web")
    return app


app = create_app()

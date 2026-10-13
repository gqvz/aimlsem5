"""Local web server for the Sahay prototype. Run: python3 server.py"""

from __future__ import annotations

import json
import os
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit

from bookkeeping import ApiError, handle as handle_bookkeeping, init_db
from care import handle as handle_care, init_care, attachment
from speech import status as speech_status, transcribe, synthesize
from engine import LANGS, ModelClient, SOURCES, respond, test_model_connection, public_model_configuration


WEB_ROOT = Path(__file__).with_name("web")


class SahayHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(WEB_ROOT), **kwargs)

    def log_message(self, format: str, *args: object) -> None:
        # Never log paths or message bodies from a health-related conversation.
        pass

    def _json(self, status: int, data: dict) -> None:
        encoded = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.end_headers()
        self.wfile.write(encoded)

    def _binary(self, payload: bytes, mime: str):
        self.send_response(200)
        self.send_header("Content-Type", mime)
        self.send_header("Content-Length", str(len(payload)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.end_headers()
        self.wfile.write(payload)

    def do_GET(self) -> None:
        path = urlsplit(self.path).path
        try:
            if path.startswith("/api/care/files/"):
                payload, mime, name = attachment(path.removeprefix("/api/care/files/"), self.headers.get("Authorization", ""))
                self._binary(payload, mime)
                return
            result = handle_care("GET", path, {}, self.headers.get("Authorization", ""))
            if result is not None:
                self._json(*result)
                return
            result = handle_bookkeeping("GET", path, {}, self.headers.get("Authorization", ""))
            if result is not None:
                self._json(*result)
                return
        except ApiError as error:
            self._json(error.status, {"error": error.message})
            return
        if path == "/api/speech/status":
            self._json(200, speech_status())
            return
        if path == "/api/health":
            client = ModelClient()
            self._json(200, {"ok": True, "mode": "live" if client.ready else "demo", "model_config": public_model_configuration(), "languages": LANGS, "agents": ["Supervisor", "Language", "Safety", "Emotional support", "Knowledge", "Activity", "Visit preparation", "Review"], "speech": speech_status()})
            return
        if path == "/api/cards":
            self._json(200, {"cards": [{"id": key, **value} for key, value in SOURCES.items()]})
            return
        if path.startswith("/api/"):
            self._json(404, {"error": "Not found"})
            return
        if path == "/":
            self.path = "/index.html"
        super().do_GET()

    def do_POST(self) -> None:
        path = urlsplit(self.path).path
        if path not in ("/api/message", "/api/model/test") and not path.startswith(("/api/care/", "/api/speech/", "/api/me/", "/api/auth/", "/api/shares", "/api/clinician/", "/api/admin/")):
            self._json(404, {"error": "Not found"})
            return
        try:
            length = int(self.headers.get("Content-Length", "0"))
            limit = 15 * 1024 * 1024 if path.startswith(("/api/care/", "/api/speech/")) else 16000
            if length < 1 or length > limit:
                self._json(413, {"error": "Message is too large or empty"})
                return
            data = json.loads(self.rfile.read(length))
            if not isinstance(data, dict):
                raise ValueError("Expected a JSON object")
            if path == "/api/model/test":
                self._json(200, test_model_connection(data))
                return
            if path == "/api/speech/transcribe":
                self._json(200, transcribe(data))
                return
            if path == "/api/speech/synthesize":
                self._binary(synthesize(data), "audio/mpeg")
                return
            result = handle_care("POST", path, data, self.headers.get("Authorization", ""))
            if result is not None:
                self._json(*result)
                return
            result = handle_bookkeeping("POST", path, data, self.headers.get("Authorization", ""))
            if result is not None:
                self._json(*result)
                return
            result = respond(data)
            self._json(200, result)
        except ApiError as error:
            self._json(error.status, {"error": error.message})
        except (ValueError, json.JSONDecodeError) as error:
            self._json(400, {"error": str(error)[:160]})
        except Exception:
            self._json(500, {"error": "The assistant is unavailable. Use the Help panel for urgent support."})

    def do_DELETE(self) -> None:
        try:
            result = handle_bookkeeping("DELETE", urlsplit(self.path).path, {}, self.headers.get("Authorization", ""))
            if result is None:
                raise ApiError(404, "Not found")
            self._json(*result)
        except ApiError as error:
            self._json(error.status, {"error": error.message})


if __name__ == "__main__":
    init_db()
    init_care()
    host = os.getenv("SAHAY_HOST", "127.0.0.1")
    port = int(os.getenv("SAHAY_PORT", "8000"))
    print(f"Sahay is available at http://{host}:{port}")
    print("Mode:", "live model" if ModelClient().ready else "local demo")
    ThreadingHTTPServer((host, port), SahayHandler).serve_forever()

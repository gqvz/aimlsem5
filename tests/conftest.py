import tempfile
import threading
from http.server import ThreadingHTTPServer
from pathlib import Path

import pytest

import bookkeeping
import server


class Api:
    """Thin client for a real SahayHandler running on an ephemeral port."""

    def __init__(self, base):
        self.base = base

    def call(self, method, path, data=None, token="", headers=None, raw=False):
        import json
        import urllib.error
        import urllib.request
        body = None if data is None else json.dumps(data).encode()
        request = urllib.request.Request(self.base + path, body, method=method)
        if body is not None:
            request.add_header("Content-Type", "application/json")
        if token:
            request.add_header("Authorization", f"Bearer {token}")
        for key, value in (headers or {}).items():
            request.add_header(key, value)
        try:
            with urllib.request.urlopen(request, timeout=15) as response:
                payload = response.read()
                return response.status, payload if raw else (json.loads(payload) if payload else {})
        except urllib.error.HTTPError as error:
            payload = error.read()
            try:
                return error.code, json.loads(payload)
            except ValueError:
                return error.code, payload


@pytest.fixture()
def api(monkeypatch):
    with tempfile.TemporaryDirectory() as temp:
        monkeypatch.setattr(bookkeeping, "DB_PATH", Path(temp) / "test.sqlite3")
        for name in ("LLM_API_URL", "LLM_API_KEY", "LLM_MODEL", "STT_API_URL", "TTS_API_URL", "SAHAY_MODEL_ALLOWLIST"):
            monkeypatch.delenv(name, raising=False)
        server.LIMITER._hits.clear()
        bookkeeping.init_db()
        import care
        care.init_care()
        httpd = ThreadingHTTPServer(("127.0.0.1", 0), server.SahayHandler)
        thread = threading.Thread(target=httpd.serve_forever, daemon=True)
        thread.start()
        try:
            yield Api(f"http://127.0.0.1:{httpd.server_address[1]}")
        finally:
            httpd.shutdown()
            httpd.server_close()

import tempfile
import threading
import time
from pathlib import Path

import pytest
import uvicorn

from sahay import db as sahay_db
from sahay.api import deps
from sahay.api.app import create_app


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
    """A real server on an ephemeral port, backed by a throwaway database."""
    with tempfile.TemporaryDirectory() as temp:
        monkeypatch.setenv("SAHAY_DB_PATH", str(Path(temp) / "test.sqlite3"))
        for name in ("LLM_API_URL", "LLM_API_KEY", "LLM_MODEL", "STT_API_URL", "TTS_API_URL",
                     "SAHAY_MODEL_ALLOWLIST", "SAHAY_TRUST_PROXY", "SAHAY_GUARD_LLM_API_URL", "SAHAY_GUARD_LLM_MODEL"):
            monkeypatch.delenv(name, raising=False)
        deps.LIMITER._hits.clear()
        server = uvicorn.Server(uvicorn.Config(create_app(), host="127.0.0.1", port=0, log_level="warning", lifespan="on"))
        thread = threading.Thread(target=server.run, daemon=True)
        thread.start()
        deadline = time.time() + 10
        while not server.started and time.time() < deadline:
            time.sleep(0.02)
        assert server.started, "server did not start"
        port = server.servers[0].sockets[0].getsockname()[1]
        try:
            yield Api(f"http://127.0.0.1:{port}")
        finally:
            server.should_exit = True
            thread.join(timeout=5)
            sahay_db.dispose_all()

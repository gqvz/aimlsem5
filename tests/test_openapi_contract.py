"""The checked-in OpenAPI document must match the app. Regenerate with:
    python -m sahay.cli openapi > contract/openapi.json
A diff here is a public API change: review it deliberately."""
import json
from pathlib import Path

from sahay.api.app import create_app


def test_openapi_snapshot_is_current():
    expected = json.loads((Path(__file__).parent.parent / "contract" / "openapi.json").read_text(encoding="utf-8"))
    assert create_app().openapi() == expected


def test_every_route_is_documented_with_its_method():
    paths = create_app().openapi()["paths"]
    must_have = {("/api/message", "post"), ("/api/auth/login", "post"), ("/api/care/threads", "get"),
                 ("/api/shares/mine", "get"), ("/api/admin/audit", "get"), ("/api/health", "get")}
    assert all(m in paths.get(p, {}) for p, m in must_have)

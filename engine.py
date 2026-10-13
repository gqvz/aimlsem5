"""Compatibility surface. New code imports from `sahay.*`; this module only re-exports."""
from __future__ import annotations

from typing import Any

from sahay.domain.content import ACTIVITIES, FALLBACK, LANGS, SOURCE_SUMMARIES, SOURCES, choose_card  # noqa: F401
from sahay.domain.pipeline import run as _run
from sahay.domain.routing import choose_action  # noqa: F401
from sahay.domain.safety import (  # noqa: F401
    CRISIS_PATTERNS,
    DEPENDENCY_PATTERNS,
    MEDICAL_PATTERNS,
    MEDICAL_REQUEST_PATTERNS,
    RISK_ORDER,
    assess,
    more_cautious,
)
from sahay.providers.llm import (  # noqa: F401
    MODEL_OPENER,
    ModelClient,
    ModelError,
    allowed_model_hosts,
    json_object,
    model_for_request,
    public_model_configuration,
    test_model_connection,
)


def rule_risk(text: str) -> str:
    return assess(text).risk


def respond(data: dict[str, Any], client: ModelClient | None = None) -> dict[str, Any]:
    return _run(data, client if client is not None else model_for_request(data))

"""Which model reviews replies. An independent reviewer is stronger than the author model."""
from __future__ import annotations

import os

from sahay.providers.llm import ModelClient


def judge_client(default: ModelClient) -> tuple[ModelClient, str]:
    """Return (client, independence). `separate` when SAHAY_GUARD_LLM_* is configured."""
    url, model = os.getenv("SAHAY_GUARD_LLM_API_URL", ""), os.getenv("SAHAY_GUARD_LLM_MODEL", "")
    if url and model:
        return ModelClient(url=url, model=model, key=os.getenv("SAHAY_GUARD_LLM_API_KEY", "")), "separate"
    return default, "same_model"

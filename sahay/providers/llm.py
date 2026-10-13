"""OpenAI-compatible chat client with the operator's endpoint allow-list.

Request bodies and provider error bodies are never logged or echoed: they may contain
patient text or keys.
"""
from __future__ import annotations

import json
import os
import re
import urllib.error
import urllib.request
from dataclasses import dataclass, field
from typing import Any
from urllib.parse import urlsplit


class ModelError(RuntimeError):
    """A provider failure that can be displayed without leaking request content."""


class _NoModelRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        raise ModelError("The model endpoint redirected. Enter the final endpoint URL.")


MODEL_OPENER = urllib.request.build_opener(_NoModelRedirect())


@dataclass
class ModelClient:
    url: str = field(default_factory=lambda: os.getenv("LLM_API_URL", ""))
    key: str = field(default_factory=lambda: os.getenv("LLM_API_KEY", ""))
    model: str = field(default_factory=lambda: os.getenv("LLM_MODEL", ""))
    timeout: float | None = None  # seconds for one call; falls back to LLM_TIMEOUT

    @property
    def ready(self) -> bool:
        return bool(self.url and self.model)

    def chat(self, system: str, user: str, max_tokens: int = 450) -> str:
        if not self.ready:
            raise RuntimeError("No live model configured")
        payload = json.dumps({
            "model": self.model,
            "temperature": 0.2,
            "max_tokens": max_tokens,
            "messages": [{"role": "system", "content": system}, {"role": "user", "content": user}],
        }).encode("utf-8")
        headers = {"Content-Type": "application/json", "User-Agent": "Sahay-Course/1.0"}
        if self.key:
            headers["Authorization"] = f"Bearer {self.key}"
        req = urllib.request.Request(self.url, payload, headers, method="POST")
        try:
            with MODEL_OPENER.open(req, timeout=self.timeout or float(os.getenv("LLM_TIMEOUT", "60"))) as response:
                data = json.load(response)
            content = data["choices"][0]["message"]["content"]
        except urllib.error.HTTPError as error:
            # Never display an arbitrary provider body: it can echo prompts or keys.
            detail = ""
            try:
                body = json.loads(error.read(4096))
                if isinstance(body.get("error"), dict) and body["error"].get("type") == "FreeTierError":
                    detail = " OpenCode's free tier is restricted to use within OpenCode."
            except (ValueError, AttributeError):
                pass
            finally:
                error.close()
            raise ModelError(f"Model provider returned HTTP {error.code}.{detail} Check provider access or the backend API key.") from None
        except (OSError, ValueError, KeyError, IndexError, TypeError):
            raise ModelError("The model could not be reached or returned an invalid reply. Check the endpoint and model name.") from None
        if not isinstance(content, str) or not content.strip():
            raise ModelError("The model returned an empty reply. Check the model name or try again.")
        return content.strip()


def allowed_model_hosts() -> set[str]:
    """Hosts a request may select. Defaults to the server's own configured endpoint only.

    Letting callers aim the server at arbitrary URLs is server-side request forgery, so
    operators opt in with SAHAY_MODEL_ALLOWLIST=host[:port],host[:port].
    """
    hosts = {h.strip().lower() for h in os.getenv("SAHAY_MODEL_ALLOWLIST", "").split(",") if h.strip()}
    try:
        configured = urlsplit(os.getenv("LLM_API_URL", ""))
        if configured.hostname:
            hosts.add((configured.netloc.rsplit("@", 1)[-1]).lower())
    except ValueError:
        pass
    return hosts


def model_for_request(data: dict[str, Any]) -> ModelClient:
    """A device may select a model endpoint only from the operator's allow-list."""
    default = ModelClient()
    config = data.get("model_config")
    if config is None:
        return default
    if not isinstance(config, dict):
        raise ValueError("Model configuration must contain an endpoint URL and model name")
    url, model = config.get("url"), config.get("model")
    if not isinstance(url, str) or not isinstance(model, str):
        raise ValueError("Enter both the model endpoint URL and model name")
    url, model = url.strip(), model.strip()
    try:
        parsed = urlsplit(url)
        valid = parsed.scheme in ("http", "https") and bool(parsed.hostname) and parsed.port != 0
    except ValueError:
        valid = False
    if not valid or parsed.username or parsed.password or parsed.query or parsed.fragment or len(url) > 2048 or any(c.isspace() for c in url):
        raise ValueError("Use a complete http:// or https:// endpoint without embedded credentials, query or fragment")
    if not model or len(model) > 200 or any(ord(c) < 32 for c in model):
        raise ValueError("Enter a model name of at most 200 characters")
    if parsed.netloc.lower() not in allowed_model_hosts():
        raise ValueError("This model endpoint is not allowed by the server operator")
    # A URL change must never forward the configured provider's secret elsewhere.
    key = default.key if url.rstrip("/") == default.url.rstrip("/") else ""
    return ModelClient(url=url, model=model, key=key)


def test_model_connection(data: dict[str, Any]) -> dict[str, Any]:
    client = model_for_request(data)
    if not client.ready:
        return {"ok": False, "error": "No model endpoint and model name are configured."}
    try:
        client.chat("This is a connectivity test. Reply with just ready.", "Connection test.", 32)
        return {"ok": True, "model": client.model}
    except ModelError as error:
        return {"ok": False, "model": client.model, "error": str(error)}


def public_model_configuration() -> dict[str, str]:
    client = ModelClient()
    try:
        parsed = urlsplit(client.url)
        # Configuration endpoints never reveal embedded credentials or query keys.
        url = parsed._replace(netloc=parsed.netloc.rsplit("@", 1)[-1], query="", fragment="").geturl()
    except ValueError:
        url = ""
    return {"url": url, "model": client.model}


def json_object(value: str) -> dict[str, Any]:
    match = re.search(r"\{.*\}", value, re.DOTALL)
    if not match:
        raise ValueError("Model did not return JSON")
    result = json.loads(match.group())
    if not isinstance(result, dict):
        raise ValueError("Model did not return an object")
    return result

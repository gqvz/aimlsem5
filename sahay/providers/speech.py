"""Server-side adapters for OpenAI-compatible transcription and speech APIs."""
from __future__ import annotations

import base64
import json
import os
import secrets
import urllib.request
from typing import Any

from ..errors import ApiError

LANGS = ("en", "hi", "bn", "ta")


def status() -> dict[str, Any]:
    return {"stt": bool(os.getenv("STT_API_URL")), "tts": bool(os.getenv("TTS_API_URL")),
            "format": "m4a input / mp3 output", "languages": list(LANGS)}


def _headers(prefix: str) -> dict[str, str]:
    key = os.getenv(prefix + "_API_KEY", "")
    return {"Authorization": "Bearer " + key} if key else {}


def transcribe(data: dict[str, Any]) -> dict[str, Any]:
    url = os.getenv("STT_API_URL", "")
    if not url:
        raise ApiError(503, "Speech API is not configured. Use device speech or type your message.")
    lang = data.get("language", "en")
    if lang not in LANGS:
        raise ApiError(400, "Invalid speech language")
    try:
        audio = base64.b64decode(data.get("base64", ""), validate=True)
    except (ValueError, TypeError):
        raise ApiError(400, "Invalid audio") from None
    if not audio or len(audio) > 8 * 1024 * 1024:
        raise ApiError(413, "Audio must be under 8 MB")
    boundary = "sahay-" + secrets.token_hex(12)
    parts = [f'--{boundary}\r\nContent-Disposition: form-data; name="{k}"\r\n\r\n{v}\r\n'.encode()
             for k, v in {"model": os.getenv("STT_MODEL", "whisper-1"), "language": lang, "response_format": "json"}.items()]
    parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="recording.m4a"\r\n'
                 f"Content-Type: audio/mp4\r\n\r\n".encode() + audio + b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    headers = {**_headers("STT"), "Content-Type": "multipart/form-data; boundary=" + boundary}
    try:
        with urllib.request.urlopen(urllib.request.Request(url, b"".join(parts), headers), timeout=45) as response:
            result = json.load(response)
        text = str(result.get("text", "")).strip()[:4000]
        if not text:
            raise ValueError("empty transcript")
        return {"text": text, "language": lang}
    except Exception:  # noqa: BLE001
        raise ApiError(502, "Transcription failed. Retry or use text input.") from None


def synthesize(data: dict[str, Any]) -> bytes:
    url = os.getenv("TTS_API_URL", "")
    if not url:
        raise ApiError(503, "Speech playback API is not configured. Use a device voice.")
    text = str(data.get("text", "")).strip()
    if not text or len(text) > 4000:
        raise ApiError(400, "Invalid spoken text")
    payload = json.dumps({"model": os.getenv("TTS_MODEL", "tts-1"), "voice": os.getenv("TTS_VOICE", "alloy"),
                          "input": text, "response_format": "mp3"}).encode()
    try:
        request = urllib.request.Request(url, payload, {**_headers("TTS"), "Content-Type": "application/json"})
        with urllib.request.urlopen(request, timeout=45) as response:
            audio = response.read(8 * 1024 * 1024 + 1)
        if not audio or len(audio) > 8 * 1024 * 1024:
            raise ValueError("invalid audio")
        return audio
    except Exception:  # noqa: BLE001
        raise ApiError(502, "Speech playback failed. The text remains available.") from None

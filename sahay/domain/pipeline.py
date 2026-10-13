"""One conversational turn: understand -> safety -> route -> draft -> localise -> guard.

Model calls per ordinary live turn: understand+route (1), draft (1), translate (1 when the
reply language is not English), judge (1). Every call shares one deadline, so a slow
provider degrades to curated copy instead of hanging a distressed user.

The model may only *raise* risk and only choose among fixed tools. Nothing it writes
reaches the user unless the guard approves it.
"""
from __future__ import annotations

import dataclasses
import json
import os
import time
from dataclasses import dataclass, field
from typing import Any

from sahay.domain import guard as output_guard
from sahay.domain import prompts
from sahay.domain.content import (
    ACTIVITIES,
    ACTIVITY_INTRO,
    BRIEF_INTRO,
    FALLBACK,
    LANGS,
    SOURCES,
    choose_card,
    fallback_response,
)
from sahay.domain.guard.config import judge_client
from sahay.domain.routing import choose_action
from sahay.domain.safety import RISK_ORDER, assess, more_cautious
from sahay.providers.llm import ModelClient, ModelError, json_object

ORCHESTRATION = "sahay-agent-graph-v1"
RISKS = ("ordinary", "concerning", "urgent_medical", "crisis")
TOOLS = ("reflect", "coping", "info", "questions", "activities", "brief")


class BoundedModel:
    """Wraps a model client so every call respects the turn's remaining time."""

    def __init__(self, client: Any, seconds: float):
        self._client = client
        self._expires = time.monotonic() + seconds

    @property
    def ready(self) -> bool:
        return bool(self._client.ready)

    def remaining(self) -> float:
        return self._expires - time.monotonic()

    def chat(self, system: str, user: str, max_tokens: int = 450) -> str:
        left = self.remaining()
        if left <= 0.5:
            raise ModelError("The assistant took too long to respond.")
        client = self._client
        if isinstance(client, ModelClient) and type(client).chat is ModelClient.chat:
            default = float(os.getenv("LLM_TIMEOUT", "60"))
            client = dataclasses.replace(client, timeout=min(left, client.timeout or default))
        return client.chat(system, user, max_tokens)


@dataclass
class Ctx:
    data: dict[str, Any]
    model: BoundedModel
    original: str
    lang: str
    english: str = ""
    risk: str = "ordinary"
    action: str = "reflect"
    model_action: str | None = None
    issue: bool = False
    trace: list[dict] = field(default_factory=list)
    extra: dict = field(default_factory=dict)

    def step(self, agent: str, tool: str, outcome: str, mode: str = "fixed") -> None:
        self.trace.append({"agent": agent, "tool": tool, "outcome": outcome, "mode": mode})

    def provider_failed(self, error: Exception) -> None:
        if isinstance(error, ModelError):
            self.extra["provider_error"] = str(error)
        self.issue = True


# --- stages ------------------------------------------------------------------------------

def understand(c: Ctx) -> None:
    """Translate to English, classify risk and suggest a tool, in one call."""
    c.english = c.original
    if c.model.ready:
        try:
            requested = c.data.get("action")
            result = json_object(c.model.chat(
                prompts.UNDERSTAND,
                f"Language: {LANGS[c.lang]}\nRequested action: {requested}\nText: {c.original}", 320))
            c.english = str(result.get("english") or c.original)[:4000]
            risk = result.get("risk", "uncertain")
            c.risk = risk if risk in RISKS else "concerning"
            action = result.get("action")
            c.model_action = action if action in TOOLS else None
            c.step("Language", "translate_and_classify", c.risk, "llm")
            return
        except Exception as error:  # noqa: BLE001 - any provider failure degrades to local rules
            c.provider_failed(error)
            c.risk = "concerning"
    c.step("Language", "original_text", "provider_unavailable" if c.issue else "local", "fallback")


def safety_gate(c: Ctx) -> str | None:
    """Return fixed copy when the turn must not reach a model-written reply."""
    original, english = assess(c.original), assess(c.english)
    c.risk = more_cautious(original.risk, english.risk, c.risk)
    c.step("Safety", "risk_gate", c.risk)
    if c.risk in ("crisis", "urgent_medical"):
        c.action = "help"
        return FALLBACK[c.lang]["crisis" if c.risk == "crisis" else "medical"]
    if original.medical_request or english.medical_request:
        c.action = "medical_boundary"
        return FALLBACK[c.lang]["medical_boundary"]
    if c.risk == "concerning":
        if original.dependency or english.dependency:
            c.action = "human_connection"
            return FALLBACK[c.lang]["connection"]
        c.action = "clarify"
        return FALLBACK[c.lang]["uncertain"]
    return None


def route(c: Ctx) -> None:
    requested = c.data.get("action")
    c.action = requested if requested in ("activities", "brief") else choose_action(c.english, requested)
    if requested is None and any(x in c.english.lower() for x in ("play a game", "distraction", "activity plan", "something to do")):
        c.action = "activities"
    if c.model_action and requested is None:
        c.action = c.model_action
        c.step("Supervisor", "delegate", c.action, "llm")
    else:
        c.step("Supervisor", "delegate", c.action, "llm" if c.model_action else "fallback")


def activity_plan(c: Ctx) -> tuple[str, str]:
    energy = c.data.get("energy")
    ids = ["sound", "peers"] if energy == "low" else ["grounding", "art", "memory"]
    mode = "demo"
    if c.model.ready and not c.issue:
        try:
            result = json_object(c.model.chat(
                prompts.ACTIVITY,
                json.dumps({"message": c.english, "energy": energy or "medium", "catalogue": ACTIVITIES}, ensure_ascii=False), 160))
            proposed = result.get("activities", [])
            if isinstance(proposed, list) and proposed and all(isinstance(x, str) and x in ACTIVITIES for x in proposed):
                allowed = list(dict.fromkeys(proposed))
                if energy == "low":
                    allowed = [x for x in allowed if ACTIVITIES[x]["energy"] == "low"]
                if any(x in c.english.lower() for x in ("no breathing", "avoid breathing", "don't want breathing", "do not want breathing")):
                    allowed = [x for x in allowed if x != "breathing"]
                if allowed:
                    ids, mode = allowed[:3], "live"
        except Exception as error:  # noqa: BLE001
            if isinstance(error, ModelError):
                c.extra["provider_error"] = str(error)
    c.extra["activity_plan"] = [{"id": x, **ACTIVITIES[x]} for x in ids]
    c.extra["suggestions"] = [{"screen": "activity", "id": x, "label": ACTIVITIES[x]["title"]} for x in ids]
    c.step("Activity", "catalogue_plan", ",".join(ids), "llm" if mode == "live" else "fallback")
    return ACTIVITY_INTRO[c.lang], mode


def _history(c: Ctx, n: int) -> str:
    return "\n".join(str(x)[:500] for x in c.data.get("history", [])[-n:] if isinstance(x, str))


def draft(c: Ctx, card: dict | None) -> str:
    if c.action in ("questions", "brief"):
        c.step("Visit preparation", "editable_draft", c.action, "llm")
        if c.action == "brief":
            return c.model.chat(prompts.BRIEF, f"Context: {_history(c, 6)}\nPatient request: {c.english}", 300)
        return c.model.chat(prompts.QUESTIONS, f"Context: {_history(c, 6)}\nPatient request: {c.english}", 160)
    if card:
        c.step("Knowledge", "curated_lookup", card["title"])
        c.step("Knowledge", "draft_reply", c.action, "llm")
        return c.model.chat(prompts.KNOWLEDGE + f"Source: {card['summary']} Include {card['url']}", f"User: {c.english}", 350)
    c.step("Emotional support", "draft_reply", c.action, "llm")
    return c.model.chat(prompts.SUPPORT, f"Context: {_history(c, 4)}\nUser: {c.english}", 200)


def localise(c: Ctx, text: str) -> str:
    if c.lang == "en":
        return text
    out = c.model.chat(prompts.TRANSLATE.format(language=LANGS[c.lang]), text, 450)
    c.step("Language", "translate_reply", c.lang, "llm")
    return out


def verify(c: Ctx, text: str, english_draft: str, card: dict | None) -> bool:
    """Independent semantic guard. Also records the faithfulness verdict as the Review step."""
    judge, independence = judge_client(c.model)
    verdict = output_guard.check(
        text, lang=c.lang, action=c.action, allowed_urls={card["url"]} if card else set(),
        judge_client=judge, require_judge=True, require_card_url=bool(card and c.action == "info"),
        draft=english_draft if c.lang != "en" else None)
    if english_draft != text:  # the English draft must also be clean before it was translated
        draft_verdict = output_guard.check(english_draft, lang="en", action=c.action,
                                           allowed_urls={card["url"]} if card else set())
        if not draft_verdict.allowed:
            verdict = draft_verdict
    if not verdict.allowed:
        c.step("Guard", "semantic_check", "blocked:" + (verdict.layer or "unknown"), "llm" if verdict.judge == "judged" else "fixed")
        return False
    c.step("Guard", "semantic_check", "approved:" + independence, "llm")
    c.step("Review", "approve_or_veto", "approved", "llm")
    return True


# --- entry point -------------------------------------------------------------------------

def result(c: Ctx, text: str, mode: str, card: dict | None = None) -> dict[str, Any]:
    return {"route": c.risk, "action": c.action, "text": text, "card": card, "mode": mode, "translation": c.english,
            "agent_trace": c.trace, "orchestration": ORCHESTRATION, **c.extra}


def run(data: dict[str, Any], client: Any) -> dict[str, Any]:
    original = str(data.get("text", "")).strip()[:4000]
    if not original:
        raise ValueError("Message is empty")
    lang = str(data.get("language", "en"))
    lang = lang if lang in LANGS else "en"
    c = Ctx(data, BoundedModel(client, float(os.getenv("SAHAY_TURN_DEADLINE", "45"))), original, lang)

    # Explicit urgent input is answered by the local gate before any slow model call.
    local = assess(original).risk
    if local in ("crisis", "urgent_medical"):
        c.english, c.risk = original, local
        return result(c, safety_gate(c) or "", "fixed", SOURCES["infection"] if local == "urgent_medical" else None)

    understand(c)
    fixed = safety_gate(c)
    if fixed is not None:
        return result(c, fixed, "fixed", SOURCES["infection"] if c.risk == "urgent_medical" else None)
    route(c)

    card_key, card = choose_card(c.english) if c.action == "info" else (None, None)
    if c.action == "activities":
        text, mode = activity_plan(c)
        return result(c, text, mode)
    if c.action == "coping":
        c.step("Activity", "grounding_script", "grounding")
        c.extra["suggestions"] = [{"screen": "activity", "id": "grounding", "label": ACTIVITIES["grounding"]["title"]}]
        return result(c, FALLBACK[lang]["coping"], "fixed")
    if c.action == "info" and not card:
        c.step("Knowledge", "curated_lookup", "no_source")
        return result(c, FALLBACK[lang]["info_unavailable"], "fixed")

    fallback = BRIEF_INTRO[lang] if c.action == "brief" else fallback_response(lang, c.action, card, card_key)
    if card:
        fallback += "\n\n" + card["title"] + " — " + card["url"]
    output, mode = fallback, "demo"
    if c.model.ready and not c.issue:
        try:
            english_draft = draft(c, card)
            reply = localise(c, english_draft)
            if card and card["url"] not in reply:
                reply += "\n\n" + card["url"]
            if verify(c, reply, english_draft, card):
                output, mode = reply, "live"
            else:
                mode = "fallback"
        except Exception as error:  # noqa: BLE001 - provider or deadline failure
            if isinstance(error, ModelError):
                c.extra["provider_error"] = str(error)
            mode = "fallback"
            c.step("Review", "provider_failure", "fixed_reply", "fallback")
    else:
        c.step("Visit preparation" if c.action in ("questions", "brief") else "Emotional support", "local_reply", c.action, "fallback")
    if c.action == "brief":
        c.extra["visit_brief"] = output
        c.extra["suggestions"] = [{"screen": "share", "label": "Review visit brief"}]
    if c.action == "questions":
        c.extra["suggestions"] = [{"screen": "questions", "label": "Save questions for your visit"}]
    return result(c, output, mode, {"id": card_key, **card} if card else None)


__all__ = ["ORCHESTRATION", "RISK_ORDER", "BoundedModel", "run"]

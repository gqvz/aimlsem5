"""Pipeline behaviour: call budget, deadline, and that a model can raise risk but never lower it."""
import json
import time

from sahay.domain.pipeline import run
from sahay.providers.llm import ModelClient


class Scripted(ModelClient):
    """A model whose answers are keyed by which stage is asking."""

    def __init__(self, understand=None, draft="That sounds hard. What feels heaviest tonight?", judge="allow", delay=0.0):
        super().__init__(url="http://model.test", model="m", key="")
        self.understand, self.draft, self.judge_verdict, self.delay = understand, draft, judge, delay
        self.stages = []

    def chat(self, system, user, max_tokens=450):
        time.sleep(self.delay)
        if "classify risk" in system:
            self.stages.append("understand")
            return json.dumps(self.understand or {"english": "I am worried tonight", "risk": "ordinary", "action": "reflect"})
        if "safety reviewer" in system:
            self.stages.append("judge")
            return json.dumps({"verdict": self.judge_verdict, "categories": []})
        if "Translate faithfully" in system:
            self.stages.append("translate")
            return "अनुवादित उत्तर: यह कठिन लग रहा है। आज रात सबसे भारी क्या लग रहा है?"
        self.stages.append("draft")
        return self.draft


def test_english_turn_uses_three_calls():
    model = Scripted()
    result = run({"text": "I am worried tonight", "language": "en"}, model)
    assert model.stages == ["understand", "draft", "judge"]
    assert result["mode"] == "live" and result["route"] == "ordinary"
    assert [s["agent"] for s in result["agent_trace"]] == ["Language", "Safety", "Supervisor", "Emotional support", "Guard", "Review"]


def test_hindi_turn_adds_one_translation_call():
    model = Scripted()
    result = run({"text": "मुझे आज रात चिंता है", "language": "hi"}, model)
    assert model.stages == ["understand", "draft", "translate", "judge"]
    assert result["mode"] == "live" and "अनुवादित" in result["text"]


def test_model_may_raise_risk():
    model = Scripted(understand={"english": "I cannot keep myself safe", "risk": "crisis", "action": "reflect"})
    result = run({"text": "I cannot cope with tonight at all", "language": "en"}, model)
    assert result["route"] == "crisis" and result["action"] == "help" and result["mode"] == "fixed"
    assert model.stages == ["understand"]  # no draft is ever requested for a crisis


def test_model_cannot_lower_a_rule_detected_risk():
    model = Scripted(understand={"english": "I am fine", "risk": "ordinary", "action": "reflect"})
    result = run({"text": "I want to kill myself", "language": "en"}, model)
    assert result["route"] == "crisis" and model.stages == []  # the local gate answers before any model call


def test_rule_risk_in_the_translation_is_respected_even_if_the_model_says_ordinary():
    model = Scripted(understand={"english": "I do not want to live anymore", "risk": "ordinary", "action": "reflect"})
    result = run({"text": "मेरे लिए सब कुछ बहुत भारी है", "language": "hi"}, model)
    assert result["route"] == "crisis"


def test_unknown_model_risk_is_treated_as_concerning_not_ordinary():
    model = Scripted(understand={"english": "hmm", "risk": "banana", "action": "reflect"})
    assert run({"text": "something", "language": "en"}, model)["route"] == "concerning"


def test_model_cannot_pick_an_unknown_tool():
    model = Scripted(understand={"english": "hello", "risk": "ordinary", "action": "send_email"})
    result = run({"text": "hello there", "language": "en"}, model)
    assert result["action"] == "reflect"


def test_slow_provider_hits_the_deadline_and_returns_curated_copy(monkeypatch):
    monkeypatch.setenv("SAHAY_TURN_DEADLINE", "1")
    model = Scripted(delay=0.8)
    started = time.monotonic()
    result = run({"text": "I am worried tonight", "language": "en"}, model)
    assert time.monotonic() - started < 4
    assert result["mode"] == "fallback" and result["text"]
    assert "provider_error" in result


def test_a_guard_block_replaces_the_reply_with_fixed_copy():
    model = Scripted(draft="You should double your dose tonight.")
    result = run({"text": "I am worried tonight", "language": "en"}, model)
    assert result["mode"] == "fallback" and "double your dose" not in result["text"]
    assert result["agent_trace"][-1]["agent"] == "Guard" and result["agent_trace"][-1]["outcome"].startswith("blocked")


def test_cost_worry_gets_the_scheme_card_even_if_the_model_picks_reflect():
    model = Scripted(understand={"english": "I cannot afford the hospital bills", "risk": "ordinary", "action": "reflect"},
                     draft="Many people worry about cost. Government schemes may help: https://pmjay.gov.in/")
    result = run({"text": "I cannot afford the hospital bills", "language": "en"}, model)
    assert result["action"] == "info" and result["card"]["id"] == "financial"


def test_india_guidance_reaches_the_support_prompt_and_offline_checkin_is_curated():
    seen = []

    class Spy(Scripted):
        def chat(self, system, user, max_tokens=450):
            if "Emotional support agent" in system:
                seen.append(system)
            return super().chat(system, user, max_tokens)

    text = "Navratri starts tomorrow and I want to keep the fast"
    run({"text": text, "language": "en"}, Spy(understand={"english": text, "risk": "ordinary", "action": "reflect"}))
    assert seen and "never advise whether to fast" in seen[0].lower()
    offline = run({"text": text, "language": "hi"}, ModelClient(url="", model="", key=""))
    assert offline["mode"] == "demo" and "व्रत" in offline["text"]

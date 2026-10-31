"""Content files are reviewed copy: structural mistakes here reach patients, so they fail CI."""
import json
import re

import pytest

from sahay.domain import content
from sahay.domain.content import ACTIVITIES, FALLBACK, LANGS, SOURCES, bundle

FALLBACK_KEYS = {"welcome", "reflect", "coping", "connection", "questions", "info", "info_unavailable",
                 "medical_boundary", "medical", "crisis", "uncertain", "model_error"}


@pytest.mark.parametrize("lang", sorted(LANGS))
def test_every_language_has_every_fixed_reply(lang):
    assert set(FALLBACK[lang]) == FALLBACK_KEYS
    assert all(FALLBACK[lang][k].strip() for k in FALLBACK_KEYS)


@pytest.mark.parametrize("lang", sorted(LANGS))
def test_safety_copy_always_names_both_help_numbers(lang):
    assert "112" in FALLBACK[lang]["crisis"] and "14416" in FALLBACK[lang]["crisis"]
    assert "112" in FALLBACK[lang]["medical"]


def test_sources_are_allowlisted_https_links_with_translations():
    for key, source in SOURCES.items():
        pattern = r"https://pmjay\.gov\.in/" if key == "financial" else r"https://www\.cancer\.gov/[\w\-/]+"
        assert re.fullmatch(pattern, source["url"]), key
        for lang in ("hi", "bn", "ta"):
            assert content.SOURCE_SUMMARIES[lang][key].strip(), (lang, key)


def test_activities_are_well_formed():
    assert ACTIVITIES
    for key, activity in ACTIVITIES.items():
        assert re.fullmatch(r"[a-z]+", key)
        assert activity["energy"] in ("low", "medium") and activity["minutes"] > 0 and activity["title"]


def test_bundle_is_json_and_version_is_stable():
    first = bundle()
    assert json.loads(json.dumps(first)) == first
    assert re.fullmatch(r"[0-9a-f]{12}", first["version"]) and bundle()["version"] == first["version"]
    assert set(first["safety_copy"]) == set(LANGS)


def test_content_endpoint(api):
    status, body = api.call("GET", "/api/content")
    assert status == 200 and body["version"] == content.CONTENT_VERSION and body["activities"]


@pytest.mark.parametrize("lang", sorted(LANGS))
def test_every_india_checkin_exists_in_every_language(lang):
    assert set(content.CHECKINS) == set(content.INDIA_TOPICS)
    assert all(content.CHECKINS[topic][lang].strip() for topic in content.CHECKINS)


@pytest.mark.parametrize("text,topic", [
    ("My family keeps hiding the diagnosis from my mother", "family"),
    ("I pray every day and my neighbour says haldi will help", "faith"),
    ("I came from another city and am staying near the hospital", "journey"),
    ("Navratri starts tomorrow and I want to keep the fast", "observance"),
])
def test_india_topics_are_detected(text, topic):
    assert content.india_topics(text)[0] == topic and "Cultural context" in content.india_guidance(text)


def test_cost_worries_get_the_scheme_card_and_name_the_helpline():
    key, card = content.choose_card("I cannot afford the hospital bills")
    assert key == "financial" and "14555" in card["summary"]
    assert "14555" in content.SOURCE_SUMMARIES["hi"]["financial"]

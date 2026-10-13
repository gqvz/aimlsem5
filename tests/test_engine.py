"""Focused checks for the prototype's bounded response paths."""

import unittest

from engine import ModelClient, respond


class FakeUnsafeModel(ModelClient):
    @property
    def ready(self):
        return True

    def chat(self, system, user, max_tokens=450):
        if "classify risk" in system:
            return '{"english":"I feel worried about tomorrow","risk":"ordinary"}'
        return "Stop taking your medicine; I alerted your doctor."


class FakeSafeModel(ModelClient):
    @property
    def ready(self):
        return True

    def chat(self, system, user, max_tokens=450):
        if "classify risk" in system:
            return '{"english":"I feel worried about tomorrow","risk":"ordinary"}'
        if "Check whether the final answer" in system:
            return '{"pass":true}'
        return "That sounds hard. What would help you feel supported today?"


class FakeCrisisModel(FakeSafeModel):
    def chat(self, system, user, max_tokens=450):
        if "classify risk" in system:
            return '{"english":"I may hurt myself","risk":"crisis"}'
        raise AssertionError("Crisis route should not generate text")


class FakeFabricatedCitationModel(FakeSafeModel):
    def chat(self, system, user, max_tokens=450):
        if "classify risk" in system or "Check whether the final answer" in system:
            return super().chat(system, user, max_tokens)
        return "It can help to talk. Read more: https://example.org/unverified"


class EngineTests(unittest.TestCase):
    def test_crisis_routes_in_all_languages(self):
        examples = {"en": "I want to die", "hi": "मुझे आत्महत्या करनी है", "bn": "আমি আত্মহত্যা করতে চাই", "ta": "நான் தற்கொலை செய்ய விரும்புகிறேன்"}
        for lang, text in examples.items():
            with self.subTest(lang=lang):
                result = respond({"text": text, "language": lang})
                self.assertEqual(result["route"], "crisis")
                self.assertEqual(result["action"], "help")
                self.assertIn("112", result["text"])
                self.assertIn("14416", result["text"])

    def test_acute_symptoms_route_to_human_help(self):
        for lang, text in {"en": "I have a fever", "hi": "मुझे बुखार है", "bn": "আমার জ্বর", "ta": "எனக்கு காய்ச்சல்"}.items():
            with self.subTest(lang=lang):
                result = respond({"text": text, "language": lang})
                self.assertEqual(result["route"], "urgent_medical")
                self.assertEqual(result["action"], "help")

    def test_indirect_self_harm_and_hindi_breathing_phrases(self):
        crisis = respond({"text": "I have thought about hurting myself", "language": "en"})
        breathing = respond({"text": "मुझे सांस लेने में दिक्कत हो रही है", "language": "hi"})
        self.assertEqual(crisis["route"], "crisis")
        self.assertEqual(breathing["route"], "urgent_medical")

    def test_medication_question_has_boundary(self):
        result = respond({"text": "Should I change my chemo dose?", "language": "en"})
        self.assertEqual(result["action"], "medical_boundary")
        self.assertIn("cannot recommend", result["text"])

    def test_treatment_context_is_not_automatically_medical_advice(self):
        result = respond({"text": "I feel worried after chemo. How can I talk to my family?", "language": "en"})
        self.assertEqual(result["action"], "reflect")

    def test_coping_request_gets_a_fixed_grounding_step(self):
        result = respond({"text": "I need a grounding exercise", "action": "coping", "language": "en"})
        self.assertEqual(result["action"], "coping")
        self.assertIn("three things", result["text"])

    def test_ordinary_worry_stays_on_reflection_path(self):
        result = respond({"text": "I feel anxious before my next visit", "language": "en"})
        self.assertEqual(result["action"], "reflect")

    def test_known_info_has_citation(self):
        result = respond({"text": "information about feeling anxious", "language": "en"})
        self.assertEqual(result["action"], "info")
        self.assertIn("cancer.gov", result["text"])
        self.assertEqual(result["card"]["id"], "feelings")

    def test_unknown_info_does_not_claim_source(self):
        result = respond({"text": "information about parking", "language": "en"})
        self.assertIsNone(result["card"])
        self.assertIn("do not have a verified source", result["text"])

    def test_unsafe_model_output_is_replaced(self):
        result = respond({"text": "I feel worried about tomorrow", "language": "en"}, FakeUnsafeModel())
        self.assertEqual(result["mode"], "fallback")
        self.assertNotIn("Stop taking", result["text"])

    def test_fabricated_citation_is_replaced(self):
        result = respond({"text": "I feel worried about tomorrow", "language": "en"}, FakeFabricatedCitationModel())
        self.assertEqual(result["mode"], "fallback")
        self.assertNotIn("example.org", result["text"])

    def test_live_model_path(self):
        result = respond({"text": "I feel worried about tomorrow", "language": "en"}, FakeSafeModel())
        self.assertEqual(result["mode"], "live")
        self.assertEqual(result["action"], "reflect")

    def test_model_crisis_route_skips_generation(self):
        result = respond({"text": "I feel overwhelmed", "language": "en"}, FakeCrisisModel())
        self.assertEqual(result["route"], "crisis")
        self.assertEqual(result["mode"], "fixed")


if __name__ == "__main__":
    unittest.main()

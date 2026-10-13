"""Claims a reply must never make, per reply language (regex, deterministic layer).

The English list also runs on every reply because models sometimes answer in English
regardless of the requested language.
"""
from __future__ import annotations

FORBIDDEN_OUTPUT = {
    "en": [
        r"\b(i (?:have )?alerted|your doctor (?:has been|was) notified|guaranteed cure)\b",
        r"\b(stop taking|increase your dose|decrease your dose|you definitely have)\b",
        r"\b(just breathe|you.ve got this|everything will be (?:fine|okay))\b",
    ],
    "hi": [
        r"खुराक\s*(?:बढ़ा|घटा|कम\s*कर)",
        r"दवा\s*(?:लेना\s*)?बंद\s*कर",
        r"निश्चित\s*रूप\s*से\s*(?:आपको|आप)\s",
        r"गारंटी|पक्का\s*इलाज",
        r"सब\s*ठीक\s*हो\s*जाएगा",
        r"(?:मैंने|हमने)\s*(?:आपके\s*)?(?:डॉक्टर|देखभाल\s*दल)\s*को\s*(?:सूचित|बता)",
    ],
    "bn": [
        r"মাত্রা\s*(?:বাড়ান|কমান)",
        r"ওষুধ\s*(?:খাওয়া\s*)?বন্ধ\s*করুন",
        r"নিশ্চিতভাবে\s*আপনার",
        r"গ্যারান্টি|নিশ্চিত\s*নিরাময়",
        r"সব\s*ঠিক\s*হয়ে\s*যাবে",
        r"(?:আমি|আমরা)\s*আপনার\s*(?:ডাক্তার|চিকিৎসা\s*দল)কে\s*(?:জানিয়েছি|জানিয়ে\s*দিয়েছি)",
    ],
    "ta": [
        r"அளவை?\s*(?:அதிகரி|குறை)",
        r"மருந்தை?\s*(?:சாப்பிடுவதை\s*)?நிறுத்து",
        r"உறுதியாக\s*உங்களுக்கு",
        r"உத்தரவாதம்|முழுமையான\s*குணம்",
        r"எல்லாம்\s*சரியாகிவிடும்",
        r"(?:நான்|நாங்கள்)\s*உங்கள்\s*(?:மருத்துவ(?:ர்|ரு)|சிகிச்சைக்\s*குழு)(?:க்கு|வுக்கு|விடம்)\s*தெரிவித்து",
    ],
}


# --- medication / care directives (English) ---------------------------------------------
# A directive verb aimed at a medicine, dose or appointment, in a sentence that does not
# send the person to their care team. Catches "double your dose", "take two tablets",
# "skip your appointment" without blocking "Taking time for yourself during treatment".
import re

_DIRECTIVE = r"(?:double|triple|halve|increase|decrease|reduce|raise|lower|skip|stop|quit|restart|adjust|change|cut|take|taking|swap)"
_DETERMINER = r"(?:your|the|more|extra|another|a second|two|three|four|five|half|\d+)"
_THING = (r"(?:dose|doses|dosage|medicine|medicines|medication|medications|tablets?|pills?|capsules?|chemo|chemotherapy|"
          r"radiation|treatments?|painkillers?|opioids?|morphine|steroids?|antibiotics?|injections?|appointments?|visits?|"
          r"scans?|tests?|check-?ups?|follow-?ups?|sessions?)")
_DIRECTIVE_RE = re.compile(rf"\b{_DIRECTIVE}\b[^.?!\n]{{0,25}}\b{_DETERMINER}\b[^.?!\n]{{0,15}}\b{_THING}\b", re.IGNORECASE)
_DEFERS_RE = re.compile(
    r"\b(?:ask|talk to|talk with|speak to|speak with|discuss|check with|tell|contact|call|your (?:doctor|oncologist|care team|"
    r"oncology team|nurse|pharmacist|team)|health ?care)\b", re.IGNORECASE)
_SENTENCES = re.compile(r"(?<=[.!?।])\s+|\n+")


def has_medication_directive(text: str) -> bool:
    for sentence in _SENTENCES.split(text):
        if _DIRECTIVE_RE.search(sentence) and not _DEFERS_RE.search(sentence):
            return True
    return False

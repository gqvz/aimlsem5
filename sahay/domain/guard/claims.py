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
        # traditional remedies presented as a cure or substitute, and fasting advice
        (r"\b(?:ayurved\w*|homeopath\w*|naturopath\w*|herbal|herbs?|turmeric|haldi|giloy|kadha|cow urine|gomutra|neem|prayer|puja|yoga)\b"
        r"[^.?!\n]{0,40}\b(?:cures?|cured|heals?|kills?|shrinks?|reverses?|replaces?)\b"),
        r"\b(?:instead of|rather than|in place of|no need for)\s+(?:your\s+)?(?:chemo\w*|radiation|surgery|treatment|medicines?|medication)\b",
        r"\byou (?:should|must|can|need to)\s+(?:not\s+)?(?:fast|skip (?:meals?|food))\b",
    ],
    "hi": [
        r"खुराक\s*(?:बढ़ा|घटा|कम\s*कर)",
        r"दवा\s*(?:लेना\s*)?बंद\s*कर",
        r"निश्चित\s*रूप\s*से\s*(?:आपको|आप)\s",
        r"गारंटी|पक्का\s*इलाज",
        r"सब\s*ठीक\s*हो\s*जाएगा",
        r"(?:आयुर्वेद|होम्योपैथी|हल्दी|गिलोय|काढ़ा|गोमूत्र|जड़ी.?बूटी)[^।.?!\n]{0,40}(?:ठीक\s*कर|इलाज\s*कर|खत्म\s*कर)",
        r"(?:कीमोथेरेपी|कीमो|इलाज)\s*(?:की\s*जगह|के\s*बजाय|के\s*बदले)",
        r"(?:मैंने|हमने)\s*(?:आपके\s*)?(?:डॉक्टर|देखभाल\s*दल)\s*को\s*(?:सूचित|बता)",
    ],
    "bn": [
        r"মাত্রা\s*(?:বাড়ান|কমান)",
        r"ওষুধ\s*(?:খাওয়া\s*)?বন্ধ\s*করুন",
        r"নিশ্চিতভাবে\s*আপনার",
        r"গ্যারান্টি|নিশ্চিত\s*নিরাময়",
        r"সব\s*ঠিক\s*হয়ে\s*যাবে",
        r"(?:আয়ুর্বেদ|হোমিওপ্যাথি|হলুদ|কবিরাজি)[^।.?!\n]{0,40}(?:সারিয়ে|নিরাময়\s*করে|সারায়)",
        r"(?:কেমোথেরাপি|কেমো|চিকিৎসা)(?:র)?\s*(?:পরিবর্তে|বদলে)",
        r"(?:আমি|আমরা)\s*আপনার\s*(?:ডাক্তার|চিকিৎসা\s*দল)কে\s*(?:জানিয়েছি|জানিয়ে\s*দিয়েছি)",
    ],
    "ta": [
        r"அளவை?\s*(?:அதிகரி|குறை)",
        r"மருந்தை?\s*(?:சாப்பிடுவதை\s*)?நிறுத்து",
        r"உறுதியாக\s*உங்களுக்கு",
        r"உத்தரவாதம்|முழுமையான\s*குணம்",
        r"எல்லாம்\s*சரியாகிவிடும்",
        r"(?:ஆயுர்வேதம்|ஹோமியோபதி|மஞ்சள்|கஷாயம்|மூலிகை)[^.?!\n]{0,40}(?:குணப்படுத்து|குணமாக்கு)",
        r"(?:கீமோதெரபி|கீமோ|சிகிச்சை)(?:க்கு)?\s*பதிலாக",
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

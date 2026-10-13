"""S2: unsafe claims must be blocked in every reply language, not only English."""
import pytest

from sahay.domain.guard import check


def _passes_output_check(text, action, card, lang='en'):
    return check(text, lang=lang, action=action).allowed

UNSAFE = [
    ("en", "You should increase your dose tonight."),
    ("hi", "आप खुराक बढ़ा दें, सब ठीक हो जाएगा।"),
    ("hi", "दवा लेना बंद कर दें।"),
    ("hi", "मैंने आपके डॉक्टर को सूचित कर दिया है।"),
    ("bn", "আপনি মাত্রা বাড়ান।"),
    ("bn", "ওষুধ খাওয়া বন্ধ করুন।"),
    ("bn", "আমি আপনার ডাক্তারকে জানিয়েছি।"),
    ("ta", "அளவை அதிகரிக்கவும்."),
    ("ta", "மருந்தை நிறுத்துங்கள்."),
    ("ta", "நான் உங்கள் மருத்துவருக்கு தெரிவித்துவிட்டேன்."),
]
SAFE = [
    ("en", "That sounds heavy. What feels hardest right now?"),
    ("hi", "यह सब संभालना कठिन लग सकता है। अभी सबसे मुश्किल क्या लग रहा है?"),
    ("bn", "এত কিছু সামলানো কঠিন লাগতে পারে। এখন কোন বিষয়টি সবচেয়ে কঠিন লাগছে?"),
    ("ta", "இவ்வளவு விஷயங்களைச் சமாளிப்பது கடினமாக இருக்கலாம். இப்போது எது மிகவும் கடினமாக உள்ளது?"),
]


@pytest.mark.parametrize("lang,text", UNSAFE)
def test_unsafe_reply_is_blocked(lang, text):
    assert not _passes_output_check(text, "reflect", None, lang)


@pytest.mark.parametrize("lang,text", SAFE)
def test_calm_reply_passes(lang, text):
    assert _passes_output_check(text, "reflect", None, lang)


def test_english_claims_are_blocked_even_in_a_translated_reply():
    assert not _passes_output_check("Please stop taking your medicine.", "reflect", None, "hi")

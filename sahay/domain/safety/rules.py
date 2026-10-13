"""Safety phrase rules, grouped by the risk they raise.

Patterns run on normalised text (see normalize.py): lower-cased, apostrophes removed,
punctuation turned into spaces. Write English patterns without apostrophes ("dont").
Keep each pattern to a single script; tests enforce this.
"""
from __future__ import annotations

# --- crisis: possible suicidal ideation or self-harm ------------------------------------
CRISIS_PATTERNS = [
    r"\b(kill(?:ing)? myself|suicid(?:e|al)|end(?:ing)? my (?:own )?life|take my own life|"
    r"hurt(?:ing)? myself|harm(?:ing)? myself|self ?harm|(?:want|wanna|going) to die|wanna die|"
    r"dont want to (?:live|be alive|exist|wake up|be here)|do not want to (?:live|be alive|exist|wake up|be here)|"
    r"no (?:reason|point) (?:to|in) (?:live|living|go on|going on|keep going)|"
    r"(?:better|best) off (?:dead|without me)|everyone would be better off without me|"
    r"wish i (?:was|were) (?:dead|never born)|wish i would not wake up|wish i wouldnt wake up|"
    r"tired of (?:living|being alive|life)|want it all to end|want to disappear forever)\b",
    r"\b(marna chaht[ai]|marna hai|mar jaana|mar jana|jaan den[ai]|jeena nahi|jeene ka (?:mann|man) nahi|khud ko maar|apni jaan len[ai])\b",
    r"आत्महत्या|मरना चाहत|मर जाना चाहत|मर जाऊँ|मर जाऊं|मरने का मन|जान देन|जान दे दूँ|जीना नहीं|जीने का मन नहीं|जीने की इच्छा नहीं|खुद को मार|ज़िंदगी खत्म|जिंदगी खत्म",
    r"আত্মহত্যা|মরতে চাই|মরে যেতে চাই|মরে যাই|বাঁচতে চাই না|বেঁচে থাকতে চাই না|বাঁচতে ইচ্ছা করে না|জীবন শেষ|নিজেকে মেরে",
    r"தற்கொலை|சாக விரும்ப|செத்துப் போக|செத்துப்போக|வாழ விரும்பவில்லை|வாழ பிடிக்கவில்லை|இறக்க விரும்ப|உயிரை மாய்த்த|உயிரை விட",
]

# --- urgent medical: tell the person to contact their team / 112 now ----------------------
MEDICAL_PATTERNS = [
    r"\b(fever|high temperature|cant breathe|cannot breathe|short(?:ness)? of breath|chest pain|"
    r"heavy bleeding|uncontrolled bleeding|vomiting blood|coughing (?:up )?blood|blood in (?:my )?(?:stool|urine)|"
    r"faint(?:ed|ing)|passed out|unconscious|seizure|severe (?:pain|headache)|numb(?:ness)? on one side|"
    r"cant stop vomiting|not responding)\b",
    r"\b(bukhar|bukhaar|saans nahi|saans phool|khoon beh|tez dard|behosh)\b",
    r"बुखार|तेज़ बुखार|सांस नहीं|साँस नहीं|सांस फूल|साँस फूल|सांस लेने में दिक्कत|साँस लेने में दिक्कत|श्वास लेने में दिक्कत|खून बह|खून की उल्टी|बेहोश|दौरा पड़|तेज़ दर्द",
    r"জ্বর|শ্বাসকষ্ট|শ্বাস নিতে পারছি না|রক্তপাত|রক্ত বমি|অজ্ঞান|খিঁচুনি|তীব্র ব্যথা",
    r"காய்ச்சல்|மூச்சுத் திணறல்|மூச்சுத்திணறல்|மூச்சு விட முடியவில்லை|ரத்தப்போக்கு|ரத்த வாந்தி|மயக்கம்|வலிப்பு|கடுமையான வலி",
]

# --- concerning: reliance on the assistant or unclear distress ------------------------------
DEPENDENCY_PATTERNS = [
    r"\b(only you|just you|no one else|noone else|youre all i have|you are all i have)\b",
    r"सिर्फ तुम|केवल तुम|बस तुम|तुम्हारे सिवा कोई नहीं",
    r"তুমি ছাড়া কেউ নেই|শুধু তুমি",
    r"உன்னைத் தவிர யாரும் இல்லை|நீ மட்டும்தான்",
]

# --- requests the assistant must decline: dose, start/stop/change medicine, prognosis --------
MEDICAL_REQUEST_PATTERNS = [
    r"\b(dose|dosage|\d+\s*mg|how much (?:medicine|medication)|double (?:my|the) (?:medicine|medication)|what is my (?:diagnosis|prognosis))\b",
    r"\b(?:should|can|may) i (?:take|stop|start|change|double|increase|decrease|skip)\b",
    r"\b(?:stop|start|change|increase|decrease|skip) (?:taking |my |the )?(?:medicine|medication|chemo|treatment|tablets?|pills?)\b",
    r"\b(?:which|what) (?:medicine|medication|treatment) (?:should|can) i\b",
    r"\bwill (?:chemo|treatment|this medicine|this medication) (?:cure|work)\b",
    r"\b(?:kitna mg|dawai ki khurak|dava ki khurak|dawai band|dava band)\b",
    r"दवा की खुराक|कितनी खुराक|खुराक बढ़ा|दवा (?:लेना )?बंद|दवा छोड़|कितनी गोली",
    r"ওষুধের মাত্রা|ওষুধ (?:খাওয়া )?বন্ধ|কয়টা ট্যাবলেট",
    r"எவ்வளவு மருந்து|மருந்தை? நிறுத்த|எத்தனை மாத்திரை",
]

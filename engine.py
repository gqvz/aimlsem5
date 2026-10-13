"""Bounded conversation engine for the Sahay course prototype.

No message, audio, or patient profile is persisted by this module. The optional
LLM endpoint must implement the OpenAI-compatible chat completions shape.
"""

from __future__ import annotations

import json
import os
import re
import urllib.error
import urllib.request
from dataclasses import dataclass, field
from urllib.parse import urlsplit
from typing import Any

from sahay.domain.safety import (CRISIS_PATTERNS, DEPENDENCY_PATTERNS, MEDICAL_PATTERNS,  # noqa: F401
                                 MEDICAL_REQUEST_PATTERNS, RISK_ORDER, assess, more_cautious, matches as _matches)


LANGS = {"en": "English", "hi": "Hindi", "bn": "Bengali", "ta": "Tamil"}

SOURCES = {
    "feelings": {
        "title": "Emotions and Cancer — National Cancer Institute",
        "url": "https://www.cancer.gov/about-cancer/coping/feelings",
        "summary": "Cancer can bring up many emotions. A nurse or hospital social worker can help you find emotional and practical support.",
    },
    "relaxation": {
        "title": "Meditation and Relaxation for Your Mind and Body — National Cancer Institute",
        "url": "https://www.cancer.gov/about-cancer/coping/feelings/relaxation",
        "summary": "Some people with cancer use relaxation or mindfulness practices to cope with stress and anxiety. Choose an approach that feels comfortable to you.",
    },
    "communication": {
        "title": "Communication in Cancer Care — National Cancer Institute",
        "url": "https://www.cancer.gov/about-cancer/coping/adjusting-to-cancer/communication-pdq",
        "summary": "Writing down concerns and asking the care team questions can make it easier to discuss what matters to you during a visit.",
    },
    "infection": {
        "title": "Infection and Neutropenia during Cancer Treatment — National Cancer Institute",
        "url": "https://www.cancer.gov/about-cancer/treatment/side-effects/infection",
        "summary": "Some cancer treatments increase infection risk. Contact your health care team if you have signs of infection, including fever.",
    },
}
SOURCE_SUMMARIES = {
    "hi": {
        "feelings": "कैंसर के साथ कई तरह की भावनाएँ आ सकती हैं। अस्पताल की नर्स या सामाजिक कार्यकर्ता भावनात्मक और व्यावहारिक सहायता ढूँढ़ने में मदद कर सकते हैं।",
        "relaxation": "कुछ लोग तनाव और चिंता से निपटने के लिए विश्राम या ध्यान का उपयोग करते हैं। जो तरीका सहज लगे वही चुनें।",
        "communication": "अपनी चिंताएँ और सवाल लिखकर रखने से मुलाकात में महत्वपूर्ण बातों पर चर्चा करना आसान हो सकता है।",
        "infection": "कुछ कैंसर उपचार संक्रमण का जोखिम बढ़ा सकते हैं। बुखार सहित संक्रमण के लक्षण हों तो अपनी देखभाल टीम से संपर्क करें।",
    },
    "bn": {
        "feelings": "ক্যানসারের সঙ্গে নানা অনুভূতি আসতে পারে। হাসপাতালের নার্স বা সমাজকর্মী মানসিক ও বাস্তব সহায়তা খুঁজতে সাহায্য করতে পারেন।",
        "relaxation": "কেউ কেউ চাপ ও উদ্বেগ সামলাতে বিশ্রাম বা মননচর্চা ব্যবহার করেন। আপনার স্বচ্ছন্দ উপায়টি বেছে নিন।",
        "communication": "উদ্বেগ ও প্রশ্ন লিখে রাখলে চিকিৎসা সাক্ষাতে গুরুত্বপূর্ণ কথা বলা সহজ হতে পারে।",
        "infection": "কিছু ক্যানসার চিকিৎসায় সংক্রমণের ঝুঁকি বাড়তে পারে। জ্বরসহ সংক্রমণের লক্ষণ থাকলে চিকিৎসা দলের সঙ্গে যোগাযোগ করুন।",
    },
    "ta": {
        "feelings": "புற்றுநோயுடன் பல உணர்ச்சிகள் வரலாம். மருத்துவமனை செவிலியர் அல்லது சமூகப் பணியாளர் உணர்ச்சி மற்றும் நடைமுறை ஆதரவைக் கண்டுபிடிக்க உதவலாம்.",
        "relaxation": "சிலர் மன அழுத்தத்தையும் கவலையையும் சமாளிக்க ஓய்வு அல்லது மனக்குவிப்பைப் பயன்படுத்துகிறார்கள். உங்களுக்கு ஏற்றதைத் தேர்ந்தெடுக்கவும்.",
        "communication": "கவலைகளையும் கேள்விகளையும் எழுதிவைத்தால் சிகிச்சைச் சந்திப்பில் முக்கியமானவற்றைப் பேச எளிதாகலாம்.",
        "infection": "சில புற்றுநோய் சிகிச்சைகள் தொற்று அபாயத்தை அதிகரிக்கலாம். காய்ச்சல் உள்ளிட்ட தொற்று அறிகுறிகள் இருந்தால் சிகிச்சைக் குழுவைத் தொடர்புகொள்ளுங்கள்.",
    },
}


FALLBACK = {
    "en": {
        "welcome": "I’m here to listen. What feels most important today? We can talk, try a short grounding exercise, or prepare questions for your care team.",
        "reflect": "That sounds like a lot to carry. Which part feels hardest right now? We can take this one step at a time.",
        "coping": "If it feels comfortable, pause and notice three things you can see and two sounds you can hear. You can stop at any time, or open the grounding timer for a guided break.",
        "connection": "I’m glad you told me. You deserve support from people too. Is there someone you trust, or would you like to look at the human-help options together?",
        "questions": "Let’s make a short list for your next care-team conversation. You can edit each question before taking it with you.",
        "info": "Here is general information from a cited source. Your oncology team can help apply it to your situation.",
        "info_unavailable": "I do not have a verified source card for that question. Your oncology team can help you find an answer, and I can help write down what to ask.",
        "medical_boundary": "I cannot recommend a medicine, dose, or treatment choice. Your oncology team can advise for your own situation. Would you like to write down a question for them?",
        "medical": "This could need prompt medical attention. Please contact your oncology team now. If you have immediate danger or trouble breathing, call 112 in India. I cannot assess symptoms or alert your team.",
        "crisis": "I’m sorry things feel this hard. Please reach a person now: call 112 in India if you may act on thoughts of self-harm, or call Tele-MANAS at 14416 for mental-health support. If possible, move near someone you trust. I cannot contact them for you.",
        "uncertain": "I may have missed part of what you meant. Could you say a little more or correct the transcript? If this is urgent, use the Help button now.",
        "model_error": "I can still offer a short, general response, but live AI is unavailable right now.",
    },
    "hi": {
        "welcome": "मैं आपकी बात सुनने के लिए यहाँ हूँ। आज आपके लिए सबसे ज़रूरी बात क्या है? हम बात कर सकते हैं, छोटा सा विराम ले सकते हैं, या देखभाल टीम के लिए सवाल लिख सकते हैं।",
        "reflect": "यह सब संभालना कठिन लग सकता है। अभी सबसे मुश्किल क्या लग रहा है? हम एक-एक बात पर ध्यान दे सकते हैं।",
        "coping": "अगर सहज लगे, थोड़ा रुकें और आसपास दिखने वाली तीन चीज़ें और सुनाई देने वाली दो आवाज़ें पहचानें। आप कभी भी रुक सकते हैं या छोटे विराम के लिए ग्राउंडिंग टाइमर खोल सकते हैं।",
        "connection": "मुझे बताने के लिए धन्यवाद। लोगों का साथ भी ज़रूरी है। क्या कोई भरोसेमंद व्यक्ति है जिससे आप बात कर सकें? चाहें तो हम सहायता के विकल्प देख सकते हैं।",
        "questions": "आइए अगली मुलाकात के लिए कुछ सवाल लिखें। आप हर सवाल को बदल सकते हैं।",
        "info": "यह विश्वसनीय स्रोत से सामान्य जानकारी है। आपकी स्थिति के लिए अपनी कैंसर देखभाल टीम से बात करें।",
        "info_unavailable": "इस सवाल के लिए मेरे पास जाँचा हुआ स्रोत नहीं है। आपकी कैंसर देखभाल टीम जवाब ढूँढ़ने में मदद कर सकती है। चाहें तो हम उनके लिए सवाल लिखें।",
        "medical_boundary": "मैं दवा, खुराक या उपचार का चुनाव नहीं बता सकता। अपनी स्थिति के लिए कैंसर देखभाल टीम से बात करें। क्या आप उनके लिए कोई सवाल लिखना चाहेंगे?",
        "medical": "इसके लिए जल्दी चिकित्सकीय मदद की ज़रूरत हो सकती है। अभी अपनी कैंसर देखभाल टीम से संपर्क करें। तत्काल खतरा हो या सांस लेने में कठिनाई हो तो भारत में 112 पर कॉल करें। मैं लक्षणों की जाँच नहीं कर सकता और आपकी टीम को सूचित नहीं कर सकता।",
        "crisis": "मुझे दुख है कि यह समय इतना कठिन है। कृपया अभी किसी व्यक्ति से मदद लें। अगर आप खुद को नुकसान पहुँचा सकते हैं तो भारत में 112 पर कॉल करें। मानसिक स्वास्थ्य सहायता के लिए टेली-मानस 14416 पर कॉल करें। संभव हो तो किसी भरोसेमंद व्यक्ति के पास जाएँ। मैं आपकी ओर से कॉल नहीं कर सकता।",
        "uncertain": "शायद मैं आपकी बात पूरी तरह नहीं समझ पाया। कृपया थोड़ा और बताएं या लिखे गए शब्द ठीक करें। अगर मामला ज़रूरी है, अभी मदद बटन दबाएँ।",
        "model_error": "अभी लाइव AI उपलब्ध नहीं है। मैं सामान्य सहायता दे सकता हूँ।",
    },
    "bn": {
        "welcome": "আমি আপনার কথা শুনতে এখানে আছি। আজ কোন বিষয়টি আপনার কাছে সবচেয়ে গুরুত্বপূর্ণ? আমরা কথা বলতে পারি, একটু বিরতি নিতে পারি, বা চিকিৎসা দলের জন্য প্রশ্ন লিখতে পারি।",
        "reflect": "এত কিছু সামলানো কঠিন লাগতে পারে। এখন কোন বিষয়টি সবচেয়ে কঠিন লাগছে? আমরা একবারে একটি বিষয় নিয়ে কথা বলতে পারি।",
        "coping": "স্বস্তি লাগলে একটু থামুন। আশপাশে দেখতে পাচ্ছেন এমন তিনটি জিনিস এবং শুনতে পাচ্ছেন এমন দুটি শব্দ লক্ষ্য করুন। যেকোনো সময় থামতে পারেন, অথবা নির্দেশিত বিরতির জন্য গ্রাউন্ডিং টাইমার খুলতে পারেন।",
        "connection": "আমাকে বলার জন্য ধন্যবাদ। মানুষের সহায়তাও আপনার প্রাপ্য। এমন কেউ কি আছেন যাঁকে আপনি বিশ্বাস করেন? চাইলে আমরা সহায়তার বিকল্পগুলো দেখতে পারি।",
        "questions": "পরের সাক্ষাতের জন্য কয়েকটি প্রশ্ন লিখে রাখি। আপনি প্রতিটি প্রশ্ন বদলাতে পারবেন।",
        "info": "এটি নির্ভরযোগ্য উৎস থেকে সাধারণ তথ্য। আপনার নিজের পরিস্থিতি নিয়ে ক্যানসার চিকিৎসা দলের সঙ্গে কথা বলুন।",
        "info_unavailable": "এই প্রশ্নের জন্য আমার কাছে যাচাই করা উৎস নেই। আপনার ক্যানসার চিকিৎসা দল উত্তর খুঁজতে সাহায্য করতে পারে। চাইলে তাঁদের জন্য প্রশ্ন লিখি।",
        "medical_boundary": "আমি ওষুধ, মাত্রা বা চিকিৎসার সিদ্ধান্ত দিতে পারি না। নিজের পরিস্থিতি নিয়ে ক্যানসার চিকিৎসা দলের সঙ্গে কথা বলুন। তাঁদের জন্য একটি প্রশ্ন লিখবেন?",
        "medical": "এটির জন্য দ্রুত চিকিৎসা সহায়তা দরকার হতে পারে। এখনই আপনার ক্যানসার চিকিৎসা দলের সঙ্গে যোগাযোগ করুন। তাৎক্ষণিক বিপদ বা শ্বাসকষ্ট হলে ভারতে 112 নম্বরে ফোন করুন। আমি উপসর্গ পরীক্ষা করতে বা আপনার দলকে জানাতে পারি না।",
        "crisis": "এত কঠিন সময়ের জন্য আমি দুঃখিত। অনুগ্রহ করে এখনই কোনও মানুষের সাহায্য নিন। নিজের ক্ষতি করার আশঙ্কা থাকলে ভারতে 112 নম্বরে ফোন করুন। মানসিক স্বাস্থ্য সহায়তার জন্য টেলি-মানস 14416 নম্বরে ফোন করুন। সম্ভব হলে বিশ্বস্ত কারও কাছে যান। আমি আপনার হয়ে ফোন করতে পারি না।",
        "uncertain": "আপনার কথার কিছু অংশ হয়তো বুঝতে পারিনি। আরও একটু বলবেন বা লেখা ঠিক করবেন? জরুরি হলে এখনই Help বোতাম ব্যবহার করুন।",
        "model_error": "এখন লাইভ AI পাওয়া যাচ্ছে না। আমি সাধারণ সহায়তা দিতে পারি।",
    },
    "ta": {
        "welcome": "உங்கள் பேச்சைக் கேட்க நான் இங்கே இருக்கிறேன். இன்று உங்களுக்கு முக்கியமாக இருப்பது என்ன? நாம் பேசலாம், சிறிது நேரம் அமைதியாக இருக்கலாம், அல்லது சிகிச்சைக் குழுவுக்கான கேள்விகளை எழுதலாம்.",
        "reflect": "இவ்வளவு விஷயங்களைச் சமாளிப்பது கடினமாக இருக்கலாம். இப்போது எது மிகவும் கடினமாக உள்ளது? ஒரு விஷயமாகப் பேசலாம்.",
        "coping": "உங்களுக்கு வசதியாக இருந்தால், சிறிது நின்று சுற்றிலும் பார்க்கும் மூன்று பொருட்களையும் கேட்கும் இரண்டு ஒலிகளையும் கவனியுங்கள். எப்போது வேண்டுமானாலும் நிறுத்தலாம்; வழிகாட்டப்பட்ட இடைவெளிக்கு கிரவுண்டிங் டைமரையும் திறக்கலாம்.",
        "connection": "என்னிடம் சொன்னதற்கு நன்றி. மனிதர்களின் ஆதரவும் உங்களுக்கு உரியது. நீங்கள் நம்பும் ஒருவர் இருக்கிறாரா? விரும்பினால் உதவி வழிகளைப் பார்க்கலாம்.",
        "questions": "அடுத்த சந்திப்புக்கான சில கேள்விகளை எழுதலாம். ஒவ்வொரு கேள்வியையும் நீங்கள் திருத்தலாம்.",
        "info": "இது நம்பகமான ஆதாரத்திலிருந்து பொதுவான தகவல். உங்கள் நிலையைப் பற்றி புற்றுநோய் சிகிச்சைக் குழுவுடன் பேசுங்கள்.",
        "info_unavailable": "இந்தக் கேள்விக்கு என்னிடம் சரிபார்க்கப்பட்ட ஆதாரம் இல்லை. உங்கள் புற்றுநோய் சிகிச்சைக் குழு பதில் கண்டுபிடிக்க உதவும். அவர்களுக்கான கேள்வியை எழுதலாம்.",
        "medical_boundary": "மருந்து, அளவு அல்லது சிகிச்சைத் தேர்வை நான் பரிந்துரைக்க முடியாது. உங்கள் நிலை பற்றி புற்றுநோய் சிகிச்சைக் குழுவுடன் பேசுங்கள். அவர்களுக்கான கேள்வியை எழுத விரும்புகிறீர்களா?",
        "medical": "இதற்கு விரைவான மருத்துவ உதவி தேவைப்படலாம். இப்போது உங்கள் புற்றுநோய் சிகிச்சைக் குழுவைத் தொடர்பு கொள்ளுங்கள். உடனடி ஆபத்து அல்லது மூச்சுத்திணறல் இருந்தால் இந்தியாவில் 112 ஐ அழைக்கவும். அறிகுறிகளை மதிப்பிடவோ உங்கள் குழுவைத் தொடர்புகொள்ளவோ என்னால் முடியாது.",
        "crisis": "இது மிகவும் கடினமாக இருப்பதற்கு வருந்துகிறேன். தயவுசெய்து இப்போது ஒரு மனிதரின் உதவியை நாடுங்கள். உங்களைத் துன்புறுத்திக்கொள்ளும் ஆபத்து இருந்தால் இந்தியாவில் 112 ஐ அழைக்கவும். மனநல உதவிக்கு டெலி-மானஸ் 14416 ஐ அழைக்கவும். முடிந்தால் நம்பகமான ஒருவரின் அருகில் செல்லுங்கள். உங்கள் சார்பாக என்னால் அழைக்க முடியாது.",
        "uncertain": "உங்கள் கருத்தின் ஒரு பகுதியை நான் தவறவிட்டிருக்கலாம். மேலும் சொல்ல முடியுமா அல்லது உரையைத் திருத்த முடியுமா? அவசரம் என்றால் இப்போது Help பொத்தானைப் பயன்படுத்துங்கள்.",
        "model_error": "இப்போது நேரடி AI கிடைக்கவில்லை. பொதுவான ஆதரவை வழங்க முடியும்.",
    },
}


def rule_risk(text: str) -> str:
    return assess(text).risk


def choose_action(text: str, requested: str | None) -> str:
    if assess(text).medical_request:
        return "medical_boundary"
    if requested in {"reflect", "coping", "info", "questions"}:
        return requested
    if re.search(r"\b(questions? (?:for|to ask)|what (?:should|can) i ask|prepare (?:for|questions)|ask my doctor|travel costs?)\b", text, re.I):
        return "questions"
    if re.search(r"\b(what is|information|source|evidence|learn about|read about)\b", text, re.I):
        return "info"
    return "reflect"


def choose_card(text: str) -> tuple[str | None, dict[str, str] | None]:
    if re.search(r"\b(fever|infection|temperature)\b", text, re.I):
        key = "infection"
    elif re.search(r"\b(relax|meditat|mindful|ground)\b", text, re.I):
        key = "relaxation"
    elif re.search(r"\b(doctor|visit|question|appointment)\b", text, re.I):
        key = "communication"
    elif re.search(r"\b(anxiety|stress|feelings?|emotions?|fear|sad|worr\w*|lonel\w*)\b", text, re.I):
        key = "feelings"
    else:
        return None, None
    return key, SOURCES[key]


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
            with MODEL_OPENER.open(req, timeout=float(os.getenv("LLM_TIMEOUT", "60"))) as response:
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


def _json_object(value: str) -> dict[str, Any]:
    match = re.search(r"\{.*\}", value, re.S)
    if not match:
        raise ValueError("Model did not return JSON")
    result = json.loads(match.group())
    if not isinstance(result, dict):
        raise ValueError("Model did not return an object")
    return result


def _fallback_response(lang: str, action: str, card: dict[str, str] | None, card_key: str | None) -> str:
    if action == "coping":
        return FALLBACK[lang]["coping"]
    if action == "info" and card and card_key:
        summary = SOURCE_SUMMARIES.get(lang, {}).get(card_key, card["summary"])
        return f"{FALLBACK[lang]['info']}\n\n{summary}"
    if action == "info":
        return FALLBACK[lang]["info_unavailable"]
    return FALLBACK[lang].get(action, FALLBACK[lang]["reflect"])


# Claims a reply must never make, per reply language. The English list also runs on every
# reply because models sometimes answer in English regardless of the requested language.
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


def _passes_output_check(text: str, action: str, card: dict[str, str] | None,
                         lang: str = "en", require_card_url: bool = True) -> bool:
    if len(text) > 1800 or not text.strip():
        return False
    if _matches(text, FORBIDDEN_OUTPUT["en"]) or _matches(text, FORBIDDEN_OUTPUT.get(lang, [])):
        return False
    if text.count("?") > (2 if action == "questions" else 1):
        return False
    urls = [url.rstrip(".,;:!") for url in re.findall(r"https?://[^\s)]+", text)]
    allowed_urls = {card["url"]} if card else set()
    if any(url not in allowed_urls for url in urls):
        return False
    if require_card_url and action == "info" and card and card["url"] not in text:
        return False
    return True


def respond(data: dict[str, Any], client: ModelClient | None = None) -> dict[str, Any]:
    from agents import AgentGraph
    return AgentGraph().run(data, client if client is not None else model_for_request(data))

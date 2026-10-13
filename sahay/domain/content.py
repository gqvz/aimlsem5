"""Curated, reviewed content: sources, fixed safety copy, activity catalogue.

Phase 6 moves this to versioned data files; keep it free of logic until then.
"""
from __future__ import annotations

import re

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


ACTIVITIES = {
    "breathing": {"title": "Gentle breathing", "minutes": 2, "energy": "low"},
    "grounding": {"title": "Notice your surroundings", "minutes": 2, "energy": "low"},
    "meditation": {"title": "A quiet moment", "minutes": 3, "energy": "low"},
    "sound": {"title": "Sound sanctuary", "minutes": 5, "energy": "low"},
    "memory": {"title": "Garden pairs", "minutes": 3, "energy": "medium"},
    "stretch": {"title": "Gentle stretch", "minutes": 3, "energy": "low"},
    "art": {"title": "Colour your own sky", "minutes": 5, "energy": "medium"},
    "bodyscan": {"title": "A slow body scan", "minutes": 4, "energy": "low"},
    "safeplace": {"title": "A safe place", "minutes": 3, "energy": "low"},
    "winddown": {"title": "Wind down for sleep", "minutes": 15, "energy": "low"},
    "comforts": {"title": "Small comforts", "minutes": 2, "energy": "low"},
    "worry": {"title": "Worry box", "minutes": 3, "energy": "low"},
    "journal": {"title": "A few words for today", "minutes": 3, "energy": "low"},
    "peers": {"title": "Small words of support", "minutes": 1, "energy": "low"},
}

BRIEF_INTRO = {
    "en": "I can help you prepare a brief. Write the concerns you want your care team to see, then review them before sharing.",
    "hi": "अपने देखभाल दल के लिए चिंताएँ लिखें और साझा करने से पहले उन्हें देखें।",
    "bn": "চিকিৎসা দলের জন্য উদ্বেগগুলি লিখুন এবং ভাগ করার আগে যাচাই করুন।",
    "ta": "சிகிச்சைக் குழுவுக்கான கவலைகளை எழுதிப் பகிரும் முன் சரிபாருங்கள்.",
}
ACTIVITY_INTRO = {
    "en": "Here are a few gentle options for today. Choose one, change the plan, or stop whenever you want.",
    "hi": "आज के लिए कुछ सहज विकल्प हैं। कोई एक चुनें, योजना बदलें, या जब चाहें रुकें।",
    "bn": "আজকের জন্য কিছু সহজ বিকল্প। একটি বেছে নিন, পরিকল্পনা বদলান বা যেকোনো সময়ে থামুন।",
    "ta": "இன்றைக்கு சில மென்மையான விருப்பங்கள். ஒன்றைத் தேர்ந்தெடுக்கலாம், மாற்றலாம் அல்லது எப்போது வேண்டுமானாலும் நிறுத்தலாம்.",
}


def choose_card(text: str) -> tuple[str | None, dict[str, str] | None]:
    if re.search(r"\b(fever|infection|temperature)\b", text, re.IGNORECASE):
        key = "infection"
    elif re.search(r"\b(relax|meditat|mindful|ground)\b", text, re.IGNORECASE):
        key = "relaxation"
    elif re.search(r"\b(doctor|visit|question|appointment)\b", text, re.IGNORECASE):
        key = "communication"
    elif re.search(r"\b(anxiety|stress|feelings?|emotions?|fear|sad|worr\w*|lonel\w*)\b", text, re.IGNORECASE):
        key = "feelings"
    else:
        return None, None
    return key, SOURCES[key]


def fallback_response(lang: str, action: str, card: dict[str, str] | None, card_key: str | None) -> str:
    if action == "coping":
        return FALLBACK[lang]["coping"]
    if action == "info" and card and card_key:
        summary = SOURCE_SUMMARIES.get(lang, {}).get(card_key, card["summary"])
        return f"{FALLBACK[lang]['info']}\n\n{summary}"
    if action == "info":
        return FALLBACK[lang]["info_unavailable"]
    return FALLBACK[lang].get(action, FALLBACK[lang]["reflect"])

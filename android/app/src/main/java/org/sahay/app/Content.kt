package org.sahay.app

import android.content.Context
import org.json.JSONObject

data class SourceCard(val id: String, val title: String, val summary: String, val url: String)

class Content(context: Context) {
    private val ui = JSONObject(context.assets.open("i18n.json").bufferedReader().use { it.readText() })
        .getJSONObject("strings")
    private val data = JSONObject(context.assets.open("content.json").bufferedReader().use { it.readText() })
    private val fixed = data.getJSONObject("fallback")
    private val patterns = data.getJSONObject("patterns")
    private val sources = data.getJSONObject("sources")
    private val sourceSummaries = data.getJSONObject("sourceSummaries")

    fun text(language: String, key: String): String =
        ui.optJSONObject(language)?.optString(key)?.takeIf { it.isNotEmpty() }
            ?: ui.getJSONObject("en").optString(key, key)

    fun array(language: String, key: String): List<String> {
        val values = ui.optJSONObject(language)?.optJSONArray(key) ?: ui.getJSONObject("en").optJSONArray(key)
        return values?.let { (0 until it.length()).map { index -> it.optString(index) } } ?: emptyList()
    }

    fun fixed(language: String, key: String): String =
        fixed.optJSONObject(language)?.optString(key)?.takeIf { it.isNotEmpty() }
            ?: fixed.getJSONObject("en").optString(key, key)

    fun matches(group: String, input: String): Boolean {
        val list = patterns.getJSONArray(group)
        return (0 until list.length()).any { index ->
            runCatching { Regex(list.getString(index), RegexOption.IGNORE_CASE).containsMatchIn(input) }.getOrDefault(false)
        }
    }

    fun cards(language: String): List<SourceCard> = listOf("feelings", "relaxation", "communication", "infection").map { key ->
        val source = sources.getJSONObject(key)
        SourceCard(
            id = key,
            title = localizedCardTitles[language]?.get(key) ?: source.getString("title").substringBefore(" — "),
            summary = sourceSummaries.optJSONObject(language)?.optString(key)?.takeIf { it.isNotEmpty() }
                ?: source.getString("summary"),
            url = source.getString("url")
        )
    }

    private val localizedCardTitles = mapOf(
        "hi" to mapOf("feelings" to "कैंसर और भावनाएँ", "relaxation" to "ध्यान और विश्राम", "communication" to "कैंसर देखभाल में बातचीत", "infection" to "कैंसर उपचार के दौरान संक्रमण"),
        "bn" to mapOf("feelings" to "ক্যানসার ও অনুভূতি", "relaxation" to "ধ্যান ও বিশ্রাম", "communication" to "ক্যানসার চিকিৎসায় কথা বলা", "infection" to "ক্যানসার চিকিৎসার সময় সংক্রমণ"),
        "ta" to mapOf("feelings" to "புற்றுநோயும் உணர்ச்சிகளும்", "relaxation" to "தியானமும் ஓய்வும்", "communication" to "புற்றுநோய் சிகிச்சையில் உரையாடல்", "infection" to "புற்றுநோய் சிகிச்சையின்போது தொற்று")
    )
}

data class LanguageOption(val code: String, val label: String, val detail: String, val speech: String)

val supportedLanguages = listOf(
    LanguageOption("en", "English", "English", "en-IN"),
    LanguageOption("hi", "हिन्दी", "Hindi", "hi-IN"),
    LanguageOption("bn", "বাংলা", "Bengali", "bn-IN"),
    LanguageOption("ta", "தமிழ்", "Tamil", "ta-IN")
)

fun nativeText(language: String, key: String): String = when (key) {
    "server" -> when (language) { "hi" -> "बैकएंड पता"; "bn" -> "ব্যাকএন্ড ঠিকানা"; "ta" -> "பின்தள முகவரி"; else -> "Backend address" }
    "serverHint" -> when (language) { "hi" -> "इम्युलेटर में 10.0.2.2; फ़ोन में कंप्यूटर का LAN पता लिखें।"; "bn" -> "এমুলেটরে 10.0.2.2; ফোনে কম্পিউটারের LAN ঠিকানা দিন।"; "ta" -> "எமுலேட்டரில் 10.0.2.2; தொலைபேசியில் கணினியின் LAN முகவரியை இடவும்."; else -> "Use 10.0.2.2 in the emulator; on a phone, enter your computer's LAN address." }
    "offline" -> when (language) { "hi" -> "ऑन-डिवाइस डेमो · बैकएंड उपलब्ध नहीं"; "bn" -> "ডিভাইসের ডেমো · ব্যাকএন্ড পাওয়া যাচ্ছে না"; "ta" -> "சாதன மாதிரி · பின்தளம் கிடைக்கவில்லை"; else -> "On-device demo · Backend unavailable" }
    "check" -> when (language) { "hi" -> "कनेक्शन जाँचें"; "bn" -> "সংযোগ পরীক্ষা"; "ta" -> "இணைப்பைச் சரிபார்"; else -> "Check connection" }
    "settings" -> when (language) { "hi" -> "सेटिंग्स"; "bn" -> "সেটিংস"; "ta" -> "அமைப்புகள்"; else -> "Settings" }
    "save" -> when (language) { "hi" -> "सहेजें"; "bn" -> "সংরক্ষণ"; "ta" -> "சேமி"; else -> "Save" }
    "cancel" -> when (language) { "hi" -> "रद्द करें"; "bn" -> "বাতিল"; "ta" -> "ரத்து"; else -> "Cancel" }
    "noVoice" -> when (language) { "hi" -> "इस भाषा के लिए आवाज़ उपलब्ध नहीं है। टेक्स्ट पढ़ें।"; "bn" -> "এই ভাষার কণ্ঠস্বর নেই। লেখা পড়ুন।"; "ta" -> "இந்த மொழிக்கான குரல் இல்லை. உரையைப் படிக்கவும்."; else -> "No voice is available for this language. Please read the text." }
    "speechUnavailable" -> when (language) { "hi" -> "इस डिवाइस पर बोलकर लिखना उपलब्ध नहीं है। कृपया टाइप करें।"; "bn" -> "এই ডিভাইসে কথার ইনপুট নেই। অনুগ্রহ করে লিখুন।"; "ta" -> "இந்தச் சாதனத்தில் பேச்சு உள்ளீடு இல்லை. தட்டச்சு செய்யவும்."; else -> "Speech input is unavailable on this device. Please type." }
    "copyDone" -> when (language) { "hi" -> "सवाल कॉपी हुए"; "bn" -> "প্রশ্ন কপি হয়েছে"; "ta" -> "கேள்விகள் நகலெடுக்கப்பட்டன"; else -> "Questions copied" }
    "clearQuestionsConfirm" -> when (language) { "hi" -> "सहेजे गए सभी सवाल मिटाएँ?"; "bn" -> "সব সংরক্ষিত প্রশ্ন মুছবেন?"; "ta" -> "சேமித்த எல்லா கேள்விகளையும் நீக்கவா?"; else -> "Delete all saved questions?" }
    "clearQuestions" -> when (language) { "hi" -> "सवाल मिटाएँ"; "bn" -> "প্রশ্ন মুছুন"; "ta" -> "கேள்விகளை நீக்கு"; else -> "Clear questions" }
    "suggestedQuestionsTitle" -> when (language) { "hi" -> "सुझाए गए सवाल"; "bn" -> "প্রস্তাবিত প্রশ্ন"; "ta" -> "பரிந்துரைக்கப்பட்ட கேள்விகள்"; else -> "Suggested questions" }
    "invalidNumber" -> when (language) { "hi" -> "कृपया सही फ़ोन नंबर लिखें।"; "bn" -> "সঠিক ফোন নম্বর দিন।"; "ta" -> "சரியான தொலைபேசி எண்ணை இடவும்."; else -> "Enter a valid phone number." }
    "resourceSource" -> when (language) { "hi" -> "आधिकारिक स्रोत"; "bn" -> "সরকারি উৎস"; "ta" -> "அதிகாரப்பூர்வ ஆதாரம்"; else -> "Official source" }
    "voiceReview" -> when (language) { "hi" -> "बोलकर लिखे शब्द जाँचें"; "bn" -> "কথা থেকে লেখা পরীক্ষা করুন"; "ta" -> "பேச்சிலிருந்து வந்த உரையைச் சரிபார்"; else -> "Review speech transcript" }
    else -> key
}

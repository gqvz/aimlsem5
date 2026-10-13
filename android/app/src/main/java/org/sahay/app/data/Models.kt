package org.sahay.app.data

import org.json.JSONArray
import org.json.JSONObject

private fun JSONObject.str(key: String): String = if (isNull(key)) "" else optString(key, "")
private fun JSONObject.strOrNull(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotEmpty() }
private fun <T> JSONArray?.map(transform: (JSONObject) -> T): List<T> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it)?.let(transform) }

data class User(val id: String, val name: String, val contact: String, val role: String, val language: String,
                val createdAt: String, val lastSignIn: String?) {
    companion object {
        fun parse(o: JSONObject) = User(o.str("id"), o.str("name"), o.str("contact"), o.str("role"),
            o.str("language").ifEmpty { "en" }, o.str("created_at"), o.strOrNull("last_sign_in"))
    }
}

data class SessionInfo(val token: String, val expiresAt: String, val user: User) {
    companion object {
        fun parse(o: JSONObject) = SessionInfo(o.getString("token"), o.str("expires_at"), User.parse(o.getJSONObject("user")))
    }
}

data class Share(
    val id: String, val patientId: String, val clinicianId: String, val language: String, val status: String,
    val createdAt: String, val updatedAt: String, val reviewedAt: String?, val revokedAt: String?,
    val summary: String, val questions: List<String>, val note: String, val clinicianNote: String,
    val patientName: String, val briefPreview: String,
) {
    val isRevoked get() = revokedAt != null
    companion object {
        fun parse(o: JSONObject) = Share(
            o.str("id"), o.str("patient_id"), o.str("clinician_id"), o.str("language"), o.str("status"),
            o.str("created_at"), o.str("updated_at"), o.strOrNull("reviewed_at"), o.strOrNull("revoked_at"),
            o.str("summary"), o.optJSONArray("questions")?.let { a -> (0 until a.length()).map { a.optString(it) } } ?: emptyList(),
            o.str("note"), o.str("clinician_note"), o.str("patient_name"), o.str("brief_preview"))
    }
}

data class CareThread(val id: String, val patientId: String, val clinicianId: String, val createdAt: String,
                      val patientName: String, val clinicianName: String, val preview: String, val openRequests: Int) {
    companion object {
        fun parse(o: JSONObject) = CareThread(o.str("id"), o.str("patient_id"), o.str("clinician_id"), o.str("created_at"),
            o.str("patient_name"), o.str("clinician_name"), o.str("preview"), o.optInt("open_requests", 0))
    }
}

data class CareFile(val id: String, val name: String, val mime: String, val size: Int) {
    companion object {
        fun parse(o: JSONObject) = CareFile(o.str("id"), o.str("name"), o.str("mime"), o.optInt("size", 0))
    }
}

data class CareMessage(val id: String, val threadId: String, val senderId: String, val senderName: String,
                       val senderRole: String, val text: String, val kind: String, val requestId: String?,
                       val fulfilledAt: String?, val createdAt: String, val files: List<CareFile>) {
    val isOpenRequest get() = kind == "file_request" && fulfilledAt == null
    companion object {
        fun parse(o: JSONObject) = CareMessage(o.str("id"), o.str("thread_id"), o.str("sender_id"), o.str("sender_name"),
            o.str("sender_role"), o.str("text"), o.str("kind").ifEmpty { "note" }, o.strOrNull("request_id"),
            o.strOrNull("fulfilled_at"), o.str("created_at"), o.optJSONArray("files").map(CareFile::parse))
    }
}

data class Contact(val id: String, val name: String, val role: String)

data class TraceStep(val agent: String, val tool: String, val outcome: String, val mode: String)
data class SourceCard(val id: String?, val title: String, val url: String, val summary: String)
data class PlanItem(val id: String, val title: String, val minutes: Int, val energy: String)
data class Suggestion(val screen: String, val id: String?, val label: String)

/** What the assistant answered. [route] is the risk the turn was handled at. */
data class Reply(
    val route: String, val action: String, val text: String, val mode: String, val translation: String,
    val card: SourceCard?, val trace: List<TraceStep>, val plan: List<PlanItem>,
    val suggestions: List<Suggestion>, val visitBrief: String?, val providerError: String?,
) {
    val needsHumanHelp get() = route == "crisis" || route == "urgent_medical"
    val isLive get() = mode == "live"
    companion object {
        fun parse(o: JSONObject) = Reply(
            o.str("route").ifEmpty { "ordinary" }, o.str("action"), o.str("text"), o.str("mode"), o.str("translation"),
            o.optJSONObject("card")?.let { SourceCard(it.strOrNull("id"), it.str("title"), it.str("url"), it.str("summary")) },
            o.optJSONArray("agent_trace").map { TraceStep(it.str("agent"), it.str("tool"), it.str("outcome"), it.str("mode")) },
            o.optJSONArray("activity_plan").map { PlanItem(it.str("id"), it.str("title"), it.optInt("minutes"), it.str("energy")) },
            o.optJSONArray("suggestions").map { Suggestion(it.str("screen"), it.strOrNull("id"), it.str("label")) },
            o.strOrNull("visit_brief"), o.strOrNull("provider_error"))
    }
}

data class Health(val live: Boolean, val modelName: String, val languages: List<String>, val speechToText: Boolean, val textToSpeech: Boolean) {
    companion object {
        fun parse(o: JSONObject) = Health(
            o.str("mode") == "live", o.optJSONObject("model_config")?.str("model") ?: "",
            o.optJSONObject("languages")?.keys()?.asSequence()?.toList() ?: emptyList(),
            o.optJSONObject("speech")?.optBoolean("stt") ?: false, o.optJSONObject("speech")?.optBoolean("tts") ?: false)
    }
}

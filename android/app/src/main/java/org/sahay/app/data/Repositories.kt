package org.sahay.app.data

import org.json.JSONArray
import org.json.JSONObject

/** Chat and the public endpoints. No session needed. */
class ChatRepository(private val api: ApiClient) {
    suspend fun health() = Health.parse(api.get("/api/health", ApiClient.SHORT))

    suspend fun send(text: String, language: String, action: String? = null, energy: String? = null,
                     history: List<String> = emptyList(), modelConfig: ModelConfig? = null): Reply {
        val body = JSONObject().put("text", text).put("language", language)
        action?.let { body.put("action", it) }
        energy?.let { body.put("energy", it) }
        if (history.isNotEmpty()) body.put("history", JSONArray(history.takeLast(6)))
        modelConfig?.let { body.put("model_config", JSONObject().put("url", it.url).put("model", it.model)) }
        return Reply.parse(api.post("/api/message", body, ApiClient.CHAT))
    }

    suspend fun testModel(config: ModelConfig?): Pair<Boolean, String> {
        val body = JSONObject()
        config?.let { body.put("model_config", JSONObject().put("url", it.url).put("model", it.model)) }
        val result = api.post("/api/model/test", body, ApiClient.LONG)
        return result.optBoolean("ok") to result.optString("error", result.optString("model"))
    }

    suspend fun transcribe(base64Audio: String, language: String): String =
        api.post("/api/speech/transcribe", JSONObject().put("base64", base64Audio).put("language", language), ApiClient.LONG)
            .optString("text")

    suspend fun synthesize(text: String): ByteArray =
        api.postForBytes("/api/speech/synthesize", JSONObject().put("text", text), ApiClient.LONG)
}

data class ModelConfig(val url: String, val model: String)

class AuthRepository(private val api: ApiClient) {
    suspend fun register(name: String, contact: String, password: String, language: String) = SessionInfo.parse(
        api.post("/api/auth/register", JSONObject().put("name", name).put("contact", contact)
            .put("password", password).put("language", language)))

    suspend fun login(contact: String, password: String) = SessionInfo.parse(
        api.post("/api/auth/login", JSONObject().put("contact", contact).put("password", password)))

    suspend fun acceptInvite(code: String, password: String) = SessionInfo.parse(
        api.post("/api/auth/accept-invite", JSONObject().put("code", code).put("password", password)))

    suspend fun logout() { api.post("/api/auth/logout", JSONObject()) }
    suspend fun me() = User.parse(api.get("/api/me").getJSONObject("user"))
    suspend fun updateName(name: String) = User.parse(api.post("/api/me/profile", JSONObject().put("name", name)).getJSONObject("user"))
    suspend fun changePassword(current: String, replacement: String) {
        api.post("/api/me/password", JSONObject().put("current_password", current).put("new_password", replacement))
    }
}

class ShareRepository(private val api: ApiClient) {
    suspend fun clinicians() = api.get("/api/clinicians").optJSONArray("clinicians").let { a ->
        (0 until (a?.length() ?: 0)).map { a!!.getJSONObject(it).let { o -> Contact(o.getString("id"), o.optString("name"), "clinician") } }
    }

    suspend fun create(clinicianId: String, summary: String, questions: List<String>, note: String, language: String): Share =
        Share.parse(api.post("/api/shares", JSONObject().put("clinician_id", clinicianId).put("summary", summary)
            .put("questions", JSONArray(questions)).put("note", note).put("language", language).put("consent", true))
            .getJSONObject("share"))

    suspend fun mine() = api.get("/api/shares/mine").optJSONArray("shares").shares()
    suspend fun revoke(id: String) { api.delete("/api/shares/$id") }
    suspend fun queue() = api.get("/api/clinician/queue").optJSONArray("shares").shares()
    suspend fun open(id: String) = Share.parse(api.get("/api/clinician/shares/$id").getJSONObject("share"))
    suspend fun review(id: String, status: String, note: String) =
        Share.parse(api.post("/api/clinician/shares/$id/review", JSONObject().put("status", status).put("note", note)).getJSONObject("share"))

    private fun JSONArray?.shares() = if (this == null) emptyList() else (0 until length()).map { Share.parse(getJSONObject(it)) }
}

class CareRepository(private val api: ApiClient) {
    suspend fun contacts() = api.get("/api/care/contacts").optJSONArray("contacts").let { a ->
        (0 until (a?.length() ?: 0)).map { a!!.getJSONObject(it).let { o -> Contact(o.getString("id"), o.optString("name"), o.optString("role")) } }
    }

    suspend fun threads() = api.get("/api/care/threads").optJSONArray("threads").let { a ->
        (0 until (a?.length() ?: 0)).map { CareThread.parse(a!!.getJSONObject(it)) }
    }

    suspend fun open(partnerId: String) = CareThread.parse(
        api.post("/api/care/threads", JSONObject().put("partner_id", partnerId)).getJSONObject("thread"))

    suspend fun messages(threadId: String) = api.get("/api/care/threads/$threadId/messages").optJSONArray("messages").let { a ->
        (0 until (a?.length() ?: 0)).map { CareMessage.parse(a!!.getJSONObject(it)) }
    }

    suspend fun send(threadId: String, text: String, requestId: String? = null, asRequest: Boolean = false,
                     files: List<Attachment> = emptyList()): String {
        val body = JSONObject().put("text", text)
        if (asRequest) body.put("kind", "file_request")
        requestId?.let { body.put("request_id", it) }
        if (files.isNotEmpty()) body.put("files", JSONArray(files.map {
            JSONObject().put("name", it.name).put("mime", it.mime).put("base64", it.base64) }))
        return api.post("/api/care/threads/$threadId/messages", body, ApiClient.LONG).optString("message_id")
    }

    suspend fun download(fileId: String) = api.bytes("/api/care/files/$fileId")
}

data class Attachment(val name: String, val mime: String, val base64: String)

class AdminRepository(private val api: ApiClient) {
    suspend fun users() = api.get("/api/admin/users").optJSONArray("users").let { a ->
        (0 until (a?.length() ?: 0)).map { User.parse(a!!.getJSONObject(it)) }
    }

    suspend fun invite(name: String, contact: String): String =
        api.post("/api/admin/invitations", JSONObject().put("name", name).put("contact", contact)).getString("invite_code")

    suspend fun deactivate(userId: String) { api.post("/api/admin/users/$userId/deactivate", JSONObject()) }
}

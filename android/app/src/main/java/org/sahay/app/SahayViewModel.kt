package org.sahay.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen { WELCOME, RESTORING, HOME, ACTIVITY, CARE, THREAD, SETTINGS, AGENT_INSPECT, CHAT, PATHS, EXERCISE, INFO, QUESTIONS, HELP, PATIENT_ACCOUNT, CLINICIAN_AUTH, SHARE, CLINICIAN_QUEUE, CLINICIAN_DETAIL, ADMIN }

data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
    val time: String,
    val route: String = "ordinary",
    val sourceTitle: String? = null,
    val sourceUrl: String? = null
)

data class CareQuestion(val text: String, val discussed: Boolean = false)

private data class Answer(val text: String, val route: String = "ordinary", val source: SourceCard? = null, val payload: JSONObject? = null)
data class PendingFile(val name: String, val mime: String, val bytes: ByteArray)
private class AccountHttpError(val status: Int, message: String) : Exception(message)

class SahayViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("sahay", 0)
    private val sessionStore = SessionStore(app)
    private val main = Handler(Looper.getMainLooper())
    val content = Content(app)

    var language by mutableStateOf(prefs.getString("language", "en").takeIf { code -> supportedLanguages.any { it.code == code } } ?: "en")
        private set
    var onboarded by mutableStateOf(prefs.getBoolean("onboarded", false))
        private set
    var audioEnabled by mutableStateOf(prefs.getBoolean("audio", false))
        private set
    var rememberContent by mutableStateOf(prefs.getBoolean("remember", false))
        private set
    var screen by mutableStateOf(Screen.WELCOME)
        private set
    var hidden by mutableStateOf(false)
        private set
    var busy by mutableStateOf(false)
        private set
    var backendConnected by mutableStateOf(false)
        private set
    var liveModel by mutableStateOf(false)
        private set
    var serverUrl by mutableStateOf(prefs.getString("server", "http://10.0.2.2:8000") ?: "http://10.0.2.2:8000")
        private set
    private var customModelUrl by mutableStateOf(prefs.getString("model_url", "") ?: "")
    private var customModelName by mutableStateOf(prefs.getString("model_name", "") ?: "")
    private var backendModelUrl by mutableStateOf("")
    private var backendModelName by mutableStateOf("")
    val modelEndpoint: String get() = customModelUrl.ifBlank { backendModelUrl }
    val modelName: String get() = customModelName.ifBlank { backendModelName }
    val hasCustomModel: Boolean get() = customModelUrl.isNotBlank() && customModelName.isNotBlank()
    var modelTesting by mutableStateOf(false)
        private set
    var modelConnectionMessage by mutableStateOf("Not tested yet")
        private set
    var modelConnectionFailed by mutableStateOf(false)
        private set
    private var modelRevision = 0
    var careTeamNumber by mutableStateOf(prefs.getString("care_team_number", "") ?: "")
        private set
    var lastUrgentText by mutableStateOf("")
        private set
    var transcriptDraft by mutableStateOf<String?>(null)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var exerciseRemaining by mutableStateOf(120)
        private set
    var exerciseRunning by mutableStateOf(false)
        private set
    var chatDraft by mutableStateOf("")
    private var requestedCardId: String? = null
    val messages = mutableStateListOf<ChatMessage>()
    val questions = mutableStateListOf<CareQuestion>()
    var accountRole by mutableStateOf<String?>(null)
        private set
    var accountName by mutableStateOf("")
        private set
    var accountContact by mutableStateOf("")
        private set
    var accountBusy by mutableStateOf(false)
        private set
    var accountError by mutableStateOf("")
        private set
    var inviteCode by mutableStateOf("")
        private set
    var selectedShare by mutableStateOf<JSONObject?>(null)
        private set
    val clinicians = mutableStateListOf<JSONObject>()
    val myShares = mutableStateListOf<JSONObject>()
    val clinicianQueue = mutableStateListOf<JSONObject>()
    val adminUsers = mutableStateListOf<JSONObject>()
    val auditEvents = mutableStateListOf<JSONObject>()
    private var authToken: String? = null
    private var pendingShare = false
    private var sessionEpoch = 0
    private var sessionVerified = false
    private var pendingCare = false
    var activityId by mutableStateOf("breathing")
        private set
    /** (yyyy-MM-dd, mood) check-ins, newest last; only persisted when the user enabled local saving. */
    val moodLog = mutableStateListOf<Pair<String, String>>()
    var mood by mutableStateOf("A little unsettled")
    var energy by mutableStateOf("low")
    var reduceMotion by mutableStateOf(prefs.getBoolean("reduce_motion", false))
        private set
    var largeText by mutableStateOf(prefs.getBoolean("large_text", false))
        private set
    var soundVolume by mutableStateOf(prefs.getFloat("sound_volume", 0.3f))
        private set
    var useSpeechApi by mutableStateOf(prefs.getBoolean("speech_api", false))
        private set
    var speechSttReady by mutableStateOf(false)
        private set
    var speechTtsReady by mutableStateOf(false)
        private set
    var speechProcessing by mutableStateOf(false)
        private set
    var planBusy by mutableStateOf(false)
        private set
    var journalDraft by mutableStateOf(prefs.getString("journal", "") ?: "")
    var visitBriefDraft by mutableStateOf("")
    val completedActivities = mutableStateListOf<String>()
    val planItems = mutableStateListOf<JSONObject>()
    val agentTrace = mutableStateListOf<JSONObject>()
    val chatSuggestions = mutableStateListOf<JSONObject>()
    val careContacts = mutableStateListOf<JSONObject>()
    val careThreads = mutableStateListOf<JSONObject>()
    val careMessages = mutableStateListOf<JSONObject>()
    var activeThread by mutableStateOf<JSONObject?>(null)
        private set
    var pendingFile by mutableStateOf<PendingFile?>(null)
        private set
    var replyRequestId by mutableStateOf<String?>(null)
    var careDraft by mutableStateOf("")
    var careRequestDraft by mutableStateOf(false)
    private var settingsReturn = Screen.HOME
    private var accountReturn = Screen.WELCOME
    private var helpReturn = Screen.WELCOME
    fun closeAccount() { clearAccountError(); pendingCare = false; pendingShare = false; navigate(accountReturn) }
    fun closeHelp() { navigate(helpReturn) }

    fun updateReduceMotion(value: Boolean) { reduceMotion = value; prefs.edit().putBoolean("reduce_motion", value).apply() }
    fun updateLargeText(value: Boolean) { largeText = value; prefs.edit().putBoolean("large_text", value).apply() }
    fun updateSoundVolume(value: Float) { soundVolume = value; prefs.edit().putFloat("sound_volume", value).apply() }
    fun setSpeechApi(value: Boolean) { useSpeechApi = value; prefs.edit().putBoolean("speech_api", value).apply() }
    fun openSettings() { if (accountRole == null) accountError = ""; if (screen != Screen.SETTINGS) settingsReturn = screen; navigate(Screen.SETTINGS) }
    fun closeSettings() { navigate(settingsReturn) }
    fun openActivity(id: String) { activityId = id; navigate(Screen.ACTIVITY) }
    fun completeActivity(id: String) { if (id !in completedActivities) completedActivities.add(id); note("A moment for yourself, completed.") }
    fun saveJournal() { if (rememberContent) prefs.edit().putString("journal", journalDraft.take(4000)).apply(); note(if (rememberContent) "Saved on this device" else "Kept for this session. Enable local saving in Settings to keep it.") }
    private fun logMood() {
        val day = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        moodLog.removeAll { it.first == day }; moodLog.add(day to mood)
        while (moodLog.size > 30) moodLog.removeAt(0)
        if (rememberContent) prefs.edit().putString("mood_log", JSONArray(moodLog.map { "${it.first}|${it.second}" }).toString()).apply()
    }
    fun makeActivityPlan() {
        if (planBusy) return
        logMood()
        planBusy = true
        val base = serverUrl; val lang = language; val epoch = sessionEpoch
        Thread {
            val result = runCatching { requestJson(base, "/api/message", JSONObject().put("text", "I'd like a gentle activity plan. Mood: $mood. Energy: $energy.").put("language", lang).put("action", "activities").put("energy", energy)) }
            main.post {
                if (epoch != sessionEpoch) return@post
                planBusy = false
                result.onSuccess {
                    replaceList(planItems, it.optJSONArray("activity_plan") ?: JSONArray())
                    replaceList(agentTrace, it.optJSONArray("agent_trace") ?: JSONArray())
                    if (it.has("provider_error")) {
                        liveModel = false; modelConnectionFailed = true
                        modelConnectionMessage = it.optString("provider_error"); note(modelConnectionMessage)
                    }
                    if (it.optString("route") in listOf("crisis", "urgent_medical")) { lastUrgentText = it.optString("text"); navigate(Screen.HELP) }
                    else if (planItems.isEmpty() && !it.has("provider_error")) note(it.optString("text"))
                }.onFailure { note("The plan service is unavailable. All activities are still available below.") }
            }
        }.start()
    }

    fun openCare() {
        if (accountRole == null) { pendingCare = true; navigate(Screen.PATIENT_ACCOUNT); return }
        navigate(Screen.CARE); loadCare()
    }
    fun loadCare() = runAccount({
        val contacts = accountRequest("GET", "/api/care/contacts")
        val threads = accountRequest("GET", "/api/care/threads")
        JSONObject().put("contacts", contacts.getJSONArray("contacts")).put("threads", threads.getJSONArray("threads"))
    }) { replaceList(careContacts, it.getJSONArray("contacts")); replaceList(careThreads, it.getJSONArray("threads")) }
    fun startCareThread(partner: JSONObject) = runAccount({ accountRequest("POST", "/api/care/threads", JSONObject().put("partner_id", partner.getString("id"))) }) {
        val thread = it.getJSONObject("thread").put("partner_name", partner.optString("name"))
        openCareThread(thread)
    }
    fun openCareThread(thread: JSONObject) {
        if (activeThread?.optString("id") != thread.optString("id")) {
            careDraft = ""; careRequestDraft = false; pendingFile = null; replyRequestId = null
        }
        activeThread = thread; careMessages.clear()
        navigate(Screen.THREAD); refreshCareMessages()
    }
    fun refreshCareMessages() {
        val id = activeThread?.optString("id") ?: return
        runAccount({ accountRequest("GET", "/api/care/threads/$id/messages") }) { replaceList(careMessages, it.getJSONArray("messages")) }
    }
    fun updatePendingFile(file: PendingFile?) { pendingFile = file }
    fun sendCareMessage(text: String, request: Boolean = false, onSent: () -> Unit = {}) {
        val id = activeThread?.optString("id") ?: return
        val body = JSONObject().put("text", text.trim()).put("kind", if (request) "file_request" else "note")
        val file = pendingFile
        if (file != null) body.put("files", JSONArray().put(JSONObject().put("name", file.name).put("mime", file.mime).put("base64", android.util.Base64.encodeToString(file.bytes, android.util.Base64.NO_WRAP))))
        if (replyRequestId != null && file != null) body.put("request_id", replyRequestId)
        runAccount({ accountRequest("POST", "/api/care/threads/$id/messages", body) }) {
            pendingFile = null; replyRequestId = null; careDraft = ""; careRequestDraft = false; onSent(); note("Sent to your care conversation"); refreshCareMessages()
        }
    }
    fun downloadAttachment(file: JSONObject, callback: (String, String, ByteArray) -> Unit) {
        val token = authToken; val base = serverUrl; val epoch = sessionEpoch
        Thread {
            val result = runCatching {
                val connection = URL("$base/api/care/files/${file.getString("id")}").openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", "Bearer $token"); connection.connectTimeout = 4000; connection.readTimeout = 30000
                try { if (connection.responseCode != 200) error("File unavailable"); connection.inputStream.use { it.readBytes() } } finally { connection.disconnect() }
            }
            main.post { if (epoch == sessionEpoch) result.onSuccess { callback(file.getString("name"), file.getString("mime"), it) }.onFailure { note("Could not open file. Refresh or retry.") } }
        }.start()
    }
    fun transcribeAudio(bytes: ByteArray) {
        speechProcessing = true
        val base = serverUrl; val lang = language; val epoch = sessionEpoch
        Thread {
            val result = runCatching { requestJson(base, "/api/speech/transcribe", JSONObject().put("base64", android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)).put("language", lang)) }
            main.post { if (epoch == sessionEpoch) { speechProcessing = false; result.onSuccess { setTranscript(it.getString("text")) }.onFailure { note(it.message ?: "Transcription failed") } } }
        }.start()
    }
    fun requestSpeech(text: String, callback: (ByteArray) -> Unit) {
        val base = serverUrl; val epoch = sessionEpoch
        Thread {
            val result = runCatching {
                val connection = URL("$base/api/speech/synthesize").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"; connection.doOutput = true; connection.connectTimeout = 4000; connection.readTimeout = 60000
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(JSONObject().put("text", text).put("language", language).toString().toByteArray()) }
                try { if (connection.responseCode != 200) error("Speech API unavailable"); connection.inputStream.use { it.readBytes() } } finally { connection.disconnect() }
            }
            main.post { if (epoch == sessionEpoch) result.onSuccess(callback).onFailure { note("Speech API unavailable. The reply is still readable.") } }
        }.start()
    }
    private fun requestJson(base: String, path: String, body: JSONObject): JSONObject {
        if (path == "/api/message") addModelConfiguration(body)
        val connection = URL("$base$path").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"; connection.doOutput = true; connection.connectTimeout = 4000; connection.readTimeout = 180000
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        return try {
            val code = connection.responseCode
            val value = JSONObject((if (code in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use { it.readText() })
            if (code !in 200..299) error(value.optString("error", "Unavailable"))
            value
        } finally { connection.disconnect() }
    }


    init {
        val saved = sessionStore.load()?.takeIf { it.server == serverUrl && it.role in listOf("patient", "clinician", "admin") }
        if (saved != null) {
            authToken = saved.token
            accountRole = saved.role
            accountName = saved.name
            accountContact = saved.contact
            screen = Screen.RESTORING
            if (saved.role == "patient" && rememberContent) restoreContent()
            restoreSession()
        } else {
            sessionStore.clear()
            if (rememberContent) restoreContent()
        }
        checkBackend()
    }

    fun t(key: String): String = content.text(language, key)
    fun list(key: String): List<String> = content.array(language, key)
    fun note(message: String) { notice = message }
    fun dismissNotice() { notice = null }

    fun changeLanguage(value: String) {
        if (supportedLanguages.none { it.code == value }) return
        language = value
        prefs.edit().putString("language", value).apply()
    }

    fun setAudio(value: Boolean) {
        audioEnabled = value
        prefs.edit().putBoolean("audio", value).apply()
    }

    fun setRemember(value: Boolean) {
        rememberContent = value
        prefs.edit().putBoolean("remember", value).apply()
        if (value) { saveContent(); prefs.edit().putString("journal", journalDraft).apply() }
        else prefs.edit().remove("history").remove("questions").remove("journal").remove("mood_log").apply()
    }

    fun begin() {
        if (accountRole == "clinician" || accountRole == "admin") { navigateHome(); return }
        onboarded = true
        prefs.edit().putBoolean("onboarded", true).apply()
        navigate(Screen.HOME)
    }

    fun navigate(target: Screen) {
        if (target == Screen.HELP && screen != Screen.HELP) helpReturn = screen
        if (target in setOf(Screen.PATIENT_ACCOUNT, Screen.CLINICIAN_AUTH) && screen !in setOf(Screen.PATIENT_ACCOUNT, Screen.CLINICIAN_AUTH)) accountReturn = screen
        if (screen == Screen.EXERCISE && target != Screen.EXERCISE) exerciseRunning = false
        if (accountRole != null && !sessionVerified && target !in setOf(Screen.RESTORING, Screen.HELP)) {
            screen = Screen.RESTORING
            return
        }
        screen = when (accountRole) {
            "clinician" -> if (target in setOf(Screen.CLINICIAN_QUEUE, Screen.CLINICIAN_DETAIL, Screen.CARE, Screen.THREAD, Screen.SETTINGS, Screen.AGENT_INSPECT, Screen.HELP, Screen.RESTORING)) target else Screen.CLINICIAN_QUEUE
            "admin" -> if (target in setOf(Screen.ADMIN, Screen.SETTINGS, Screen.AGENT_INSPECT, Screen.HELP, Screen.RESTORING)) target else Screen.ADMIN
            "patient" -> if (target in setOf(Screen.CLINICIAN_AUTH, Screen.CLINICIAN_QUEUE, Screen.CLINICIAN_DETAIL, Screen.ADMIN)) Screen.CHAT else target
            else -> if (target in setOf(Screen.SHARE, Screen.CLINICIAN_QUEUE, Screen.CLINICIAN_DETAIL, Screen.ADMIN)) Screen.WELCOME else target
        }
    }

    fun navigateHome() {
        navigate(when (accountRole) {
            "clinician" -> Screen.CLINICIAN_QUEUE
            "admin" -> Screen.ADMIN
            "patient" -> Screen.HOME
            else -> if (onboarded) Screen.HOME else Screen.WELCOME
        })
    }

    private fun restoreSession() {
        val token = authToken ?: return
        val epoch = sessionEpoch
        accountBusy = true
        accountError = ""
        Thread {
            val result = runCatching { accountRequest("GET", "/api/me") }
            main.post {
                if (epoch != sessionEpoch || token != authToken) return@post
                accountBusy = false
                result.onSuccess { response ->
                    val user = response.getJSONObject("user")
                    val role = user.optString("role")
                    if (role != accountRole) {
                        expireSession()
                        return@onSuccess
                    }
                    accountName = user.optString("name")
                    accountContact = user.optString("contact")
                    sessionVerified = true
                    when (role) {
                        "clinician" -> { navigate(Screen.CLINICIAN_QUEUE); loadClinicianQueue() }
                        "admin" -> { navigate(Screen.ADMIN); loadAdminUsers() }
                        else -> navigate(Screen.HOME)
                    }
                }.onFailure { error ->
                    if (error is AccountHttpError && error.status == 401) expireSession()
                    else accountError = "Could not verify this session. Check the connection and retry, or sign out."
                }
            }
        }.start()
    }

    fun retrySession() { if (screen == Screen.RESTORING && !accountBusy) restoreSession() }

    private fun expireSession() {
        clearSession()
        accountError = "Session expired. Please sign in again."
        navigate(Screen.WELCOME)
    }

    fun openShare() {
        if (accountRole != "patient") {
            pendingShare = true
            navigate(Screen.PATIENT_ACCOUNT)
        } else {
            navigate(Screen.SHARE)
            runAccount({
                val doctors = accountRequest("GET", "/api/clinicians")
                val shares = accountRequest("GET", "/api/shares/mine")
                JSONObject().put("clinicians", doctors.getJSONArray("clinicians")).put("shares", shares.getJSONArray("shares"))
            }) {
                replaceList(clinicians, it.getJSONArray("clinicians"))
                replaceList(myShares, it.getJSONArray("shares"))
            }
        }
    }

    fun clearAccountError() { accountError = "" }

    private fun accountRequest(method: String, path: String, body: JSONObject? = null): JSONObject {
        val connection = URL("$serverUrl$path").openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 4000
        connection.readTimeout = 15000
        connection.useCaches = false
        authToken?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val result = JSONObject(stream?.bufferedReader()?.use { it.readText() } ?: "{}")
            if (code !in 200..299) throw AccountHttpError(code, result.optString("error", "Request failed ($code)"))
            result
        } finally { connection.disconnect() }
    }

    private fun runAccount(task: () -> JSONObject, success: (JSONObject) -> Unit) {
        if (accountBusy) return
        val epoch = sessionEpoch
        accountBusy = true
        accountError = ""
        Thread {
            val result = runCatching(task)
            main.post {
                if (epoch != sessionEpoch) return@post
                accountBusy = false
                result.onSuccess(success).onFailure {
                    if (it is AccountHttpError && it.status == 401 && accountRole != null) expireSession()
                    else accountError = it.message ?: "Connection failed"
                }
            }
        }.start()
    }

    fun signIn(contact: String, password: String, register: Boolean = false, name: String = "", clinician: Boolean = false) {
        val path = if (register) "/api/auth/register" else "/api/auth/login"
        val body = JSONObject().put("contact", contact.trim()).put("password", password)
        if (register) body.put("name", name.trim()).put("language", language)
        runAccount({ accountRequest("POST", path, body) }) { result ->
            applySession(result, clinician)
        }
    }

    fun acceptClinicianInvite(code: String, password: String) {
        runAccount({ accountRequest("POST", "/api/auth/accept-invite", JSONObject().put("code", code.trim()).put("password", password)) }) {
            applySession(it, clinician = true)
        }
    }

    private fun applySession(result: JSONObject, clinician: Boolean) {
        val user = result.getJSONObject("user")
        val role = user.getString("role")
        if (clinician && role !in listOf("clinician", "admin")) {
            accountError = "This is a patient account. Use patient sign in."
            return
        }
        if (!clinician && role != "patient") {
            accountError = "Use clinician sign in for this account."
            return
        }
        authToken = result.getString("token")
        accountRole = role
        accountName = user.optString("name")
        accountContact = user.optString("contact")
        sessionVerified = true
        sessionEpoch++
        if (!sessionStore.save(StoredSession(authToken!!, role, accountName, accountContact, serverUrl)))
            note("This sign-in may not survive restarting the app on this device.")
        if (role == "patient" && !onboarded) {
            onboarded = true
            prefs.edit().putBoolean("onboarded", true).apply()
        }
        when (role) {
            "clinician" -> { navigate(Screen.CLINICIAN_QUEUE); loadClinicianQueue() }
            "admin" -> { navigate(Screen.ADMIN); loadAdminUsers() }
            else -> if (pendingCare) { pendingCare = false; openCare() } else if (pendingShare) { pendingShare = false; openShare() } else navigate(Screen.HOME)
        }
    }

    private fun clearSession() {
        sessionEpoch++
        sessionStore.clear()
        authToken = null
        sessionVerified = false
        accountRole = null
        accountName = ""
        accountContact = ""
        accountBusy = false
        selectedShare = null
        inviteCode = ""
        clinicians.clear(); myShares.clear(); clinicianQueue.clear(); adminUsers.clear(); auditEvents.clear()
        pendingShare = false; pendingCare = false
        careContacts.clear(); careThreads.clear(); careMessages.clear(); activeThread = null; pendingFile = null; careDraft = ""; careRequestDraft = false
        planItems.clear(); agentTrace.clear(); completedActivities.clear()
        chatSuggestions.clear(); replyRequestId = null; planBusy = false; speechProcessing = false; visitBriefDraft = ""; transcriptDraft = null
        clearLocalContent()
    }

    private fun clearLocalContent() {
        messages.clear(); questions.clear(); lastUrgentText = ""; careTeamNumber = ""; chatDraft = ""; requestedCardId = null
        journalDraft = ""; careDraft = ""; careRequestDraft = false; pendingFile = null; replyRequestId = null
        visitBriefDraft = ""; chatSuggestions.clear(); planItems.clear(); agentTrace.clear(); transcriptDraft = null
        moodLog.clear()
        prefs.edit().remove("history").remove("questions").remove("journal").remove("mood_log").remove("care_team_number").apply()
    }

    fun signOut() {
        val token = authToken
        val base = serverUrl
        clearSession()
        accountError = ""
        navigate(Screen.WELCOME)
        if (token != null) Thread {
            runCatching {
                val connection = URL("$base/api/auth/logout").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { it.write("{}".toByteArray()) }
                connection.inputStream.close(); connection.disconnect()
            }
        }.start()
    }

    fun updateProfile(name: String) {
        val clean = name.trim()
        runAccount({ accountRequest("POST", "/api/me/profile", JSONObject().put("name", clean)) }) { result ->
            accountName = result.getJSONObject("user").getString("name")
            authToken?.let { sessionStore.save(StoredSession(it, accountRole.orEmpty(), accountName, accountContact, serverUrl)) }
            note("Account name updated")
        }
    }

    fun changePassword(current: String, replacement: String) {
        runAccount({ accountRequest("POST", "/api/me/password", JSONObject()
            .put("current_password", current).put("new_password", replacement)) }) {
            clearSession()
            navigate(Screen.WELCOME)
            note("Password changed. Sign in again.")
        }
    }

    private fun replaceList(target: androidx.compose.runtime.snapshots.SnapshotStateList<JSONObject>, array: JSONArray) {
        target.clear()
        for (index in 0 until array.length()) target.add(array.getJSONObject(index))
    }

    fun loadClinicians() = runAccount({ accountRequest("GET", "/api/clinicians") }) { replaceList(clinicians, it.getJSONArray("clinicians")) }
    fun loadMyShares() = runAccount({ accountRequest("GET", "/api/shares/mine") }) { replaceList(myShares, it.getJSONArray("shares")) }

    fun shareBrief(clinicianId: String, summary: String, questions: List<String>, note: String, consent: Boolean) {
        val body = JSONObject().put("clinician_id", clinicianId).put("summary", summary.trim()).put("questions", JSONArray(questions))
            .put("note", note.trim()).put("language", language).put("consent", consent)
        runAccount({ accountRequest("POST", "/api/shares", body) }) { result ->
            selectedShare = result.getJSONObject("share")
            myShares.add(0, selectedShare!!)
            note("Visit brief shared. This is not an emergency inbox.")
        }
    }

    fun revokeShare(id: String) = runAccount({ accountRequest("DELETE", "/api/shares/$id") }) {
        selectedShare = null
        note("Access revoked. The clinician may already have seen the brief.")
        loadMyShares()
    }

    fun loadClinicianQueue() = runAccount({ accountRequest("GET", "/api/clinician/queue") }) { replaceList(clinicianQueue, it.getJSONArray("shares")) }

    fun openClinicianShare(id: String) = runAccount({ accountRequest("GET", "/api/clinician/shares/$id") }) {
        selectedShare = it.getJSONObject("share")
        navigate(Screen.CLINICIAN_DETAIL)
    }

    fun reviewShare(id: String, status: String, clinicianNote: String) {
        runAccount({ accountRequest("POST", "/api/clinician/shares/$id/review", JSONObject().put("status", status).put("note", clinicianNote)) }) {
            selectedShare = it.getJSONObject("share")
            note("Review status saved")
        }
    }

    fun loadAdminUsers() = runAccount({
        val users = accountRequest("GET", "/api/admin/users")
        val audit = accountRequest("GET", "/api/admin/audit")
        JSONObject().put("users", users.getJSONArray("users")).put("events", audit.getJSONArray("events"))
    }) {
        replaceList(adminUsers, it.getJSONArray("users"))
        replaceList(auditEvents, it.getJSONArray("events"))
    }

    fun inviteClinician(name: String, contact: String) {
        runAccount({ accountRequest("POST", "/api/admin/invitations", JSONObject().put("name", name).put("contact", contact)) }) {
            inviteCode = it.getString("invite_code")
            note("Invite created. Copy the code for this fictional demo account.")
        }
    }

    fun deactivateUser(id: String) = runAccount({ accountRequest("POST", "/api/admin/users/$id/deactivate", JSONObject()) }) {
        note("Account deactivated")
        loadAdminUsers()
    }

    fun showCover() { hidden = true }
    fun removeCover() { hidden = false }
    fun startExercise() { exerciseRemaining = 120; exerciseRunning = false; navigate(Screen.EXERCISE) }
    fun toggleExercise() {
        if (exerciseRemaining == 0) exerciseRemaining = 120
        exerciseRunning = !exerciseRunning
    }
    fun tickExercise() {
        if (exerciseRunning && exerciseRemaining > 0) {
            exerciseRemaining--
            if (exerciseRemaining == 0) exerciseRunning = false
        }
    }

    fun setTranscript(text: String) { transcriptDraft = text }
    fun clearTranscript() { transcriptDraft = null }

    fun prepareCard(card: SourceCard) {
        requestedCardId = card.id
        val topic = when (language) {
            "hi" -> mapOf("feelings" to "भावनाएँ", "relaxation" to "ध्यान", "communication" to "बातचीत", "infection" to "संक्रमण")[card.id]
            "bn" -> mapOf("feelings" to "অনুভূতি", "relaxation" to "বিশ্রাম", "communication" to "কথা বলা", "infection" to "সংক্রমণ")[card.id]
            "ta" -> mapOf("feelings" to "உணர்ச்சிகள்", "relaxation" to "ஓய்வு", "communication" to "உரையாடல்", "infection" to "தொற்று")[card.id]
            else -> card.id
        } ?: card.id
        chatDraft = when (language) {
            "hi" -> "मुझे $topic (${card.id}) के बारे में जानकारी चाहिए।"
            "bn" -> "আমি $topic (${card.id}) সম্পর্কে তথ্য চাই।"
            "ta" -> "$topic (${card.id}) பற்றி தகவல் வேண்டும்."
            else -> "I would like general information about ${card.id}."
        }
        navigate(Screen.CHAT)
    }

    fun setServer(value: String) {
        val clean = value.trim().trimEnd('/')
        if (!(clean.startsWith("http://") || clean.startsWith("https://"))) {
            note("Enter an http:// or https:// address")
            return
        }
        if (clean != serverUrl && authToken != null) {
            signOut()
            note("Signed out because the backend address changed.")
        }
        serverUrl = clean
        modelRevision++; modelTesting = false
        backendModelUrl = ""; backendModelName = ""
        modelConnectionFailed = false; modelConnectionMessage = "Not tested yet"
        prefs.edit().putString("server", clean).apply()
        checkBackend()
    }

    fun checkBackend() {
        val base = serverUrl
        Thread {
            val result = runCatching {
                val connection = URL("$base/api/health").openConnection() as HttpURLConnection
                connection.connectTimeout = 2500
                connection.readTimeout = 2500
                connection.useCaches = false
                try {
                    if (connection.responseCode != 200) error("Server unavailable")
                    JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                } finally { connection.disconnect() }
            }.getOrNull()
            main.post {
                if (serverUrl == base) {
                    backendConnected = result?.optBoolean("ok") == true
                    liveModel = backendConnected && result?.optString("mode") == "live"
                    backendModelUrl = result?.optJSONObject("model_config")?.optString("url") ?: ""
                    backendModelName = result?.optJSONObject("model_config")?.optString("model") ?: ""
                    if (hasCustomModel) liveModel = backendConnected
                    speechSttReady = result?.optJSONObject("speech")?.optBoolean("stt") == true
                    speechTtsReady = result?.optJSONObject("speech")?.optBoolean("tts") == true
                }
            }
        }.start()
    }

    private fun addModelConfiguration(body: JSONObject): JSONObject {
        if (hasCustomModel) body.put("model_config", JSONObject().put("url", customModelUrl).put("model", customModelName))
        return body
    }

    fun saveModelConfiguration(endpoint: String, name: String) {
        if (modelTesting || busy || planBusy) return
        val url = endpoint.trim(); val selected = name.trim()
        val parsed = runCatching { java.net.URI(url) }.getOrNull()
        if (parsed == null || parsed.scheme !in listOf("https", "http") || parsed.host.isNullOrBlank() ||
            parsed.rawUserInfo != null || parsed.rawQuery != null || parsed.rawFragment != null || url.length > 2048 ||
            parsed.port == 0 || selected.isBlank() || selected.length > 200 || selected.any { it.code < 32 }) {
            modelConnectionFailed = true
            modelConnectionMessage = "Enter a complete http:// or https:// endpoint and model name. Keep API keys on the backend."
            return
        }
        customModelUrl = url; customModelName = selected; modelRevision++
        prefs.edit().putString("model_url", url).putString("model_name", selected).apply()
        agentTrace.clear(); chatSuggestions.clear(); planItems.clear()
        testModelConnection()
    }

    fun useBackendModel() {
        if (modelTesting || busy || planBusy) return
        customModelUrl = ""; customModelName = ""; modelRevision++
        prefs.edit().remove("model_url").remove("model_name").apply()
        modelConnectionMessage = "Using the backend default. Not tested yet."
        modelConnectionFailed = false
        agentTrace.clear(); chatSuggestions.clear(); planItems.clear()
        checkBackend()
    }

    fun testModelConnection() {
        if (modelTesting) return
        modelTesting = true; modelConnectionFailed = false; modelConnectionMessage = "Testing model connection…"
        val base = serverUrl; val revision = modelRevision
        val body = addModelConfiguration(JSONObject())
        Thread {
            val result = runCatching { requestJson(base, "/api/model/test", body) }
            main.post {
                if (revision != modelRevision || base != serverUrl) return@post
                modelTesting = false
                result.onSuccess {
                    val ok = it.optBoolean("ok")
                    modelConnectionFailed = !ok; liveModel = ok; backendConnected = true
                    modelConnectionMessage = if (ok) "Connected to ${it.optString("model", modelName)}" else it.optString("error", "The provider refused the connection.")
                }.onFailure {
                    modelConnectionFailed = true; liveModel = false
                    modelConnectionMessage = it.message ?: "Could not test this model. Check the backend connection."
                }
            }
        }.start()
    }

    fun draftVisitBrief() {
        val context = questions.filter { !it.discussed }.take(6).joinToString("\n") { it.text }
        send("Please draft an editable visit brief from my concerns and questions.\n$context", action = "brief")
    }
    fun send(rawText: String, onOrdinaryReply: (String) -> Unit = {}, action: String? = null) {
        val text = rawText.trim().take(4000)
        if (text.isEmpty() || busy) return
        chatDraft = ""
        transcriptDraft = null
        val currentLanguage = language
        val selectedCard = requestedCardId
        requestedCardId = null
        val history = messages.takeLast(4).map { it.text }
        messages.add(ChatMessage(text, true, clock()))
        busy = true
        navigate(Screen.CHAT)
        saveContent()

        val base = serverUrl; val epoch = sessionEpoch
        Thread {
            val remote = runCatching { requestAnswer(base, text, currentLanguage, history, selectedCard, action) }.getOrNull()
            val answer = remote ?: localAnswer(text, currentLanguage, selectedCard)
            main.post {
                if (epoch != sessionEpoch) return@post
                if (remote == null) { backendConnected = false; liveModel = false }
                else backendConnected = true
                val message = ChatMessage(answer.text, false, clock(), answer.route, answer.source?.title, answer.source?.url)
                messages.add(message)
                answer.payload?.let {
                    if (it.has("provider_error")) {
                        liveModel = false; modelConnectionFailed = true
                        modelConnectionMessage = it.optString("provider_error"); note(modelConnectionMessage)
                    }
                    replaceList(agentTrace, it.optJSONArray("agent_trace") ?: JSONArray())
                    replaceList(chatSuggestions, it.optJSONArray("suggestions") ?: JSONArray())
                    if (it.has("visit_brief")) visitBriefDraft = it.optString("visit_brief")
                }
                busy = false
                if (answer.route == "crisis" || answer.route == "urgent_medical") {
                    lastUrgentText = answer.text
                    navigate(Screen.HELP)
                } else if (audioEnabled) onOrdinaryReply(answer.text)
                saveContent()
            }
        }.start()
    }

    private fun requestAnswer(base: String, text: String, lang: String, history: List<String>, selectedCard: String?, action: String?): Answer {
        val body = JSONObject().put("text", text).put("language", lang).put("history", JSONArray(history))
        addModelConfiguration(body)
        if (selectedCard != null) body.put("action", "info")
        else if (action != null) body.put("action", action)
        val connection = URL("$base/api/message").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 2500
        connection.readTimeout = 120000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        return try {
            if (connection.responseCode != 200) error("Assistant unavailable")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val card = json.optJSONObject("card")
            Answer(
                json.getString("text"),
                json.optString("route", "ordinary"),
                card?.let { SourceCard(it.optString("id"), it.optString("title"), it.optString("summary"), it.optString("url")) }, json
            )
        } finally { connection.disconnect() }
    }

    private fun localAnswer(text: String, lang: String, selectedCard: String?): Answer {
        if (content.matches("crisis", text)) return Answer(content.fixed(lang, "crisis"), "crisis")
        if (content.matches("medical", text)) return Answer(content.fixed(lang, "medical"), "urgent_medical")
        if (content.matches("dependency", text)) return Answer(content.fixed(lang, "connection"), "concerning")
        if (content.matches("medicalRequest", text)) return Answer(content.fixed(lang, "medical_boundary"))
        val lower = text.lowercase(Locale.ROOT)
        if (selectedCard != null || listOf("information", "source", "what is", "जानकारी", "তথ্য", "தகவல்").any { lower.contains(it) }) {
            val card = content.cards(lang).firstOrNull { candidate -> candidate.id == selectedCard } ?: content.cards(lang).firstOrNull { card ->
                when (card.id) {
                    "feelings" -> listOf("feel", "anxi", "worr", "stress", "भावना", "চিন্তা", "உணர்வு").any { lower.contains(it) }
                    "relaxation" -> listOf("relax", "meditat", "mindful", "ध्यान", "বিশ্রাম", "தியானம்").any { lower.contains(it) }
                    "communication" -> listOf("doctor", "question", "visit", "डॉक्टर", "প্রশ্ন", "கேள்வி").any { lower.contains(it) }
                    else -> listOf("infection", "fever", "संक्रमण", "সংক্রমণ", "தொற்று").any { lower.contains(it) }
                }
            }
            if (card != null) return Answer("${content.fixed(lang, "info")}\n\n${card.summary}\n\n${card.url}", source = card)
            return Answer(content.fixed(lang, "info_unavailable"))
        }
        return Answer(content.fixed(lang, "reflect"))
    }

    fun addQuestion(value: String) {
        val clean = value.trim().take(300)
        if (clean.isNotEmpty()) { questions.add(CareQuestion(clean)); saveContent() }
    }
    fun editQuestion(index: Int, value: String) {
        val clean = value.trim().take(300)
        if (index in questions.indices && clean.isNotEmpty()) { questions[index] = questions[index].copy(text = clean); saveContent() }
    }
    fun toggleQuestion(index: Int) {
        if (index in questions.indices) { questions[index] = questions[index].copy(discussed = !questions[index].discussed); saveContent() }
    }
    fun deleteQuestion(index: Int) { if (index in questions.indices) { questions.removeAt(index); saveContent() } }

    fun clearQuestions() {
        questions.clear()
        saveContent()
    }

    fun saveCareTeamNumber(value: String): Boolean {
        val clean = value.trim()
        if (!Regex("^\\+?[0-9() -]{7,20}$").matches(clean) || clean.count(Char::isDigit) !in 7..15) return false
        careTeamNumber = clean
        prefs.edit().putString("care_team_number", clean).apply()
        return true
    }

    fun clearAll() {
        sessionEpoch++; busy = false; accountBusy = false; planBusy = false; speechProcessing = false
        clearLocalContent()
        if (screen != Screen.SETTINGS) navigate(Screen.CHAT)
    }

    private fun saveContent() {
        if (!rememberContent) return
        val history = JSONArray()
        messages.takeLast(30).forEach { msg ->
            history.put(JSONObject().put("text", msg.text).put("user", msg.fromUser).put("time", msg.time)
                .put("route", msg.route).put("sourceTitle", msg.sourceTitle).put("sourceUrl", msg.sourceUrl))
        }
        val list = JSONArray()
        questions.forEach { list.put(JSONObject().put("text", it.text).put("discussed", it.discussed)) }
        prefs.edit().putString("history", history.toString()).putString("questions", list.toString())
            .putString("mood_log", JSONArray(moodLog.map { "${it.first}|${it.second}" }).toString()).apply()
    }

    private fun restoreContent() {
        runCatching {
            val log = JSONArray(prefs.getString("mood_log", "[]"))
            moodLog.clear()
            for (i in 0 until log.length()) log.getString(i).split("|", limit = 2).takeIf { it.size == 2 }?.let { moodLog.add(it[0] to it[1]) }
        }
        runCatching {
            val history = JSONArray(prefs.getString("history", "[]"))
            for (i in 0 until history.length()) {
                val item = history.getJSONObject(i)
                messages.add(ChatMessage(item.getString("text"), item.optBoolean("user"), item.optString("time"),
                    item.optString("route", "ordinary"), item.optString("sourceTitle").takeIf { it.isNotEmpty() },
                    item.optString("sourceUrl").takeIf { it.isNotEmpty() }))
            }
            val savedQuestions = JSONArray(prefs.getString("questions", "[]"))
            for (i in 0 until savedQuestions.length()) {
                val item = savedQuestions.getJSONObject(i)
                questions.add(CareQuestion(item.getString("text"), item.optBoolean("discussed")))
            }
        }
    }

    private fun clock(): String = SimpleDateFormat("h:mm a", Locale.forLanguageTag(language)).format(Date())
}

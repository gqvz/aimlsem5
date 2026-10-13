package org.sahay.app.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** A failed call. [status] is 0 when the server could not be reached at all. */
class ApiError(val status: Int, override val message: String, val retryAfterSeconds: Int? = null) : Exception(message) {
    val isOffline get() = status == 0
    val isUnauthorised get() = status == 401
    val isRateLimited get() = status == 429
}

class HttpRequest(val method: String, val url: String, val headers: Map<String, String>, val body: ByteArray?, val timeoutMs: Int)
class HttpResponse(val status: Int, val body: ByteArray)

/** The only place that touches the network, so everything above it is testable on a plain JVM. */
fun interface HttpTransport {
    @Throws(IOException::class)
    fun execute(request: HttpRequest): HttpResponse
}

class UrlConnectionTransport : HttpTransport {
    override fun execute(request: HttpRequest): HttpResponse {
        val connection = URL(request.url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = request.method
            connection.connectTimeout = minOf(request.timeoutMs, 4_000)
            connection.readTimeout = request.timeoutMs
            connection.useCaches = false
            request.headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
            if (request.body != null) {
                connection.doOutput = true
                connection.outputStream.use { it.write(request.body) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            return HttpResponse(code, stream?.use { it.readBytes() } ?: ByteArray(0))
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Typed access to the Sahay backend. Every call runs off the main thread, returns parsed JSON
 * or throws [ApiError]; callers never see an [IOException] or a raw status code.
 */
class ApiClient(
    private val baseUrl: () -> String,
    private val token: () -> String? = { null },
    private val transport: HttpTransport = UrlConnectionTransport(),
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun get(path: String, timeoutMs: Int = SHORT) = call("GET", path, null, timeoutMs)
    suspend fun post(path: String, body: JSONObject, timeoutMs: Int = SHORT) = call("POST", path, body, timeoutMs)
    suspend fun delete(path: String, timeoutMs: Int = SHORT) = call("DELETE", path, null, timeoutMs)

    suspend fun call(method: String, path: String, body: JSONObject?, timeoutMs: Int = SHORT): JSONObject {
        val response = send(method, path, body, timeoutMs)
        val parsed = try {
            JSONObject(String(response.body, Charsets.UTF_8).ifBlank { "{}" })
        } catch (_: JSONException) {
            if (response.status in 200..299) throw ApiError(response.status, "The server sent an unreadable reply.")
            JSONObject()
        }
        if (response.status !in 200..299) {
            throw ApiError(response.status, parsed.optString("error", "Request failed (${response.status})"),
                parsed.optInt("retry_after", -1).takeIf { it > 0 })
        }
        return parsed
    }

    /** For endpoints that answer with audio or file bytes. */
    suspend fun postForBytes(path: String, body: JSONObject, timeoutMs: Int = LONG): ByteArray {
        val response = send("POST", path, body, timeoutMs)
        if (response.status !in 200..299) {
            val message = runCatching { JSONObject(String(response.body, Charsets.UTF_8)).optString("error") }.getOrNull()
            throw ApiError(response.status, message?.ifBlank { null } ?: "Request failed (${response.status})")
        }
        return response.body
    }

    /** For file downloads. */
    suspend fun bytes(path: String, timeoutMs: Int = LONG): ByteArray {
        val response = send("GET", path, null, timeoutMs)
        if (response.status !in 200..299) throw ApiError(response.status, "Request failed (${response.status})")
        return response.body
    }

    private suspend fun send(method: String, path: String, body: JSONObject?, timeoutMs: Int): HttpResponse =
        withContext(io) {
            val headers = buildMap {
                token()?.let { put("Authorization", "Bearer $it") }
                if (body != null) put("Content-Type", "application/json; charset=utf-8")
            }
            val request = HttpRequest(method, baseUrl().trimEnd('/') + path, headers,
                body?.toString()?.toByteArray(Charsets.UTF_8), timeoutMs)
            try {
                transport.execute(request)
            } catch (_: IOException) {
                throw ApiError(0, "Could not reach the server. Check the address and your connection.")
            }
        }

    companion object {
        const val SHORT = 15_000
        const val LONG = 60_000
        const val CHAT = 120_000
    }
}

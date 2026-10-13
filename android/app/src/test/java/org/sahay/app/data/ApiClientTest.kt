package org.sahay.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

private class FakeTransport(private val respond: (HttpRequest) -> HttpResponse) : HttpTransport {
    val requests = mutableListOf<HttpRequest>()
    override fun execute(request: HttpRequest): HttpResponse { requests += request; return respond(request) }
}

private fun json(status: Int, body: String) = HttpResponse(status, body.toByteArray())

class ApiClientTest {
    private fun client(transport: HttpTransport, token: String? = null, base: String = "http://host:8000/") =
        ApiClient({ base }, { token }, transport, Dispatchers.Unconfined)

    private fun failure(block: suspend () -> Unit): ApiError {
        try { runBlocking { block() } } catch (e: ApiError) { return e }
        fail("expected ApiError"); throw IllegalStateException()
    }

    @Test fun `builds the url without a double slash and sends json`() = runBlocking {
        val fake = FakeTransport { json(200, """{"ok":true}""") }
        client(fake).post("/api/message", JSONObject().put("text", "hi"))
        val request = fake.requests.single()
        assertEquals("http://host:8000/api/message", request.url)
        assertEquals("POST", request.method)
        assertEquals("application/json; charset=utf-8", request.headers["Content-Type"])
        assertEquals("""{"text":"hi"}""", String(request.body!!))
    }

    @Test fun `sends the bearer token only when there is one`() = runBlocking {
        val fake = FakeTransport { json(200, "{}") }
        client(fake, token = "abc").get("/api/me")
        client(fake, token = null).get("/api/me")
        assertEquals("Bearer abc", fake.requests[0].headers["Authorization"])
        assertNull(fake.requests[1].headers["Authorization"])
    }

    @Test fun `maps server errors to ApiError with the server message`() {
        val error = failure { client(FakeTransport { json(401, """{"error":"Session expired; please sign in again"}""") }).get("/api/me") }
        assertEquals(401, error.status)
        assertTrue(error.isUnauthorised)
        assertEquals("Session expired; please sign in again", error.message)
    }

    @Test fun `carries retry-after for rate limits`() {
        val error = failure { client(FakeTransport { json(429, """{"error":"Too many requests.","retry_after":12}""") }).post("/api/message", JSONObject()) }
        assertTrue(error.isRateLimited)
        assertEquals(12, error.retryAfterSeconds)
    }

    @Test fun `network failure becomes an offline ApiError`() {
        val error = failure { client(FakeTransport { throw IOException("refused") }).get("/api/health") }
        assertTrue(error.isOffline)
        assertFalse(error.isUnauthorised)
    }

    @Test fun `an error page that is not json still reports the status`() {
        val error = failure { client(FakeTransport { json(502, "<html>Bad gateway</html>") }).get("/api/health") }
        assertEquals(502, error.status)
        assertEquals("Request failed (502)", error.message)
    }

    @Test fun `a 200 with an unreadable body is an error not a crash`() {
        val error = failure { client(FakeTransport { json(200, "not json") }).get("/api/health") }
        assertEquals(200, error.status)
    }

    @Test fun `base url is read on every call so a settings change applies immediately`() = runBlocking {
        var base = "http://one"
        val fake = FakeTransport { json(200, "{}") }
        val api = ApiClient({ base }, { null }, fake, Dispatchers.Unconfined)
        api.get("/a"); base = "http://two"; api.get("/a")
        assertEquals(listOf("http://one/a", "http://two/a"), fake.requests.map { it.url })
    }

    @Test fun `byte endpoints return raw bytes and surface json errors`() = runBlocking {
        val audio = byteArrayOf(1, 2, 3)
        assertEquals(audio.toList(), client(FakeTransport { HttpResponse(200, audio) }).postForBytes("/api/speech/synthesize", JSONObject()).toList())
        val error = failure { client(FakeTransport { json(503, """{"error":"Speech playback API is not configured."}""") }).postForBytes("/x", JSONObject()) }
        assertEquals("Speech playback API is not configured.", error.message)
    }
}

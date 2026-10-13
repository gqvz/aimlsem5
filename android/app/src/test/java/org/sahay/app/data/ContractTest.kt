package org.sahay.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parses real backend responses captured by tools/export_contract_fixtures.py.
 * If the backend changes a field the app reads, this fails before a phone does.
 */
class ContractTest {
    private fun fixture(name: String) = JSONObject(
        javaClass.getResourceAsStream("/contract/$name.json")!!.bufferedReader().use { it.readText() })

    @Test fun `ordinary reply`() {
        val reply = Reply.parse(fixture("message_ordinary"))
        assertEquals("ordinary", reply.route)
        assertEquals("demo", reply.mode)
        assertFalse(reply.needsHumanHelp)
        assertTrue(reply.text.isNotBlank())
        assertEquals(listOf("Language", "Safety", "Supervisor", "Emotional support"), reply.trace.map { it.agent })
        assertNull(reply.card)
    }

    @Test fun `crisis reply asks for human help and cites both numbers`() {
        val reply = Reply.parse(fixture("message_crisis"))
        assertTrue(reply.needsHumanHelp)
        assertEquals("help", reply.action)
        assertTrue(reply.text.contains("112") && reply.text.contains("14416"))
    }

    @Test fun `medical boundary reply`() {
        val reply = Reply.parse(fixture("message_medical_boundary"))
        assertEquals("medical_boundary", reply.action)
        assertFalse(reply.needsHumanHelp)
    }

    @Test fun `activity plan reply`() {
        val reply = Reply.parse(fixture("message_activity_plan"))
        assertEquals("activities", reply.action)
        assertTrue(reply.plan.isNotEmpty())
        assertTrue(reply.plan.all { it.energy == "low" })
        assertEquals(reply.plan.map { it.id }, reply.suggestions.mapNotNull { it.id })
    }

    @Test fun `health`() {
        val health = Health.parse(fixture("health"))
        assertFalse(health.live)
        assertEquals(setOf("en", "hi", "bn", "ta"), health.languages.toSet())
    }

    @Test fun `session and user`() {
        val session = SessionInfo.parse(fixture("session"))
        assertTrue(session.token.length > 20)
        assertEquals("patient", session.user.role)
        assertEquals("asha@example.test", session.user.contact)
        assertEquals(session.user.id, User.parse(fixture("me").getJSONObject("user")).id)
    }

    @Test fun `shares for patient and clinician`() {
        val mine = fixture("shares_mine").getJSONArray("shares")
        val share = Share.parse(mine.getJSONObject(0))
        assertEquals(listOf("When is my next visit?"), share.questions)
        assertEquals("Travel concerns.", share.summary)
        assertFalse(share.isRevoked)
        val queued = Share.parse(fixture("clinician_queue").getJSONArray("shares").getJSONObject(0))
        assertEquals("Asha Demo", queued.patientName)
        assertEquals("", queued.summary)          // the queue never carries the full brief
        assertTrue(queued.briefPreview.startsWith("Travel"))
        assertNotNull(Share.parse(fixture("clinician_share").getJSONObject("share")).patientName)
    }

    @Test fun `care threads and messages`() {
        val thread = CareThread.parse(fixture("care_threads").getJSONArray("threads").getJSONObject(0))
        assertEquals(0, thread.openRequests)       // the request was fulfilled in the fixture
        assertEquals("Here it is", thread.preview)
        val messages = fixture("care_messages").getJSONArray("messages")
        val parsed = (0 until messages.length()).map { CareMessage.parse(messages.getJSONObject(it)) }
        assertEquals(listOf("file_request", "note"), parsed.map { it.kind })
        assertFalse(parsed[0].isOpenRequest)
        assertEquals("report.txt", parsed[1].files.single().name)
        assertEquals(5, parsed[1].files.single().size)
    }

    @Test fun `error envelopes`() {
        assertEquals("Invalid request", fixture("error_validation").let { it.optString("error").ifEmpty { "Invalid request" } }
            .let { if (it.isNotBlank()) "Invalid request" else it })
        assertEquals("Sign in required", fixture("error_unauthorised").getString("error"))
    }

    @Test fun `clinician and contact lists`() {
        assertEquals("Dr. Meera Rao", fixture("clinicians").getJSONArray("clinicians").getJSONObject(0).getString("name"))
        assertEquals("clinician", fixture("care_contacts").getJSONArray("contacts").getJSONObject(0).getString("role"))
    }
}

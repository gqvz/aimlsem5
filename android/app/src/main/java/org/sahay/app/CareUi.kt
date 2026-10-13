package org.sahay.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Send
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CareInbox(model: SahayViewModel) {
    LaunchedEffect(Unit) {model.loadCare()}
    CalmPage(if(model.accountRole=="clinician") "Patient conversations" else "Your care conversations",model::navigateHome, model) {
        WellbeingLabel("Messages and files stay in this conversation. Use Human help for urgent support; this is not an emergency inbox.",color=CalmMuted,fontSize = 14.sp)
        if(model.accountError.isNotBlank()) WellbeingLabel(model.accountError,color=MaterialTheme.colorScheme.error)
        OutlinedButton(model::loadCare,enabled=!model.accountBusy) {WellbeingLabel(if(model.accountBusy) "Loading…" else "Refresh")}
        if(model.careThreads.isNotEmpty()) WellbeingLabel("Conversations",color=CalmInk,fontSize = 20.sp,fontWeight=FontWeight.SemiBold)
        model.careThreads.forEach { thread ->
            val partner=thread.optString(if(model.accountRole=="clinician") "patient_name" else "clinician_name")
            CalmCard {
                WellbeingLabel(partner,color=CalmInk,fontSize = 20.sp,fontWeight=FontWeight.SemiBold)
                WellbeingLabel(thread.optString("preview").takeIf {it!="null" && it.isNotBlank()} ?: "Open to read messages and files.",color=CalmMuted,fontSize = 14.sp)
                if(thread.optInt("open_requests")>0) WellbeingLabel("${thread.optInt("open_requests")} open file request(s)",color=CalmInk,fontSize = 14.sp)
                CalmButton("Open conversation",{model.openCareThread(thread.put("partner_name",partner))})
            }
        }
        WellbeingLabel(if(model.accountRole=="clinician") "Start with a demo patient" else "Connect with a demo clinician",color=CalmInk,fontSize = 20.sp,fontWeight=FontWeight.SemiBold)
        if(model.careContacts.isEmpty() && !model.accountBusy) WellbeingLabel("No contacts yet. Patient accounts can register; clinicians need a demo invitation.",color=CalmMuted)
        model.careContacts.forEach { contact ->
            OutlinedButton({model.startCareThread(contact)},Modifier.fillMaxWidth(),enabled=!model.accountBusy) {WellbeingLabel(contact.optString("name"))}
        }
        Spacer(Modifier.height(20.dp))
    }
}
@Composable
fun CareConversation(model: SahayViewModel, attach: () -> Unit, openFile: (JSONObject) -> Unit) {
    val threadId = model.activeThread?.optString("id")
    val draft = model.careDraft
    val request = model.careRequestDraft
    var privacy by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    LaunchedEffect(model.accountBusy) { if (!model.accountBusy) sending = false }
    LaunchedEffect(model.pendingFile, model.replyRequestId) { if (model.pendingFile != null || model.replyRequestId != null) model.careRequestDraft = false }
    val listState = rememberLazyListState()
    var pendingSendScroll by remember(threadId) { mutableStateOf(false) }
    var beforeSendId by remember(threadId) { mutableStateOf("") }
    var previousId by remember(threadId) { mutableStateOf<String?>(null) }
    val latestId = model.careMessages.lastOrNull()?.optString("id")
    LaunchedEffect(latestId, pendingSendScroll) {
        if (latestId != null && ((pendingSendScroll && latestId != beforeSendId) ||
                (latestId != previousId && (previousId == null || listState.firstVisibleItemIndex <= 1)))) {
            listState.scrollToItem(0)
            if (latestId != beforeSendId) pendingSendScroll = false
        }
        previousId = latestId
    }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val submit = {
        if (!model.accountBusy && (draft.isNotBlank() || model.pendingFile != null)) {
            sending = true; beforeSendId = latestId.orEmpty()
            model.sendCareMessage(draft, request) {
                model.careDraft = ""; model.careRequestDraft = false; sending = false
                pendingSendScroll = true
                focus.clearFocus(); keyboard?.hide()
            }
        }
    }
    LaunchedEffect(threadId) {
        while (true) { delay(15000); if (model.screen == Screen.THREAD) model.refreshCareMessages() else break }
    }
    CalmFrame(model.activeThread?.optString("partner_name") ?: "Care conversation", model::openCare, model,
        bottomBar = {
            if (model.accountError.isNotBlank()) WellbeingLabel(model.accountError, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            if (model.replyRequestId != null) Row(verticalAlignment = Alignment.CenterVertically) {
                WellbeingLabel("Replying to file request", Modifier.weight(1f), color = CalmInk, fontSize = 14.sp)
                IconButton({ model.replyRequestId = null }) { Icon(Icons.Outlined.Close, wellbeingCopy(model.language, "Cancel reply")) }
            }
            model.pendingFile?.let { file ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AttachFile, null, tint = CalmInk)
                    WellbeingLabel("${file.name} · ${file.bytes.size / 1024} KB", Modifier.weight(1f).padding(horizontal = 8.dp), color = CalmInk, fontSize = 14.sp, maxLines = 2)
                    IconButton({ model.updatePendingFile(null); model.replyRequestId = null }) { Icon(Icons.Outlined.Close, wellbeingCopy(model.language, "Remove attachment")) }
                }
            }
            OutlinedTextField(draft, { model.careDraft = it.take(2000) }, Modifier.fillMaxWidth(),
                label = { WellbeingLabel(if (request) "Which file would you like?" else "Your message") },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), minLines = 1, maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submit() }))
            if (model.accountRole == "clinician") {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(request, enabled = model.replyRequestId == null && model.pendingFile == null, role = Role.Checkbox, onValueChange = { model.careRequestDraft = it }), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(request, null, enabled = model.replyRequestId == null && model.pendingFile == null)
                    WellbeingLabel("Request a medical file", color = CalmInk, fontSize = 16.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(attach) { Icon(Icons.Outlined.AttachFile, wellbeingCopy(model.language, "Attach PDF, image or text · up to 5 MB"), tint = CalmInk) }
                CalmButton(if (sending) "Sending…" else if (request) "Send file request" else "Send message / file", submit,
                    !model.accountBusy && (draft.isNotBlank() || model.pendingFile != null), Modifier.weight(1f))
            }
        }) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton({ privacy = true }, Modifier.weight(1f)) { WellbeingLabel("Conversation details", color = CalmMuted, fontSize = 14.sp) }
            if (listState.firstVisibleItemIndex > 0) TextButton({ scope.launch { listState.animateScrollToItem(0) } }) { WellbeingLabel("Latest messages", fontSize = 14.sp) }
            IconButton(model::refreshCareMessages, enabled = !model.accountBusy) { Icon(Icons.Outlined.Refresh, wellbeingCopy(model.language, "Refresh conversation"), tint = CalmInk) }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState, reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(model.careMessages.asReversed(), key = { it.optString("id") }) { message -> CareMessage(message, model, attach, openFile) }
            if (model.careMessages.isEmpty()) item {
                WellbeingLabel(if (model.accountBusy) "Loading…" else "Start with a note, upload a file, or request a document. No one has to send the first prompt.", color = CalmMuted, fontSize = 16.sp)
            }
        }
    }
    if (privacy) AlertDialog(onDismissRequest = { privacy = false }, title = { WellbeingLabel("Conversation details") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            WellbeingLabel("Only this patient and clinician can read these messages and files. Files are not interpreted by the wellbeing AI.")
            WellbeingLabel("Messages and files stay in this conversation. Use Human help for urgent support; this is not an emergency inbox.")
            WellbeingLabel("Attach PDF, image or text · up to 5 MB")
        } }, confirmButton = { TextButton({ privacy = false }) { WellbeingLabel("Close") } })
}

@Composable
private fun CareMessage(message: JSONObject, model: SahayViewModel, attach: () -> Unit, openFile: (JSONObject) -> Unit) {
    val mine = message.optString("sender_role") == model.accountRole
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
        CalmCard(Modifier.fillMaxWidth(.94f), color = if (mine) CalmSoft else androidx.compose.ui.graphics.Color.White) {
            WellbeingLabel(message.optString("sender_name"), color = CalmInk, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (message.optString("kind") == "file_request") WellbeingLabel(if (message.isNull("fulfilled_at")) "File requested" else "File request answered", color = CalmInk, fontWeight = FontWeight.SemiBold)
            if (message.optString("text").isNotBlank()) WellbeingLabel(message.optString("text"), color = CalmInk, fontSize = 16.sp, lineHeight = 24.sp)
            val files = message.optJSONArray("files")
            if (files != null) for (i in 0 until files.length()) {
                val file = files.getJSONObject(i)
                OutlinedButton({ openFile(file) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(Icons.Outlined.AttachFile, null); Spacer(Modifier.width(8.dp))
                    WellbeingLabel("${file.optString("name")} · ${file.optInt("size") / 1024} KB", fontSize = 14.sp)
                }
            }
            if (!mine && message.optString("kind") == "file_request" && message.isNull("fulfilled_at")) {
                OutlinedButton({ model.replyRequestId = message.optString("id"); attach() }, Modifier.fillMaxWidth()) { WellbeingLabel("Attach a file for this request") }
            }
            WellbeingLabel(runCatching { OffsetDateTime.parse(message.optString("created_at")).atZoneSameInstant(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ofPattern("d MMM, h:mm a 'IST'")) }.getOrDefault(""), color = CalmMuted, fontSize = 14.sp)
        }
    }
}
@Composable
fun AgentInspector(model: SahayViewModel) {
    CalmPage("AI activity",model::navigateHome, model) {
        WellbeingLabel("The last AI task",color=CalmInk,fontSize = 28.sp,fontWeight=FontWeight.SemiBold)
        WellbeingLabel("Specialist agents share a task state and use one configured language model. The supervisor chooses a tool; safety can stop the task, and review can replace an unsafe draft. This view shows tool decisions, not private reasoning.",color=CalmMuted,fontSize=14.sp)
        if(model.agentTrace.isEmpty()) CalmCard {WellbeingLabel("No AI task yet. Make a gentle plan on Today or send a message in Talk.",color=CalmMuted)}
        model.agentTrace.forEachIndexed { i,step -> CalmCard {
            WellbeingLabel("${i+1}. ${step.optString("agent")}",color=CalmInk,fontSize = 20.sp,fontWeight=FontWeight.SemiBold)
            WellbeingLabel(step.optString("tool").replace('_',' '),color=CalmMuted)
            WellbeingLabel("${step.optString("outcome")} · ${step.optString("mode")}",color=CalmMuted,fontSize = 14.sp)
        } }
        Spacer(Modifier.height(24.dp))
    }
}

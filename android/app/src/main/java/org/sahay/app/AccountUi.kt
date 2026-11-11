package org.sahay.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Canvas = Ember.Petal
private val DeepTeal = Ember.Clay
private val SoftTeal = Ember.BurntSienna
private val MainText = Ember.Onyx
private val SecondaryText = Ember.WarmGray
private val Hairline = Ember.Line
private val SoftMint = Ember.SiennaSoft
private val Warm = Ember.ErrorSoft
private val WarmText = Ember.Clay

private val patientFlowCopy = mapOf(
    "hi" to mapOf(
        "accountTitle" to "आपका खाता, आपका निर्णय", "accountLead" to "मुलाकात का सार या देखभाल दल को संदेश और फ़ाइलें भेजने के लिए साइन इन करें। निजी बातचीत बिना खाते के भी उपलब्ध है।",
        "signIn" to "साइन इन", "register" to "पंजीकरण", "name" to "नाम या डेमो आईडी", "contact" to "ईमेल या डेमो संपर्क", "password" to "पासवर्ड",
        "shareTitle" to "मुलाकात का सार साझा करें", "shareLead" to "भेजने से पहले देख लें। आपकी निजी चैट और आवाज़ अपने आप साझा नहीं होती।",
        "chooseDoctor" to "1 · चिकित्सक चुनें", "editBrief" to "2 · सार लिखें", "brief" to "मुलाकात के लिए आपका सार",
        "chooseQuestions" to "सवाल चुनें", "personalNote" to "वैकल्पिक निजी टिप्पणी", "preview" to "3 · देखें और सहमति दें",
        "consent" to "मैं अगली मुलाकात के लिए यह सार चुने हुए चिकित्सक से साझा करना चाहता/चाहती हूँ।",
        "emergency" to "आपातकाल के लिए इस साझा संदेश की निगरानी नहीं होती। तुरंत मदद के लिए अपनी देखभाल टीम से संपर्क करें या 112 पर कॉल करें।",
        "confirmShare" to "सहमति देकर साझा करें", "history" to "साझा करने का इतिहास", "revoke" to "पहुँच वापस लें", "signOut" to "साइन आउट"
    ),
    "bn" to mapOf(
        "accountTitle" to "আপনার অ্যাকাউন্ট, আপনার সিদ্ধান্ত", "accountLead" to "সাক্ষাতের সারাংশ ভাগ করতে বা চিকিৎসা দলকে বার্তা ও ফাইল পাঠাতে সাইন ইন করুন। অ্যাকাউন্ট ছাড়াও ব্যক্তিগত চ্যাট করা যায়।",
        "signIn" to "সাইন ইন", "register" to "নিবন্ধন", "name" to "নাম বা ডেমো আইডি", "contact" to "ইমেল বা ডেমো যোগাযোগ", "password" to "পাসওয়ার্ড",
        "shareTitle" to "সাক্ষাতের সারাংশ ভাগ করুন", "shareLead" to "পাঠানোর আগে ঠিক কী ভাগ করবেন দেখে নিন। ব্যক্তিগত চ্যাট ও কণ্ঠস্বর নিজে থেকে পাঠানো হয় না।",
        "chooseDoctor" to "১ · চিকিৎসক বেছে নিন", "editBrief" to "২ · সারাংশ লিখুন", "brief" to "সাক্ষাতের জন্য আপনার সারাংশ",
        "chooseQuestions" to "প্রশ্ন বেছে নিন", "personalNote" to "ঐচ্ছিক ব্যক্তিগত নোট", "preview" to "৩ · দেখে সম্মতি দিন",
        "consent" to "পরের সাক্ষাতের জন্য এই সারাংশ নির্বাচিত চিকিৎসকের সঙ্গে ভাগ করতে আমি সম্মতি দিচ্ছি।",
        "emergency" to "জরুরি অবস্থার জন্য এই শেয়ার দেখা হয় না। তৎক্ষণাৎ সাহায্যের জন্য চিকিৎসা দলের সঙ্গে যোগাযোগ করুন বা 112 নম্বরে ফোন করুন।",
        "confirmShare" to "সম্মতি দিয়ে ভাগ করুন", "history" to "ভাগ করার ইতিহাস", "revoke" to "অনুমতি প্রত্যাহার করুন", "signOut" to "সাইন আউট"
    ),
    "ta" to mapOf(
        "accountTitle" to "உங்கள் கணக்கு, உங்கள் தேர்வு", "accountLead" to "சந்திப்புச் சுருக்கத்தைப் பகிர அல்லது சிகிச்சைக் குழுவுடன் செய்திகளையும் கோப்புகளையும் பரிமாற உள்நுழையுங்கள். கணக்கு இல்லாமலும் தனிப்பட்ட உரையாடல் கிடைக்கும்.",
        "signIn" to "உள்நுழை", "register" to "பதிவு செய்", "name" to "பெயர் அல்லது டெமோ அடையாளம்", "contact" to "மின்னஞ்சல் அல்லது டெமோ தொடர்பு", "password" to "கடவுச்சொல்",
        "shareTitle" to "சந்திப்புச் சுருக்கத்தைப் பகிருங்கள்", "shareLead" to "அனுப்புவதற்கு முன் சரிபாருங்கள். தனிப்பட்ட உரையாடலும் குரல் பதிவும் தானாகப் பகிரப்படாது.",
        "chooseDoctor" to "1 · மருத்துவரைத் தேர்வுசெய்க", "editBrief" to "2 · சுருக்கத்தை எழுதுக", "brief" to "சந்திப்பிற்கான உங்கள் சுருக்கம்",
        "chooseQuestions" to "கேள்விகளைத் தேர்வுசெய்க", "personalNote" to "விருப்பத் தனிப்பட்ட குறிப்பு", "preview" to "3 · பார்த்து ஒப்புதல் அளிக்கவும்",
        "consent" to "அடுத்த சந்திப்பிற்காக இந்தச் சுருக்கத்தைத் தேர்ந்தெடுத்த மருத்துவருடன் பகிர ஒப்புக்கொள்கிறேன்.",
        "emergency" to "அவசர உதவிக்காக இந்தப் பகிர்வு கண்காணிக்கப்படாது. உடனடி உதவிக்கு உங்கள் சிகிச்சைக் குழுவைத் தொடர்புகொள்ளுங்கள் அல்லது 112 ஐ அழையுங்கள்.",
        "confirmShare" to "ஒப்புதல் அளித்துப் பகிர்", "history" to "பகிர்வு வரலாறு", "revoke" to "அணுகலைத் திரும்பப்பெறு", "signOut" to "வெளியேறு"
    )
)

private fun patientLabel(lang: String, key: String, english: String): String = patientFlowCopy[lang]?.get(key) ?: english

private fun displayIst(value: String): String = runCatching {
    OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
        .format(DateTimeFormatter.ofPattern("dd MMM, h:mm a 'IST'", Locale.ENGLISH))
}.getOrDefault(value)

@Composable
private fun FlowShell(model: SahayViewModel, eyebrow: String, title: String, subtitle: String, back: (() -> Unit)?,
    onSettings: (() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit) {
    val reportHeight = LocalActionAreaHeight.current
    if (bottomBar != null) DisposableEffect(reportHeight) { onDispose { reportHeight(0) } }
    Column(Modifier.fillMaxSize().background(CalmBackground)) {
        CalmTopBar("Sahay", model, back, settings = if(model.screen == Screen.CLINICIAN_QUEUE) null else onSettings)
        Column(Modifier.weight(1f).fillMaxWidth().widthIn(max = 600.dp).align(Alignment.CenterHorizontally)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(eyebrow, color = SoftTeal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(title, color = MainText, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = SecondaryText, fontSize = 16.sp, lineHeight = 24.sp)
            }
            if (model.accountError.isNotBlank()) {
                Surface(color = Warm, shape = RoundedCornerShape(16.dp)) {
                    Text(model.accountError, Modifier.fillMaxWidth().padding(16.dp), color = WarmText, fontSize = 14.sp)
                }
            }
            if (model.accountBusy) Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = DeepTeal)
                Spacer(Modifier.width(8.dp))
                Text("Working…", color = SecondaryText, fontSize = 14.sp)
            }
            content()
        }
        if (bottomBar != null) {
            HorizontalDivider(color = CalmSoft)
            Column(Modifier.fillMaxWidth().onSizeChanged { reportHeight(it.height) }.padding(horizontal = 20.dp, vertical = 12.dp)) { bottomBar() }
        }
    }
}

@Composable
private fun FlowCard(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = androidx.compose.ui.graphics.Color.White) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { content() } }
}

@Composable
private fun FlowButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Ember.Blush, contentColor = Ember.Onyx,
            disabledContainerColor = Ember.SandyCream, disabledContentColor = Ember.WarmGray),
        border = BorderStroke(1.5.dp, Ember.BurntSienna.copy(alpha = if (enabled) .5f else .2f)), shape = RoundedCornerShape(20.dp)) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String = "Password") {
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), singleLine = true)
}

@Composable
fun PatientAccountScreen(model: SahayViewModel, onSettings: () -> Unit) {
    var register by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    FlowShell(model, "PATIENT ACCOUNT · मरीज़ खাতা", patientLabel(model.language, "accountTitle", "Your account, your choice"),
        patientLabel(model.language, "accountLead", "Sign in to share a visit brief or exchange messages and files with your care team. Private chat stays available without an account."),
        back = model::closeAccount, onSettings = onSettings,
        bottomBar = if (model.accountRole == "patient") null else { {
            FlowButton(if (register) patientLabel(model.language, "register", "Create patient account") else patientLabel(model.language, "signIn", "Sign in"),
                !model.accountBusy && contact.isNotBlank() && password.isNotBlank() && (!register || (name.isNotBlank() && password.length >= 10))) {
                model.signIn(contact, password, register, name)
            }
        } }) {
        if (model.accountRole == "patient") {
            FlowCard {
                Icon(Icons.Outlined.PersonOutline, null, tint = DeepTeal)
                Text("Signed in as ${model.accountName}", color = MainText, fontWeight = FontWeight.SemiBold)
                Text("Only visit briefs you explicitly confirm are shared. Your raw chat and voice recordings are not included.", color = SecondaryText, fontSize = 14.sp)
                FlowButton("Share visit brief") { model.openShare() }
                OutlinedButton(onClick = model::signOut, modifier = Modifier.fillMaxWidth()) { Text(patientLabel(model.language, "signOut", "Sign out")) }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!register, { register = false; model.clearAccountError() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text(patientLabel(model.language, "signIn", "Sign in")) })
                FilterChip(register, { register = true; model.clearAccountError() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text(patientLabel(model.language, "register", "Register")) })
            }
            FlowCard {
                if (register) OutlinedTextField(name, { name = it.take(100) }, Modifier.fillMaxWidth(), label = { Text(patientLabel(model.language, "name", "Name or demo ID")) }, singleLine = true)
                OutlinedTextField(contact, { contact = it.take(160) }, Modifier.fillMaxWidth(), label = { Text(patientLabel(model.language, "contact", "Email or demo contact")) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
                PasswordField(password, { password = it.take(128) }, patientLabel(model.language, "password", "Password"))
                Text("Use at least 10 characters. This course app uses fictional demo accounts.", color = SecondaryText, fontSize = 14.sp)
            }
            TextButton(onClick = model::begin, modifier = Modifier.fillMaxWidth()) { Text("Continue private chat without account") }
        }
        Surface(color = SoftMint, shape = RoundedCornerShape(12.dp)) {
            Text("Urgent help is always available without sign-in. This app does not watch conversations or shares for emergencies.",
                Modifier.padding(16.dp), color = SecondaryText, fontSize = 14.sp)
        }
    }
}

@Composable
fun ClinicianAuthScreen(model: SahayViewModel, onSettings: () -> Unit) {
    var invite by remember { mutableStateOf(false) }
    var contact by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    FlowShell(model, "CLINICIAN DEMO · चिकित्सक", "Care team sign in",
        "Invited demo accounts can review shared visit briefs and exchange messages and files with demo patients. This is not an emergency inbox.",
        back = model::closeAccount, onSettings = onSettings, bottomBar = {
            FlowButton(if (invite) "Activate invited account" else "Open review queue",
                !model.accountBusy && password.isNotBlank() && (if (invite) code.isNotBlank() && password.length >= 10 else contact.isNotBlank())) {
                if (invite) model.acceptClinicianInvite(code, password) else model.signIn(contact, password, clinician = true)
            }
        }) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!invite, { invite = false; model.clearAccountError() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("Sign in") })
            FilterChip(invite, { invite = true; model.clearAccountError() }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text("Accept invite") })
        }
        FlowCard {
            Icon(Icons.Outlined.Lock, null, tint = DeepTeal)
            if (invite) OutlinedTextField(code, { code = it.take(200) }, Modifier.fillMaxWidth(), label = { Text("Demo invitation code") }, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            else OutlinedTextField(contact, { contact = it.take(160) }, Modifier.fillMaxWidth(), label = { Text("Work email or demo contact") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            PasswordField(password, { password = it.take(128) }, if (invite) "Create password" else "Password")
        }
        Text("For this course demo, ask the demo admin for a new invitation or password reset. No hospital or regulator has verified these accounts.",
            color = SecondaryText, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun ShareVisitBriefScreen(model: SahayViewModel, onSettings: () -> Unit) {
    var clinicianId by remember { mutableStateOf("") }
    var summary by remember(model.visitBriefDraft) { mutableStateOf(model.visitBriefDraft) }
    var note by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    var revokeId by remember { mutableStateOf<String?>(null) }
    val selectedQuestions = remember { mutableStateListOf<Int>() }
    FlowShell(model, "PATIENT CONTROLLED SHARING", patientLabel(model.language, "shareTitle", "Share a visit brief"),
        patientLabel(model.language, "shareLead", "Preview exactly what will be sent. Your private chat and voice are never included automatically."),
        back = { model.navigate(Screen.QUESTIONS) }, onSettings = onSettings) {
        FlowCard {
            Text(patientLabel(model.language, "chooseDoctor", "1 · Choose a clinician"), color = DeepTeal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            if (model.clinicians.isEmpty()) Text("No invited demo clinician is available yet.", color = SecondaryText, fontSize = 14.sp)
            model.clinicians.forEach { doctor ->
                val id = doctor.optString("id")
                Surface(Modifier.fillMaxWidth().clickable { clinicianId = id }, shape = RoundedCornerShape(16.dp),
                    color = if (clinicianId == id) SoftMint else Canvas,
                    border = BorderStroke(if (clinicianId == id) 2.dp else 1.dp, if (clinicianId == id) DeepTeal else Hairline)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PersonOutline, null, tint = DeepTeal)
                        Spacer(Modifier.width(8.dp))
                        Text(doctor.optString("name"), Modifier.weight(1f), color = MainText, fontWeight = FontWeight.SemiBold)
                        if (clinicianId == id) Icon(Icons.Outlined.Check, null, tint = DeepTeal)
                    }
                }
            }
        }
        FlowCard {
            Text(patientLabel(model.language, "editBrief", "2 · Edit the brief"), color = DeepTeal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(summary, { summary = it.take(1200) }, Modifier.fillMaxWidth(),
                label = { Text(patientLabel(model.language, "brief", "Your brief for the next visit")) }, minLines = 3,
                supportingText = { Text("Write only what you want the clinician to see. You approve the final text.") })
            Text(patientLabel(model.language, "chooseQuestions", "Choose questions to include"), color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (model.questions.isEmpty()) Text("Add questions on the Questions screen first, or write a brief above.", color = SecondaryText, fontSize = 14.sp)
            model.questions.forEachIndexed { index, question ->
                Row(Modifier.fillMaxWidth().clickable { if (selectedQuestions.contains(index)) selectedQuestions.remove(index) else selectedQuestions.add(index) },
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = selectedQuestions.contains(index), onCheckedChange = {
                        if (it) { if (!selectedQuestions.contains(index)) selectedQuestions.add(index) } else selectedQuestions.remove(index)
                    })
                    Text(question.text, Modifier.weight(1f), color = MainText, fontSize = 14.sp)
                }
            }
            OutlinedTextField(note, { note = it.take(500) }, Modifier.fillMaxWidth(), label = { Text(patientLabel(model.language, "personalNote", "Optional personal note")) }, minLines = 2)
        }
        FlowCard {
            Text(patientLabel(model.language, "preview", "3 · Preview and consent"), color = DeepTeal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("Brief: ${summary.ifBlank { "No brief text" }}", color = MainText, fontSize = 14.sp)
            selectedQuestions.sorted().forEach { index ->
                model.questions.getOrNull(index)?.let { Text("• ${it.text}", color = MainText, fontSize = 14.sp) }
            }
            if (note.isNotBlank()) Text("Personal note: $note", color = MainText, fontSize = 14.sp)
            Row(Modifier.fillMaxWidth().clickable { consent = !consent }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(consent, { consent = it })
                Text(patientLabel(model.language, "consent", "I choose to share this snapshot with the selected clinician for my next visit."), Modifier.weight(1f), color = MainText, fontSize = 14.sp)
            }
            Surface(color = Warm, shape = RoundedCornerShape(16.dp)) {
                Text(patientLabel(model.language, "emergency", "This share is not monitored for emergencies. For urgent help, contact your care team or call 112."),
                    Modifier.padding(12.dp), color = WarmText, fontSize = 14.sp)
            }
            FlowButton(patientLabel(model.language, "confirmShare", "Confirm and share"), !model.accountBusy && consent && clinicianId.isNotBlank() && (summary.isNotBlank() || selectedQuestions.isNotEmpty())) {
                model.shareBrief(clinicianId, summary, selectedQuestions.sorted().mapNotNull { model.questions.getOrNull(it)?.text }, note, consent)
            }
        }
        if (model.myShares.isNotEmpty()) FlowCard {
            Text(patientLabel(model.language, "history", "Share history"), color = DeepTeal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            model.myShares.forEach { share ->
                val revoked = !share.isNull("revoked_at")
                Text("${displayIst(share.optString("created_at"))} · ${if (revoked) "Revoked" else share.optString("status").replace('_', ' ')}",
                    color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(share.optString("summary").ifBlank { "Questions shared" }, color = SecondaryText, fontSize = 14.sp)
                if (!revoked) OutlinedButton(onClick = { revokeId = share.optString("id") }) { Text(patientLabel(model.language, "revoke", "Revoke access")) }
            }
        }
        OutlinedButton(onClick = model::signOut, modifier = Modifier.fillMaxWidth()) { Text(patientLabel(model.language, "signOut", "Sign out")) }
    }
    if (revokeId != null) AlertDialog(onDismissRequest = { revokeId = null },
        title = { Text("Revoke this share?") },
        text = { Text("It will disappear from the clinician queue. A clinician who already read or copied it may retain that information.") },
        confirmButton = { TextButton(onClick = { model.revokeShare(revokeId!!); revokeId = null }) { Text("Revoke") } },
        dismissButton = { TextButton(onClick = { revokeId = null }) { Text("Cancel") } })
}

@Composable
fun ClinicianQueueScreen(model: SahayViewModel, onSettings: () -> Unit) {
    var filter by remember { mutableStateOf("all") }
    var search by remember { mutableStateOf("") }
    val rows = model.clinicianQueue.filter {
        (filter == "all" || it.optString("status") == filter) &&
            (search.isBlank() || it.optString("patient_name").contains(search, ignoreCase = true) ||
                it.optString("brief_preview").contains(search, ignoreCase = true))
    }
    FlowShell(model, "CLINICIAN REVIEW · DEMO", "Shared visit briefs",
        "Only patient-approved snapshots appear here. This is not monitored for emergencies.",
        back = null, onSettings = onSettings) {
        FlowCard {
            Text("Hello, ${model.accountName}", color = DeepTeal, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            Text("New ${model.clinicianQueue.count { it.optString("status") == "new" }}  ·  In review ${model.clinicianQueue.count { it.optString("status") == "in_review" }}  ·  Reviewed ${model.clinicianQueue.count { it.optString("status") == "reviewed" }}",
                color = SecondaryText, fontSize = 14.sp)
            OutlinedButton(onClick = model::loadClinicianQueue, enabled = !model.accountBusy) { Text("Refresh queue") }
        }
        FlowButton("Patient messages and files") { model.openCare() }
        OutlinedTextField(search, { search = it.take(100) }, Modifier.fillMaxWidth(),
            label = { Text("Search patient or brief") }, singleLine = true)
        listOf("all" to "All", "new" to "New", "in_review" to "In review", "reviewed" to "Reviewed").chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { (id, title) ->
                    FilterChip(filter == id, { filter = id }, modifier = Modifier.weight(1f).heightIn(min = 48.dp), label = { Text(title, fontSize = 16.sp) })
                }
            }
        }
        if (rows.isEmpty()) FlowCard { Text("No patient-shared briefs in this view.", color = SecondaryText, fontSize = 14.sp) }
        rows.forEach { share ->
            FlowCard {
                Text(share.optString("patient_name"), color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("${share.optString("language").uppercase()} · ${displayIst(share.optString("created_at"))} · ${share.optString("status").replace('_', ' ')}",
                    color = SecondaryText, fontSize = 14.sp)
                if (share.optString("brief_preview").isNotBlank()) Text(share.optString("brief_preview"), color = SecondaryText, fontSize = 14.sp)
                FlowButton("Review brief") { model.openClinicianShare(share.optString("id")) }
            }
        }
        OutlinedButton(onClick = model::signOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
    }
}

@Composable
fun ClinicianDetailScreen(model: SahayViewModel, onSettings: () -> Unit) {
    val share = model.selectedShare
    var note by remember(share?.optString("id")) { mutableStateOf(share?.optString("clinician_note") ?: "") }
    FlowShell(model, "PATIENT SHARED CONTENT", share?.optString("patient_name") ?: "Visit brief",
        "Patient-approved snapshot. Verify AI-assisted wording and medical details with the patient.",
        back = { model.navigate(Screen.CLINICIAN_QUEUE); model.loadClinicianQueue() }, onSettings = onSettings) {
        if (share == null) {
            FlowCard { Text("Brief unavailable or access revoked.", color = SecondaryText) }
        } else {
            FlowCard {
                Text("Consent recorded ${displayIst(share.optString("created_at"))}", color = SecondaryText, fontSize = 14.sp)
                Text("Status: ${share.optString("status").replace('_', ' ')}", color = DeepTeal, fontWeight = FontWeight.SemiBold)
                if (!share.isNull("reviewed_at")) Text("Reviewed ${displayIst(share.optString("reviewed_at"))} by ${model.accountName}",
                    color = SecondaryText, fontSize = 14.sp)
                Text("Patient-approved draft · verify with patient", color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(share.optString("summary").ifBlank { "No brief text provided" }, color = MainText, fontSize = 16.sp, lineHeight = 23.sp)
            }
            FlowCard {
                Text("Questions for this visit", color = DeepTeal, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                val questions = share.optJSONArray("questions")
                if (questions == null || questions.length() == 0) Text("No questions selected", color = SecondaryText, fontSize = 14.sp)
                else for (i in 0 until questions.length()) Text("${i + 1}. ${questions.optString(i)}", color = MainText, fontSize = 14.sp)
                if (share.optString("note").isNotBlank()) Text("Patient note: ${share.optString("note")}", color = SecondaryText, fontSize = 14.sp)
            }
            FlowCard {
                Text("Clinician-authored note", color = DeepTeal, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                OutlinedTextField(note, { note = it.take(1000) }, Modifier.fillMaxWidth(), minLines = 3,
                    label = { Text("Your note") }, supportingText = { Text("Visible only in the clinician review record.") })
                OutlinedButton(onClick = { model.reviewShare(share.optString("id"), "in_review", note) }, enabled = !model.accountBusy,
                    modifier = Modifier.fillMaxWidth()) { Text("Mark in review") }
                FlowButton("Mark reviewed", !model.accountBusy) { model.reviewShare(share.optString("id"), "reviewed", note) }
            }
        }
    }
}

@Composable
fun AdminBookkeepingScreen(model: SahayViewModel, onSettings: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var deactivateId by remember { mutableStateOf<String?>(null) }
    FlowShell(model, "DEMO ADMIN · BOOKKEEPING", "Accounts and access",
        "Manage fictional clinician invitations and account status. Private unshared chats are never visible here.",
        back = null, onSettings = onSettings) {
        FlowCard {
            Text("Invite clinician", color = DeepTeal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(name, { name = it.take(100) }, Modifier.fillMaxWidth(), label = { Text("Clinician demo name") })
            OutlinedTextField(contact, { contact = it.take(160) }, Modifier.fillMaxWidth(), label = { Text("Demo email or contact") })
            FlowButton("Create invitation", !model.accountBusy && name.isNotBlank() && contact.isNotBlank()) { model.inviteClinician(name, contact) }
            if (model.inviteCode.isNotBlank()) {
                Text("One-time invite code: ${model.inviteCode}", color = MainText, fontSize = 14.sp)
                OutlinedButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Sahay demo invite", model.inviteCode))
                    model.note("Invite code copied")
                }) { Text("Copy invite code") }
            }
        }
        FlowCard {
            Text("Accounts", color = DeepTeal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = model::loadAdminUsers, enabled = !model.accountBusy) { Text("Refresh") }
            model.adminUsers.forEach { user ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(user.optString("name"), color = MainText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${user.optString("role")} · ${if (user.optBoolean("active")) "active" else "inactive"} · ${user.optString("contact")}",
                            color = SecondaryText, fontSize = 14.sp)
                    }
                    if (user.optBoolean("active") && user.optString("role") != "admin")
                        TextButton(onClick = { deactivateId = user.optString("id") }) { Text("Deactivate", color = WarmText, fontSize = 14.sp) }
                }
            }
        }
        FlowCard {
            Text("Recent access history", color = DeepTeal, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            if (model.auditEvents.isEmpty()) Text("No events yet", color = SecondaryText, fontSize = 14.sp)
            model.auditEvents.take(20).forEach { event ->
                Text("${displayIst(event.optString("at"))} · ${event.optString("event")} · ${event.optString("object_type")}",
                    color = SecondaryText, fontSize = 14.sp)
            }
        }
        OutlinedButton(onClick = model::signOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
    }
    if (deactivateId != null) AlertDialog(onDismissRequest = { deactivateId = null },
        title = { Text("Deactivate account?") },
        text = { Text("The account will lose access immediately. This can be reversed later by a demo administrator using the database.") },
        confirmButton = { TextButton(onClick = { model.deactivateUser(deactivateId!!); deactivateId = null }) { Text("Deactivate") } },
        dismissButton = { TextButton(onClick = { deactivateId = null }) { Text("Cancel") } })
}

package org.sahay.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Mint = androidx.compose.ui.graphics.Color(0xFFF1FCFA)
private val Teal = androidx.compose.ui.graphics.Color(0xFF003C3A)
private val TealSoft = androidx.compose.ui.graphics.Color(0xFF1D5351)
private val Ink = androidx.compose.ui.graphics.Color(0xFF131D1C)
private val Muted = androidx.compose.ui.graphics.Color(0xFF4A5553)
private val Pale = androidx.compose.ui.graphics.Color(0xFFE6F2F0)
private val Peach = androidx.compose.ui.graphics.Color(0xFFFFDBD0)
private val Rust = androidx.compose.ui.graphics.Color(0xFF97472E)
private val Rose = androidx.compose.ui.graphics.Color(0xFFF9ECE8)
private val Line = androidx.compose.ui.graphics.Color(0xFFDCE8E5)

@Composable
fun SahayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = CalmInk, onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = CalmInk, secondaryContainer = CalmSoft, onSecondaryContainer = CalmInk, background = CalmBackground, surface = androidx.compose.ui.graphics.Color.White,
            onSurface = CalmInk, onSurfaceVariant = CalmMuted, outline = androidx.compose.ui.graphics.Color(0xFF718C85)),
        typography = Typography(),
        content = content
    )
}

@Composable
fun SahayApp(
    model: SahayViewModel,
    onRecord: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDial: (String) -> Unit,
    onToast: (String) -> Unit,
    onAttachFile: () -> Unit,
    onOpenFile: (org.json.JSONObject) -> Unit
) {
    val snackbars = remember { SnackbarHostState() }
    var actionHeightPx by remember { mutableIntStateOf(0) }
    val reportHeight = remember { { value: Int -> actionHeightPx = value } }
    val actionHeight = with(LocalDensity.current) { actionHeightPx.toDp() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    androidx.activity.compose.BackHandler(enabled = model.screen !in setOf(Screen.WELCOME, Screen.HOME, Screen.CLINICIAN_QUEUE, Screen.ADMIN, Screen.RESTORING)) {
        when (model.screen) {
            Screen.SETTINGS -> model.closeSettings()
            Screen.HELP -> model.closeHelp()
            Screen.PATIENT_ACCOUNT, Screen.CLINICIAN_AUTH -> model.closeAccount()
            Screen.ACTIVITY -> model.navigate(Screen.PATHS)
            Screen.THREAD -> model.openCare()
            else -> model.navigateHome()
        }
    }
    val notice = model.notice
    LaunchedEffect(notice) {
        if (notice != null) { snackbars.showSnackbar(notice); model.dismissNotice() }
    }
    LaunchedEffect(model.screen, model.exerciseRunning) {
        while (model.screen == Screen.EXERCISE && model.exerciseRunning) {
            delay(1000)
            model.tickExercise()
        }
    }
    LaunchedEffect(model.screen) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    if (model.hidden) {
        LaunchedEffect(Unit) { onStopSpeaking(); AmbientAudio.stop() }
        PrivacyCover(model)
        return
    }

    val accountScreen = model.screen in setOf(Screen.RESTORING, Screen.PATIENT_ACCOUNT, Screen.CLINICIAN_AUTH, Screen.SHARE,
        Screen.CLINICIAN_QUEUE, Screen.CLINICIAN_DETAIL, Screen.ADMIN, Screen.SETTINGS, Screen.CARE, Screen.THREAD, Screen.ACTIVITY, Screen.AGENT_INSPECT)
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    CompositionLocalProvider(LocalActionAreaHeight provides reportHeight) {
    Scaffold(
        containerColor = CalmBackground,
        snackbarHost = { SnackbarHost(snackbars, Modifier.windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars)).padding(bottom = actionHeight)) },
        topBar = {
            if (model.screen != Screen.WELCOME && !accountScreen) SahayHeader(model, onStopSpeaking, onSettings = model::openSettings)
        },
        bottomBar = {
            val patientTabs = model.onboarded && model.accountRole !in setOf("clinician", "admin") &&
                model.screen in setOf(Screen.HOME, Screen.CHAT, Screen.PATHS, Screen.CARE, Screen.SETTINGS, Screen.INFO, Screen.QUESTIONS)
            val clinicianTabs = model.accountRole == "clinician" && model.screen in setOf(Screen.CLINICIAN_QUEUE, Screen.CARE, Screen.SETTINGS)
            if (!keyboardOpen && (patientTabs || clinicianTabs)) BottomNavigation(model)
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            when (model.screen) {
                Screen.WELCOME -> CalmArrival(model)
                Screen.HOME -> TodayHome(model)
                Screen.ACTIVITY -> ActivityScreen(model, onSpeak, onStopSpeaking)
                Screen.CARE -> CareInbox(model)
                Screen.THREAD -> CareConversation(model, onAttachFile, onOpenFile)
                Screen.SETTINGS -> SettingsPage(model, onStopSpeaking)
                Screen.AGENT_INSPECT -> AgentInspector(model)
                Screen.RESTORING -> SessionRestoringScreen(model)
                Screen.CHAT -> ChatScreen(model, onRecord, onSpeak, onOpenUrl)
                Screen.PATHS -> ActivityLibrary(model)
                Screen.EXERCISE -> ExerciseScreen(model)
                Screen.INFO -> InfoScreen(model, onOpenUrl)
                Screen.QUESTIONS -> QuestionsScreen(model, onToast)
                Screen.HELP -> HelpScreen(model, onDial, onOpenUrl, onToast)
                Screen.PATIENT_ACCOUNT -> PatientAccountScreen(model) { model.openSettings() }
                Screen.CLINICIAN_AUTH -> ClinicianAuthScreen(model) { model.openSettings() }
                Screen.SHARE -> ShareVisitBriefScreen(model) { model.openSettings() }
                Screen.CLINICIAN_QUEUE -> ClinicianQueueScreen(model) { model.openSettings() }
                Screen.CLINICIAN_DETAIL -> ClinicianDetailScreen(model) { model.openSettings() }
                Screen.ADMIN -> AdminBookkeepingScreen(model) { model.openSettings() }
            }
        }
    }

    }

    if (model.transcriptDraft != null) TranscriptDialog(model, onRecord, onSpeak)

}

@Composable
private fun SahayHeader(model: SahayViewModel, onStopSpeaking: () -> Unit, onSettings: () -> Unit) {
    CalmTopBar("Sahay", model, back = when(model.screen) {
        Screen.HELP -> model::closeHelp
        Screen.INFO, Screen.QUESTIONS, Screen.EXERCISE -> model::navigateHome
        else -> null
    }, modifier = Modifier.background(CalmBackground).statusBarsPadding())
}

@Composable
private fun BottomNavigation(model: SahayViewModel) {
    val choices = if(model.accountRole == "clinician") listOf(
        Triple(Screen.CLINICIAN_QUEUE, Icons.Outlined.MenuBook, wellbeingCopy(model.language, "Review")),
        Triple(Screen.CARE, Icons.Outlined.ChatBubbleOutline, wellbeingCopy(model.language, "Messages")),
        Triple(Screen.SETTINGS, Icons.Outlined.Settings, wellbeingText(model.language, "settings"))
    ) else listOf(
        Triple(Screen.HOME, Icons.Outlined.Home, wellbeingText(model.language, "home")),
        Triple(Screen.CHAT, Icons.Outlined.ChatBubbleOutline, wellbeingText(model.language, "talk")),
        Triple(Screen.PATHS, Icons.Outlined.Palette, wellbeingText(model.language, "activities")),
        Triple(Screen.CARE, Icons.Outlined.PersonOutline, when (model.language) { "hi" -> "देखभाल"; "bn" -> "চিকিৎসা"; "ta" -> "சிகிச்சை"; else -> "Care" }),
        Triple(Screen.SETTINGS, Icons.Outlined.Settings, wellbeingText(model.language, "settings")))
    androidx.compose.material3.NavigationBar(containerColor = CalmBackground) {
        choices.forEach { (screen,icon,label) ->
            val fullLabel = if (model.accountRole == "clinician") label else wellbeingText(model.language, when(screen) {
                Screen.HOME -> "home"; Screen.CHAT -> "talk"; Screen.PATHS -> "activities"; Screen.CARE -> "care"; else -> "settings"
            })
            val compactLabel = if(model.accountRole == "clinician") label else when(model.language) {
                "ta" -> when(screen) { Screen.CHAT -> "பேசு"; Screen.PATHS -> "செயல்"; Screen.CARE -> "குழு"; Screen.SETTINGS -> "அமைவு"; else -> label }
                "hi" -> if(screen == Screen.PATHS) "खोजें" else label
                "bn" -> if(screen == Screen.PATHS) "খুঁজুন" else label
                else -> label
            }
            NavigationBarItem(selected = model.screen == screen, onClick = {
                if(screen == Screen.CARE) model.openCare() else if(screen == Screen.SETTINGS) model.openSettings() else model.navigate(screen)
            }, icon = { Icon(icon, fullLabel) }, label = { Text(compactLabel, modifier = Modifier.clearAndSetSemantics { contentDescription = fullLabel }, fontSize = 12.sp, maxLines = 1, textAlign = TextAlign.Center) },
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(selectedIconColor = CalmInk, selectedTextColor = CalmInk, indicatorColor = CalmSoft, unselectedIconColor = CalmMuted, unselectedTextColor = CalmMuted))
        }
    }
}

@Composable
private fun SurfaceCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = androidx.compose.ui.graphics.Color.White,
        shadowElevation = 2.dp, border = BorderStroke(1.dp, Line), content = content)
}

@Composable
private fun SoftPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Pale, content = content)
}

@Composable
private fun Notice(text: String, urgent: Boolean = false, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = if (urgent) Rose else androidx.compose.ui.graphics.Color.White) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(if (urgent) Icons.Outlined.HelpOutline else Icons.Outlined.Info, null, Modifier.size(19.dp), tint = if (urgent) Rust else Teal)
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 14.sp, lineHeight = 19.sp, color = if (urgent) Rust else Ink)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Ink, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: @Composable (() -> Unit)? = null) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Teal, contentColor = androidx.compose.ui.graphics.Color.White)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        if (icon != null) { Spacer(Modifier.width(6.dp)); icon() }
    }
}

@Composable
private fun ToggleRow(title: String, hint: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(hint, color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun StatusBand(model: SahayViewModel) {
    val text = if (!model.backendConnected) nativeText(model.language, "offline") else if (model.liveModel) model.t("statusLive") else model.t("statusDemo")
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), color = Pale) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (model.backendConnected) Teal else Rust))
            Spacer(Modifier.width(8.dp))
            Text(text, color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ChatScreen(model: SahayViewModel, onRecord: () -> Unit, onSpeak: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    val listState = rememberLazyListState()
    val reportActionHeight = LocalActionAreaHeight.current
    DisposableEffect(reportActionHeight) { onDispose { reportActionHeight(0) } }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val submit = {
        if (model.chatDraft.isNotBlank()) {
            model.send(model.chatDraft, onSpeak)
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }
    LaunchedEffect(model.messages.size, model.busy) {
        val last = if (model.messages.isEmpty()) 0 else model.messages.size - 1 + if (model.busy) 1 else 0
        listState.animateScrollToItem(last)
    }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusBand(model)
            SoftPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Shield, null, tint = Teal, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(model.t("aiLabel").uppercase(), color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(model.t("boundary"), color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
        }
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (model.messages.isEmpty()) {
                item { MessageBubble(ChatMessage(model.t("intro"), false, ""), model, onSpeak, onOpenUrl) }
            } else {
                itemsIndexed(model.messages) { _, message -> MessageBubble(message, model, onSpeak, onOpenUrl) }
            }
            if (model.chatSuggestions.isNotEmpty() && !model.busy) item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    model.chatSuggestions.forEach { suggestion ->
                        OutlinedButton(onClick = {
                            when (suggestion.optString("screen")) {
                                "activity" -> model.openActivity(suggestion.optString("id"))
                                "share" -> model.openShare()
                                "questions" -> { model.messages.lastOrNull()?.text?.lines()?.filter { it.isNotBlank() }?.take(2)?.forEach(model::addQuestion); model.navigate(Screen.QUESTIONS) }
                            }
                        }) { Text(suggestion.optString("label")) }
                    }
                }
            }
            if (model.speechProcessing) item { Text("Transcribing your recording…", color = Muted) }
            if (model.busy) item { Text(model.t("thinking"), color = Muted, fontSize = 14.sp) }
        }
        Column(Modifier.fillMaxWidth().onSizeChanged { reportActionHeight(it.height) }.background(CalmBackground).padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = model.chatDraft, onValueChange = { model.chatDraft = it.take(4000) },
                    modifier = Modifier.weight(1f), placeholder = { Text(model.t("typePlaceholder"), fontSize = 16.sp) },
                    shape = RoundedCornerShape(16.dp), maxLines = 4, minLines = 1, enabled = !model.busy,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }))
                IconButton(onRecord, enabled = !model.busy && !model.speechProcessing, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Mic, model.t("mic"), tint = CalmInk)
                }
                Button(submit, enabled = model.chatDraft.isNotBlank() && !model.busy, modifier = Modifier.size(56.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Outlined.Send, model.t("send"))
                }
            }
            Text(model.t("transcriptHint"), color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, model: SahayViewModel, onSpeak: (String) -> Unit, onOpenUrl: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (message.fromUser) Alignment.End else Alignment.Start) {
        Text(if (message.fromUser) "${model.t("chat")} · ${message.time}" else "Sahay · AI${if (message.time.isNotBlank()) " · ${message.time}" else ""}",
            color = if (message.fromUser) Muted else Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = if (message.fromUser) TealSoft else androidx.compose.ui.graphics.Color.White,
            border = if (message.fromUser) null else BorderStroke(1.dp, Line), modifier = Modifier.fillMaxWidth(if (message.fromUser) 0.82f else 0.92f)) {
            Column(Modifier.padding(16.dp)) {
                Text(message.text, color = if (message.fromUser) androidx.compose.ui.graphics.Color.White else Ink, fontSize = 14.sp, lineHeight = 21.sp)
                if (!message.sourceUrl.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(message.sourceTitle ?: model.t("source"), modifier = Modifier.heightIn(min = 48.dp).clickable { message.sourceUrl?.let(onOpenUrl) }.padding(vertical = 12.dp),
                        color = Teal, fontSize = 14.sp, textDecoration = TextDecoration.Underline)
                }
            }
        }
        if (!message.fromUser) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onSpeak(message.text) }) {
                    Icon(Icons.Outlined.PlayArrow, null, Modifier.size(16.dp))
                    Text(model.t("listen"), fontSize = 14.sp)
                }
                if (message.route == "crisis" || message.route == "urgent_medical") {
                    TextButton(onClick = { model.navigate(Screen.HELP) }) { Text(model.t("urgentHelp"), fontSize = 14.sp, color = Rust) }
                }
            }
        }
    }
}

@Composable
private fun PathsScreen(model: SahayViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusBand(model)
        SurfaceCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(model.t("aiLabel").uppercase(), color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(model.t("pathsTitle"), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text(model.t("pathsLead"), color = Muted, fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
        val options = listOf(
            Triple("reflectTitle", "reflectBody", Screen.CHAT),
            Triple("copingTitle", "copingBody", Screen.EXERCISE),
            Triple("infoTitle", "infoBody", Screen.INFO),
            Triple("questionsTitle", "questionsBody", Screen.QUESTIONS)
        )
        options.forEachIndexed { index, (title, body, destination) ->
            SurfaceCard(Modifier.fillMaxWidth().clickable {
                if (destination == Screen.EXERCISE) model.startExercise() else model.navigate(destination)
            }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Pale) {
                        Box(Modifier.size(39.dp), contentAlignment = Alignment.Center) {
                            Icon(when (index) {
                                0 -> Icons.Outlined.ChatBubbleOutline; 1 -> Icons.Outlined.FavoriteBorder
                                2 -> Icons.Outlined.MenuBook; else -> Icons.Outlined.Check
                            }, null, tint = Teal)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(model.t(title), color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(model.t(body), color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = Teal)
                }
            }
        }
        SoftPanel(Modifier.fillMaxWidth()) { Text(model.t("boundary"), Modifier.padding(16.dp), color = Muted, fontSize = 14.sp, lineHeight = 18.sp) }
    }
}

@Composable
private fun ExerciseScreen(model: SahayViewModel) {
    val elapsed = 120 - model.exerciseRemaining
    val step = (elapsed / 30).coerceIn(0, 3)
    val timer = "${model.exerciseRemaining / 60}:${(model.exerciseRemaining % 60).toString().padStart(2, '0')}"
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(model.t("copingTitle"), Modifier.weight(1f), color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { model.navigate(Screen.CHAT) }) { Text(model.t("exerciseStop"), color = Rust, fontSize = 14.sp) }
        }
        Text(model.t("exerciseTitle"), fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 33.sp, color = Ink)
        SoftPanel(Modifier.fillMaxWidth()) { Text(model.t("exerciseHint"), Modifier.padding(16.dp), color = Muted, fontSize = 14.sp, lineHeight = 19.sp) }
        Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(220.dp).border(12.dp, Pale, CircleShape).padding(12.dp).border(2.dp, Teal, CircleShape), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.FavoriteBorder, null, Modifier.size(28.dp), tint = Teal)
                    Spacer(Modifier.height(8.dp))
                    Text(timer, color = Teal, fontSize = 31.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(model.list("exerciseSteps").getOrNull(step).orEmpty(), color = Teal, fontSize = 20.sp, lineHeight = 26.sp,
                textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${step + 1} / 4", color = Muted, fontSize = 14.sp)
            Text(timer, color = Teal, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Pale)) {
            Box(Modifier.fillMaxWidth(elapsed / 120f).fillMaxHeight().background(Teal))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(if (model.exerciseRunning) model.t("exercisePause") else if (elapsed > 0) model.t("exerciseResume") else model.t("exerciseStart"),
                model::toggleExercise, Modifier.weight(1f)) {
                Icon(if (model.exerciseRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null, Modifier.size(18.dp))
            }
            OutlinedButton(onClick = { model.navigate(Screen.CHAT) }, modifier = Modifier.heightIn(min = 56.dp), shape = RoundedCornerShape(16.dp)) {
                Text(model.t("exerciseDone"))
            }
        }
        Notice(model.t("exerciseCaution"), urgent = true)
        TextButton(onClick = { model.navigate(Screen.HELP) }) { Text(model.t("urgentHelp"), color = Rust) }
    }
}

@Composable
private fun InfoScreen(model: SahayViewModel, onOpenUrl: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(model.t("infoTitle").uppercase(), color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(model.t("infoHeading"), color = Ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Text(model.t("infoLead"), color = Muted, fontSize = 14.sp, lineHeight = 19.sp)
        model.content.cards(model.language).forEach { card ->
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("NATIONAL CANCER INSTITUTE", color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(card.title, color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(card.summary, color = Muted, fontSize = 14.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onOpenUrl(card.url) }) {
                            Icon(Icons.Outlined.MenuBook, null, Modifier.size(17.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(model.t("source"), fontSize = 14.sp, textDecoration = TextDecoration.Underline)
                        }
                        TextButton(onClick = { model.prepareCard(card) }) {
                            Text(model.t("askAbout"), fontSize = 14.sp)
                            Icon(Icons.Outlined.ArrowForward, null, Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
        Notice(model.t("helpMedical"), urgent = true)
    }
}

@Composable
private fun QuestionsScreen(model: SahayViewModel, onToast: (String) -> Unit) {
    val context = LocalContext.current
    var draft by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }
    var confirmClear by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(model.t("questionsTitle").uppercase(), color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(model.t("questionsHeading"), color = Ink, fontSize = 28.sp, lineHeight = 31.sp, fontWeight = FontWeight.SemiBold)
        Text(model.t("questionsLead"), color = Muted, fontSize = 14.sp, lineHeight = 19.sp)
        OutlinedButton(model::draftVisitBrief, Modifier.fillMaxWidth(), enabled = !model.busy) { Text("Draft a visit brief with AI") }
        PrimaryButton("Share visit brief with a clinician", model::openShare, Modifier.fillMaxWidth())
        if (model.questions.isEmpty()) {
            SoftPanel(Modifier.fillMaxWidth()) { Text(model.t("emptyQuestions"), Modifier.padding(23.dp), color = Muted, fontSize = 14.sp) }
        } else {
            model.questions.forEachIndexed { index, question ->
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = question.discussed, onCheckedChange = { model.toggleQuestion(index) })
                        Text(question.text, Modifier.weight(1f), color = if (question.discussed) Muted else Ink,
                            fontSize = 14.sp, lineHeight = 18.sp,
                            textDecoration = if (question.discussed) TextDecoration.LineThrough else null)
                        IconButton(onClick = { editingIndex = index; editingText = question.text }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.Edit, model.t("edit"), Modifier.size(18.dp), tint = Teal)
                        }
                        IconButton(onClick = { model.deleteQuestion(index) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.Delete, model.t("delete"), Modifier.size(18.dp), tint = Rust)
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = draft, onValueChange = { draft = it.take(300) }, modifier = Modifier.weight(1f),
                placeholder = { Text(model.t("questionPlaceholder"), fontSize = 14.sp) }, maxLines = 3)
            IconButton(onClick = { model.addQuestion(draft); draft = "" }, modifier = Modifier.size(48.dp).background(Teal, RoundedCornerShape(10.dp))) {
                Icon(Icons.Outlined.Add, model.t("addQuestion"), tint = androidx.compose.ui.graphics.Color.White)
            }
        }
        Text(nativeText(model.language, "suggestedQuestionsTitle").uppercase(), color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        model.list("suggestedQuestions").forEach { suggestion ->
            OutlinedButton(onClick = { if (model.questions.none { it.text == suggestion }) model.addQuestion(suggestion) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Text("+ $suggestion", color = Teal, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Sahay questions", model.questions.mapIndexed { i, q -> "${i + 1}. ${q.text}" }.joinToString("\n")))
                onToast(nativeText(model.language, "copyDone"))
            }, enabled = model.questions.isNotEmpty(), modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.ContentCopy, null, Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text(model.t("copyQuestions"), fontSize = 14.sp)
            }
            OutlinedButton(onClick = { confirmClear = true }, enabled = model.questions.isNotEmpty(), modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Delete, null, Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text(nativeText(model.language, "clearQuestions"), fontSize = 14.sp)
            }
        }
    }
    if (editingIndex != null) AlertDialog(
        onDismissRequest = { editingIndex = null }, title = { Text(model.t("edit")) },
        text = { OutlinedTextField(value = editingText, onValueChange = { editingText = it.take(300) }, minLines = 2) },
        confirmButton = { TextButton(onClick = { model.editQuestion(editingIndex!!, editingText); editingIndex = null }) { Text(model.t("save")) } },
        dismissButton = { TextButton(onClick = { editingIndex = null }) { Text(nativeText(model.language, "cancel")) } }
    )
    if (confirmClear) AlertDialog(
        onDismissRequest = { confirmClear = false }, title = { Text(nativeText(model.language, "clearQuestions")) }, text = { Text(nativeText(model.language, "clearQuestionsConfirm")) },
        confirmButton = { TextButton(onClick = { model.clearQuestions(); confirmClear = false }) { Text(nativeText(model.language, "clearQuestions"), color = Rust) } },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(nativeText(model.language, "cancel")) } }
    )
}

@Composable
private fun HelpScreen(model: SahayViewModel, onDial: (String) -> Unit, onOpenUrl: (String) -> Unit, onToast: (String) -> Unit) {
    var number by remember(model.careTeamNumber) { mutableStateOf(model.careTeamNumber) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("INDIA · HUMAN HELP", color = Teal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(model.t("helpTitle"), color = Ink, fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold)
        Notice(model.lastUrgentText.ifBlank { model.t("helpLead") }, urgent = true)
        HelpCard(model.t("emergency"), model.t("emergencyHint"), "112", model, onDial, urgent = true)
        HelpCard(model.t("mentalHealth"), model.t("mentalHealthHint"), "14416", model, onDial)
        SurfaceCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Call, null, tint = Teal)
                    Spacer(Modifier.width(8.dp))
                    Text(model.t("careTeam"), color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                Text(model.t("careTeamHint"), color = Muted, fontSize = 14.sp)
                if (model.careTeamNumber.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton("${model.t("call")} ${model.careTeamNumber}", { onDial(model.careTeamNumber) }, Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = number, onValueChange = { number = it.take(20) }, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(model.t("phonePlaceholder")) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone))
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { if (!model.saveCareTeamNumber(number)) onToast(nativeText(model.language, "invalidNumber")) }) {
                    Text(model.t("saveNumber"))
                }
            }
        }
        SoftPanel(Modifier.fillMaxWidth()) { Text(model.t("helpMedical"), Modifier.padding(16.dp), color = Muted, fontSize = 14.sp, lineHeight = 18.sp) }
        OutlinedButton(onClick = model::navigateHome, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.ArrowBack, null, Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text(model.t("returnChat"))
        }
        Text(nativeText(model.language, "resourceSource"), color = Muted, fontSize = 14.sp)
        TextButton(onClick = { onOpenUrl("https://112.gov.in/") }) { Text("112 · Emergency Response Support System", fontSize = 14.sp) }
        TextButton(onClick = { onOpenUrl("https://dghs.mohfw.gov.in/national-mental-health-programme.php") }) { Text("Tele-MANAS · DGHS", fontSize = 14.sp) }
    }
}

@Composable
private fun HelpCard(title: String, hint: String, number: String, model: SahayViewModel, onDial: (String) -> Unit, urgent: Boolean = false) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = if (urgent) Peach else Pale) {
                    Box(Modifier.size(37.dp), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Call, null, tint = if (urgent) Rust else Teal) }
                }
                Spacer(Modifier.width(8.dp))
                Text(title, color = Teal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Text(hint, color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onDial(number) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (urgent) Rust else Teal)) {
                Icon(Icons.Outlined.Call, null, Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text("${model.t("call")} $number", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PrivacyCover(model: SahayViewModel) {
    Column(Modifier.fillMaxSize().background(Pale).systemBarsPadding().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.sahay_emblem), null, Modifier.size(105.dp))
        Spacer(Modifier.height(24.dp))
        Text(model.t("privacyCover"), color = Teal, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(model.t("privacyCoverHint"), color = Muted, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        PrimaryButton(model.t("showAgain"), model::removeCover)
    }
}

@Composable
private fun TranscriptDialog(model: SahayViewModel, onRecord: () -> Unit, onSpeak: (String) -> Unit) {
    var text by remember(model.transcriptDraft) { mutableStateOf(model.transcriptDraft.orEmpty()) }
    AlertDialog(
        onDismissRequest = model::clearTranscript,
        title = { Text(nativeText(model.language, "voiceReview")) },
        text = {
            Column {
                Text(model.t("transcriptHint"), color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = text, onValueChange = { text = it.take(4000) }, minLines = 3, maxLines = 7)
            }
        },
        confirmButton = { TextButton(onClick = { model.send(text, onSpeak) }) { Text(model.t("sendTranscript")) } },
        dismissButton = {
            Row {
                TextButton(onClick = { model.clearTranscript(); onRecord() }) { Text(model.t("recordAgain")) }
                TextButton(onClick = model::clearTranscript) { Text(model.t("cancel")) }
            }
        }
    )
}

@Composable
private fun SessionRestoringScreen(model: SahayViewModel) {
    Column(Modifier.fillMaxSize().background(Mint).systemBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.sahay_emblem), null, Modifier.size(70.dp))
        Spacer(Modifier.height(18.dp))
        Text("Checking your ${model.accountRole ?: "account"} session", color = Teal, fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("${model.accountName} · ${model.accountContact}", color = Muted, fontSize = 14.sp,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        if (model.accountBusy) CircularProgressIndicator(color = Teal)
        if (model.accountError.isNotBlank()) {
            Notice(model.accountError)
            Spacer(Modifier.height(12.dp))
            PrimaryButton("Retry connection", model::retrySession, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = model::signOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
        TextButton(onClick = { model.navigate(Screen.HELP) }) { Text("Urgent help") }
    }
}

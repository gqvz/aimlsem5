package org.sahay.app

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.*

val CalmInk = Color(0xFF164B46)
val CalmBackground = Color(0xFFF2F7F6)
val CalmSoft = Color(0xFFE2EDEA)
val CalmLavender = Color(0xFFECE9F4)
val CalmMuted = Color(0xFF586B68)

fun wellbeingText(lang: String, key: String): String {
    val words = mapOf(
        "home" to listOf("Today", "आज", "আজ", "இன்று"),
        "talk" to listOf("Talk", "बातचीत", "কথা", "பேசலாம்"),
        "activities" to listOf("Explore", "गतिविधियाँ", "কার্যকলাপ", "செயல்கள்"),
        "care" to listOf("Care team", "देखभाल दल", "চিকিৎসা দল", "சிகிச்சைக் குழு"),
        "settings" to listOf("Settings", "सेटिंग्स", "সেটিংস", "அமைப்புகள்"),
        "pause" to listOf("Take a little pause", "थोड़ा विराम लें", "একটু বিরতি নিন", "சிறிது இடைவெளி எடுங்கள்"),
        "ready" to listOf("I'm ready", "मैं तैयार हूँ", "আমি প্রস্তুত", "நான் தயார்"),
        "welcome" to listOf("You can arrive just as you are.", "आप जैसे हैं, वैसे ही आ सकते हैं।", "আপনি যেমন আছেন, তেমনই আসতে পারেন।", "நீங்கள் இருப்பது போலவே வரலாம்."))
    return words[key]?.get(when(lang) { "hi" -> 1; "bn" -> 2; "ta" -> 3; else -> 0 }) ?: key
}

@Composable
fun CalmCard(modifier: Modifier = Modifier, color: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(28.dp), color = color) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable
fun CalmButton(text: String, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier.fillMaxWidth()) {
    Button(onClick, modifier.heightIn(min = 56.dp), enabled = enabled, shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CalmInk)) { WellbeingLabel(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
}
@Composable
fun CalmButton(text: String, enabled: Boolean = true, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    CalmButton(text, onClick, enabled, modifier)
}
@Composable
fun CalmPage(title: String, back: () -> Unit, model: SahayViewModel,
             bottomBar: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    CalmFrame(title, back, model, bottomBar) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = UiSpace.edge, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
fun HumanHelpLink(model: SahayViewModel) {
    TextButton({ model.navigate(Screen.HELP) }, Modifier.heightIn(min = 48.dp)) {
        Icon(Icons.Outlined.Call, null, Modifier.size(18.dp), tint = CalmMuted)
        Spacer(Modifier.width(8.dp))
        WellbeingLabel("Need to talk to someone now?", color = CalmMuted, fontSize = 14.sp)
    }
}

@Composable
private fun ActivityLayout(model: SahayViewModel, actions: @Composable () -> Unit,
                           content: @Composable ColumnScope.() -> Unit) {
    CalmPage(activityEntries.first { it.id == model.activityId }.title,
        { model.navigate(Screen.PATHS) }, model, bottomBar = actions) { content(); HumanHelpLink(model) }
}

@Composable
fun CalmArrival(model: SahayViewModel) {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    val transition = rememberInfiniteTransition(label = "arrival")
    val wave by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "ripple")
    Column(Modifier.fillMaxSize().background(CalmBackground).padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            WellbeingLabel("Sahay", color = CalmInk, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            TextButton({ model.navigate(Screen.HELP) }) { WellbeingLabel(model.t("help"), color = CalmInk) }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            WellbeingLabel(wellbeingText(model.language, "pause"), color = CalmInk, fontSize = 28.sp, lineHeight = 36.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            WellbeingLabel(wellbeingText(model.language, "welcome"), color = CalmMuted, fontSize = 16.sp, textAlign = TextAlign.Center)
            Canvas(Modifier.fillMaxWidth().height(200.dp).semantics { contentDescription = "Interactive garden" }.clickable { taps++ }) {
                val center = Offset(size.width / 2, size.height / 2)
                val base = size.minDimension * .24f
                for (i in 0..3) {
                    val progress = if (model.reduceMotion) i / 4f else (wave + i / 4f) % 1
                    drawCircle(Color(0xFF91B8AE).copy(alpha = (1 - progress) * .24f), base + progress * base * 1.3f, center, style = Stroke(2.dp.toPx()))
                }
                drawCircle(Color(0xFFD1E4DE), base, center)
                for (i in 0 until min(taps + 5, 12)) {
                    val angle = i * 2 * PI / min(taps + 5, 12)
                    val petal = center + Offset(cos(angle).toFloat() * base * .42f, sin(angle).toFloat() * base * .42f)
                    drawCircle(Color(0xFFB4C9BC).copy(alpha = .7f), base * .3f, petal)
                }
                drawCircle(Color(0xFFF0DEAE), base * .19f, center)
            }
            WellbeingLabel(if (taps == 0) "Touch the garden. Let it grow at your pace." else "A little space, just for this moment.", color = CalmMuted, textAlign = TextAlign.Center, fontSize = 14.sp)
        }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CalmButton(wellbeingText(model.language, "ready"), model::begin)
            TextButton({ model.navigate(Screen.CLINICIAN_AUTH) }) { WellbeingLabel("Care team sign in", color = CalmMuted) }
            WellbeingLabel("English · हिन्दी · বাংলা · தமிழ்", color = CalmMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun TodayHome(model: SahayViewModel) {
    var checkIn by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = UiSpace.edge, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WellbeingLabel(if (model.accountName.isBlank()) "A little space for today" else "Welcome, ${model.accountName.substringBefore(' ')}",
                fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, color = CalmInk)
            WellbeingLabel("Some days need rest. Some days need a little company. There is no right way to use this space.", color = CalmMuted, fontSize = 16.sp, lineHeight = 24.sp)
        }
        StatusBand(model)
        CalmCard(color = CalmSoft) {
            val orb by rememberInfiniteTransition(label = "orb").animateFloat(.88f, 1f,
                infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "orbScale")
            val scale = if (model.reduceMotion) 1f else orb
            Canvas(Modifier.fillMaxWidth().height(96.dp)) {
                val c = Offset(size.width / 2, size.height / 2)
                drawCircle(Color(0xFF91B8AE).copy(alpha = .2f), 44.dp.toPx() * scale, c)
                drawCircle(Color(0xFF91B8AE).copy(alpha = .35f), 32.dp.toPx() * scale, c)
                drawCircle(Color(0xFFF2F7F6), 20.dp.toPx() * scale, c)
            }
            WellbeingLabel("One gentle breath", Modifier.fillMaxWidth(), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = CalmInk, textAlign = TextAlign.Center)
            WellbeingLabel("A quiet two-minute rhythm. No breath holds. You can pause at any time.", Modifier.fillMaxWidth(), color = CalmMuted, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center)
            CalmButton("Begin when you're ready", { model.openActivity("breathing") })
        }
        CalmCard(color = CalmLavender) {
            SettingsLink("How are you arriving today?", { checkIn = !checkIn }, if (checkIn) "Close check-in" else "Make a gentle plan", Icons.Outlined.FavoriteBorder)
            if (checkIn) {
                listOf("A little unsettled", "Feeling low", "Doing okay").forEach { option ->
                    FilterChip(model.mood == option, { model.mood = option }, label = { WellbeingLabel(option, fontSize = 16.sp) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
                }
                WellbeingLabel("Your energy today", color = CalmInk, fontSize = 16.sp)
                listOf("low" to "Low energy", "medium" to "Some energy").forEach { (id, label) ->
                    FilterChip(model.energy == id, { model.energy = id }, label = { WellbeingLabel(label, fontSize = 16.sp) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
                }
                CalmButton(if (model.planBusy) "Finding a gentle plan…" else "Make a gentle plan", model::makeActivityPlan, !model.planBusy)
            }
        }
        if (model.moodLog.isNotEmpty()) MoodWeek(model)
        if (model.planItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WellbeingLabel("Your gentle plan", color = CalmInk, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                WellbeingLabel("A suggestion you can change. Choose only what feels comfortable.", color = CalmMuted, fontSize = 14.sp, lineHeight = 20.sp)
                model.planItems.forEach { ActivityRow(it.optString("id"), model) }
                TextButton({ model.navigate(Screen.AGENT_INSPECT) }) { WellbeingLabel("How this plan was made", color = CalmMuted) }
            }
        }
        CalmCard {
            SettingsLink("Someone to talk to", { model.navigate(Screen.CHAT) }, "Your AI companion", Icons.Outlined.ChatBubbleOutline)
            HorizontalDivider(color = CalmSoft)
            SettingsLink("Something to do", { model.navigate(Screen.PATHS) }, "Games, sound and art", Icons.Outlined.Palette)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WellbeingLabel("For your next care conversation", color = CalmInk, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            SettingsLink("Prepare my questions", { model.navigate(Screen.QUESTIONS) }, icon = Icons.Outlined.EditNote)
            SettingsLink("Read trusted information", { model.navigate(Screen.INFO) }, icon = Icons.Outlined.MenuBook)
        }
        if (model.completedActivities.isNotEmpty()) WellbeingLabel("You made time for ${model.completedActivities.size} quiet moment${if (model.completedActivities.size == 1) "" else "s"} today.", color = CalmMuted, fontSize = 14.sp)
    }
}

@Composable
private fun MoodWeek(model: SahayViewModel) {
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val letter = java.text.SimpleDateFormat("EEE", java.util.Locale(model.language))
    val byDay = model.moodLog.toMap()
    val days = (6 downTo 0).map { back -> java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -back) }.time }
    CalmCard {
        WellbeingLabel("This week", color = CalmInk, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                val mood = byDay[fmt.format(day)]
                val tint = when (mood) { "A little unsettled" -> Color(0xFFC9BFE0); "Feeling low" -> Color(0xFFA8CDDF); "Doing okay" -> Color(0xFFA9C4AD); else -> null }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(tint ?: Color.Transparent)
                        .border(1.5.dp, tint ?: CalmSoft, CircleShape)
                        .semantics { contentDescription = "${letter.format(day)}: ${mood?.let { wellbeingCopy(model.language, it) } ?: "no check-in"}" })
                    Text(letter.format(day).take(2), color = CalmMuted, fontSize = 12.sp)
                }
            }
        }
        WellbeingLabel("Only you can see this on this device. It is not a score.", color = CalmMuted, fontSize = 14.sp)
    }
}

data class ActivityEntry(val id: String, val title: String, val detail: String, val symbol: String, val color: Color)
val activityEntries = listOf(
    ActivityEntry("breathing", "Gentle breathing", "2 min · A rhythm you can follow or change", "○", CalmSoft),
    ActivityEntry("grounding", "Notice your surroundings", "2 min · A simple five-senses pause", "✧", Color(0xFFEDEEDB)),
    ActivityEntry("meditation", "A quiet moment", "3 min · Guided attention with optional voice", "☾", CalmLavender),
    ActivityEntry("sound", "Sound sanctuary", "White noise, brown noise and soft rain", "≈", Color(0xFFDFECF2)),
    ActivityEntry("memory", "Garden pairs", "An unhurried matching game", "❀", Color(0xFFF4E9EC)),
    ActivityEntry("stretch", "Gentle stretch", "3 min · Easy movements, seated or standing", "∿", Color(0xFFF0E9D8)),
    ActivityEntry("art", "Colour your own sky", "Draw, undo and save your own artwork", "⌁", CalmLavender),
    ActivityEntry("bodyscan", "A slow body scan", "4 min · Notice each part of you, gently", "◍", CalmLavender),
    ActivityEntry("safeplace", "A safe place", "3 min · Picture somewhere calm", "⌂", Color(0xFFDFECF2)),
    ActivityEntry("winddown", "Wind down for sleep", "15 or 30 min · Sound that fades out", "☽", Color(0xFFE6E6F2)),
    ActivityEntry("comforts", "Small comforts", "2 min · Notice what was a little easier", "☼", Color(0xFFF4E9EC)),
    ActivityEntry("worry", "Worry box", "Write it down, then let it go. Nothing is saved", "◌", CalmSoft),
    ActivityEntry("journal", "A few words for today", "Write freely. Keep it on your device if you choose", "✎", CalmSoft),
    ActivityEntry("peers", "Small words of support", "Example notes to read and reflect on", "♡", Color(0xFFF4E9EC)))

@Composable
fun ActivityRow(id: String, model: SahayViewModel) {
    val activity = activityEntries.firstOrNull { it.id == id } ?: return
    Row(Modifier.fillMaxWidth().clickable { model.openActivity(id) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(18.dp), color = activity.color) { Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) { WellbeingLabel(activity.symbol, color = CalmInk, fontSize = 28.sp) } }
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            WellbeingLabel(activity.title, color = CalmInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            WellbeingLabel(activity.detail, color = CalmMuted, fontSize = 14.sp, lineHeight = 20.sp)
        }
        Icon(Icons.Outlined.ChevronRight, "Open ${activity.title}", tint = CalmMuted)
    }
}
@Composable
fun ActivityLibrary(model: SahayViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = UiSpace.edge, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WellbeingLabel("Make room for a moment", color = CalmInk, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)
            WellbeingLabel("Rest, play or make something. Nothing to achieve, and no streak to keep.", color = CalmMuted, fontSize = 16.sp, lineHeight = 24.sp)
        }
        listOf("Rest and reset" to listOf("breathing", "grounding", "meditation", "bodyscan", "safeplace", "sound", "winddown"),
            "A little play" to listOf("memory", "stretch"), "Make and reflect" to listOf("art", "comforts", "worry", "journal", "peers")).forEach { (title, ids) ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WellbeingLabel(title, color = CalmInk, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                CalmCard { ids.forEachIndexed { i, id ->
                    ActivityRow(id, model)
                    if (i < ids.lastIndex) HorizontalDivider(color = CalmSoft)
                } }
            }
        }
    }
}
@Composable
fun ActivityScreen(model: SahayViewModel, onSpeak: (String) -> Unit, stopSpeaking: () -> Unit) {
    val entry = activityEntries.first { it.id == model.activityId }
    DisposableEffect(Unit) { onDispose { stopSpeaking(); AmbientAudio.stop() } }
    when (entry.id) {
        "breathing" -> BreathingActivity(model)
        "grounding" -> GroundingActivity(model)
        "meditation" -> MeditationActivity(model, onSpeak)
        "sound" -> SoundActivity(model)
        "memory" -> MemoryActivity(model)
        "stretch" -> StretchActivity(model)
        "bodyscan" -> BodyScanActivity(model)
        "safeplace" -> SafePlaceActivity(model)
        "comforts" -> ComfortsActivity(model)
        "worry" -> WorryActivity(model)
        "winddown" -> WindDownActivity(model)
        "art" -> ArtActivity(model)
        "journal" -> JournalActivity(model)
        "peers" -> PeerActivity(model)
    }
}
@Composable
private fun BreathingActivity(model: SahayViewModel) {
    var running by rememberSaveable { mutableStateOf(false) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    PauseOnBackground { running = false }
    val phase = seconds % 8
    val expanded by animateFloatAsState(if (running && phase < 4) 1f else .62f, tween(if (model.reduceMotion) 0 else 3800), label = "breath")
    LaunchedEffect(running) { while (running && seconds < 120) { delay(1000); seconds++; if (seconds == 120) { running = false; model.completeActivity("breathing") } } }
    ActivityLayout(model, actions = {
    CalmButton(if (running) "Pause" else if (seconds == 120) "Start again" else "Begin / resume") { if (seconds == 120) seconds = 0; running = !running }
    TextButton({ seconds = 0; running = false }) { WellbeingLabel("Reset") }
    }) {
    WellbeingLabel("Keep your breath comfortable. Follow the rhythm only if it feels easy, or breathe at your own pace. Stop if you feel uncomfortable; choose sound or art instead.", color = CalmMuted, fontSize = 14.sp, lineHeight = 21.sp)
    Canvas(Modifier.fillMaxWidth().height(250.dp)) {
        val radius = size.minDimension * .38f * (if (model.reduceMotion) .8f else expanded)
        drawCircle(Color(0xFFD5E5DF), radius + 20.dp.toPx())
        drawCircle(Color(0xFF91B8AD), radius)
        drawCircle(CalmBackground.copy(alpha = .5f), radius * .6f)
    }
    WellbeingLabel(if (seconds >= 120) "A little pause, complete" else if (!running) "Ready when you are" else if (phase < 4) "Breathe in gently" else "Let it out gently", color = CalmInk, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    WellbeingLabel("${120-seconds} seconds remaining", color = CalmMuted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}
@Composable
private fun GroundingActivity(model: SahayViewModel) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val prompts = listOf("Look around. Notice five things you can see.", "Notice four things you can feel, like a chair or soft fabric.", "Listen for three sounds around you.", "Notice two scents, or think of two familiar scents.", "Notice one taste, or picture a comforting drink.")
    ActivityLayout(model, actions = {
    CalmButton(if (step == 4) "Finish this pause" else "Next sense") { if (step < 4) step++ else { model.completeActivity("grounding"); model.navigate(Screen.HOME) } }
    TextButton({ if (step < 4) step++ else model.navigate(Screen.HOME) }) { WellbeingLabel("Skip this step") }
    }) {
    WellbeingLabel("You can skip any sense that feels uncomfortable. No need to close your eyes.", color = CalmMuted)
    CalmCard(color = CalmSoft) { WellbeingLabel("${step+1} of 5", color = CalmMuted); WellbeingLabel(prompts[step], color = CalmInk, fontSize = 28.sp, lineHeight = 33.sp) }
    }
}
@Composable
private fun MeditationActivity(model: SahayViewModel, speak: (String) -> Unit) {
    var running by rememberSaveable { mutableStateOf(false) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    PauseOnBackground { running = false }
    val scripts = listOf("Find a comfortable position. Your eyes can stay open. Notice the support beneath you.", "Notice a sound nearby. Let it come and go, without needing to change it.", "Thoughts may wander. Gently notice where you are again. There is nothing to get right.", "Take a moment to notice one small thing around you. When you are ready, return at your own pace.")
    val step = min(seconds / 45, 3)
    LaunchedEffect(running) { while (running && seconds < 180) { delay(1000); seconds++; if (seconds == 180) { running=false; model.completeActivity("meditation") } } }
    ActivityLayout(model, actions = {
    CalmButton(if (running) "Pause" else if (seconds == 180) "Start again" else "Begin / resume") { if (seconds == 180) seconds=0; running=!running }
    OutlinedButton({ speak(wellbeingCopy(model.language, scripts[step])) }, Modifier.fillMaxWidth()) { WellbeingLabel("Read this guidance aloud") }
    }) {
    WellbeingLabel("A three-minute practice of noticing. Pause whenever you like.", color = CalmMuted)
    CalmCard(color = CalmLavender) { WellbeingLabel(if (seconds == 180) "Your quiet moment is complete." else scripts[step], color = CalmInk, fontSize = 20.sp, lineHeight = 34.sp); WellbeingLabel("${180-seconds} seconds remaining", color = CalmMuted) }
    LinearProgressIndicator(progress = { seconds / 180f }, modifier = Modifier.fillMaxWidth(), color = CalmInk)
    }
}
@Composable
private fun SoundActivity(model: SahayViewModel) {
    var sound by rememberSaveable { mutableStateOf("rain") }
    var playing by remember { mutableStateOf(false) }
    var seconds by rememberSaveable { mutableIntStateOf(300) }
    PauseOnBackground { playing = false; AmbientAudio.stop() }
    DisposableEffect(Unit) { onDispose { AmbientAudio.stop() } }
    LaunchedEffect(playing) { while (playing && seconds > 0) { delay(1000); seconds--; if (seconds == 0) { playing=false; AmbientAudio.stop(); model.completeActivity("sound") } } }
    ActivityLayout(model, actions = {
    CalmButton(if (playing) "Stop sound" else "Play sound") { playing=!playing; if (playing) { if(seconds==0) seconds=300; AmbientAudio.play(sound,model.soundVolume) } else AmbientAudio.stop() }
    OutlinedButton({ playing=false; AmbientAudio.stop(); model.completeActivity("sound"); model.navigate(Screen.HOME) },Modifier.fillMaxWidth()) { WellbeingLabel("Finish my listening break") }
    }) {
    WellbeingLabel("Choose a sound and keep the volume comfortable. Audio stops when you leave this screen or background the app.", color = CalmMuted)
    listOf("rain" to "Soft rain", "white" to "White noise", "brown" to "Brown noise").forEach { (id,title) ->
        FilterChip(sound==id,{ sound=id; if (playing) AmbientAudio.play(sound,model.soundVolume) },label={ WellbeingLabel(title) })
    }
    WellbeingLabel("Volume", color = CalmInk)
    Slider(model.soundVolume,{ model.updateSoundVolume(it); AmbientAudio.volume=it }, valueRange=0f..1f)
    WellbeingLabel("${seconds/60}:${(seconds%60).toString().padStart(2,'0')} remaining", color=CalmMuted)
    }
}
@Composable
private fun MemoryActivity(model: SahayViewModel) {
    val cards=remember { listOf("❀","☾","✧","♧","❀","☾","✧","♧").shuffled() }
    val open=remember { mutableStateListOf<Int>() }; val matched=remember { mutableStateListOf<Int>() }
    LaunchedEffect(open.size) { if (open.size==2) { delay(850); if(cards[open[0]]==cards[open[1]]) matched.addAll(open); open.clear(); if(matched.size==8) model.completeActivity("memory") } }
    ActivityLayout(model, actions = {
        CalmButton(if (matched.size==8) "Back to my day" else "Back to activities", { model.navigate(if (matched.size==8) Screen.HOME else Screen.PATHS) })
    }) {
    WellbeingLabel("Turn two cards and find a pair. Take as much time as you like.",color=CalmMuted)
    cards.indices.chunked(2).forEach { row ->
        Row(horizontalArrangement=Arrangement.spacedBy(16.dp)) { row.forEach { index ->
            Surface(Modifier.weight(1f).height(88.dp).semantics { contentDescription = "Card ${index+1}, ${if(index in matched) "matched" else if(index in open) cards[index] else "face down"}" }.clickable(enabled=open.size<2 && index !in matched && index !in open) { open.add(index) },shape=RoundedCornerShape(20.dp),color=if(index in matched) CalmSoft else CalmLavender) {
                Box(contentAlignment=Alignment.Center) { WellbeingLabel(if(index in open || index in matched) cards[index] else "·",color=CalmInk,fontSize=40.sp) }
            }
        } }
    }
    WellbeingLabel(if(matched.size==8) "Your garden is complete. A lovely little pause." else "${matched.size/2} of 4 pairs found",color=CalmInk)
    }
}
/** One calm card per step with progress dots; every step can be skipped and nothing is timed. */
@Composable
private fun GuidedSteps(model: SahayViewModel, id: String, intro: String, steps: List<String>,
                        next: String, finish: String, onFinish: () -> Unit = { model.navigate(Screen.HOME) }) {
    var step by rememberSaveable(id) { mutableIntStateOf(0) }
    val last = steps.lastIndex
    val advance = { if (step < last) step++ else { model.completeActivity(id); onFinish() } }
    ActivityLayout(model, actions = {
        CalmButton(if (step == last) finish else next) { advance() }
        TextButton({ if (step < last) step++ else model.navigate(Screen.HOME) }) { WellbeingLabel("Skip this step") }
    }) {
        WellbeingLabel(intro, color = CalmMuted)
        CalmCard(color = CalmSoft) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.indices.forEach { i ->
                    Box(Modifier.size(if (i == step) 10.dp else 8.dp).clip(CircleShape)
                        .background(if (i <= step) CalmInk else CalmInk.copy(alpha = .2f)))
                }
            }
            WellbeingLabel(steps[step], color = CalmInk, fontSize = 28.sp, lineHeight = 36.sp)
            WellbeingLabel("${step + 1} of ${steps.size}", color = CalmMuted, fontSize = 14.sp)
        }
    }
}
@Composable
private fun StretchActivity(model: SahayViewModel) = GuidedSteps(model, "stretch",
    "Move only as far as feels comfortable. Skip anything that hurts, and stop whenever you like.",
    listOf("Let your shoulders drop away from your ears. Breathe out slowly.",
        "Slowly turn your head to one side, then the other. Stop if anything hurts.",
        "Open and close your hands a few times. Let your fingers loosen.",
        "Unclench your jaw. Let your tongue rest and your face soften.",
        "Press your feet into the floor. Notice the ground holding you."),
    "Next movement", "Finish this stretch")
@Composable
private fun BodyScanActivity(model: SahayViewModel) = GuidedSteps(model, "bodyscan",
    "Notice without trying to change anything. Skip any area that feels uncomfortable.",
    listOf("Notice your feet and legs. Feel where they rest.",
        "Notice your belly and chest rising and falling, just as they are.",
        "Notice your hands and arms. Let them be heavy.",
        "Notice your shoulders, neck and face. Soften what you can.",
        "Take in your whole body at once, resting here."),
    "Next area", "Finish body scan")
@Composable
private fun SafePlaceActivity(model: SahayViewModel) = GuidedSteps(model, "safeplace",
    "Imagine a place that feels safe, real or invented. If an image feels uncomfortable, let it go or stop.",
    listOf("Picture a place where you feel calm. It can be real or imagined.",
        "Notice what you can see there: colours, light, shapes.",
        "Notice what you can hear and feel there: a breeze, warmth, quiet.",
        "Let yourself stay a moment. You can return here whenever you like."),
    "Next detail", "Leave gently")
@Composable
private fun ComfortsActivity(model: SahayViewModel) = GuidedSteps(model, "comforts",
    "You don't need a big moment. A small one is enough.",
    listOf("Something that felt a little easier today.", "Someone or something that was kind to you.",
        "A small thing you noticed and liked."),
    "Next comfort", "Note one in my journal") {
    val prompt = wellbeingCopy(model.language, "A small comfort today was…")
    model.journalDraft = if (model.journalDraft.isBlank()) prompt else model.journalDraft + "\n" + prompt
    model.openActivity("journal")
}
@Composable
private fun WorryActivity(model: SahayViewModel) {
    // Deliberately plain remember{}: the text must never reach saved instance state or disk.
    var text by remember { mutableStateOf("") }
    var fading by remember { mutableStateOf("") }
    var released by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (released) 0f else 1f, tween(if (model.reduceMotion) 0 else 1800), label = "release")
    ActivityLayout(model, actions = {
        if (released) CalmButton("Back to my day", { model.navigate(Screen.HOME) })
        else CalmButton("Let it go", { fading = text; text = ""; released = true; model.completeActivity("worry") }, text.isNotBlank())
    }) {
        WellbeingLabel("Write what is on your mind. When you let it go, it fades and is not saved or sent anywhere.", color = CalmMuted)
        if (!released) OutlinedTextField(text, { text = it.take(600) }, Modifier.fillMaxWidth(), minLines = 5,
            label = { WellbeingLabel("What is weighing on you?") })
        else {
            CalmCard(color = CalmLavender) { WellbeingLabel(fading, Modifier.graphicsLayer { this.alpha = alpha }, color = CalmInk, fontSize = 20.sp, lineHeight = 28.sp) }
            WellbeingLabel("Released. You can come back to it later, or not at all.", color = CalmMuted)
        }
    }
}
@Composable
private fun WindDownActivity(model: SahayViewModel) {
    var minutes by rememberSaveable { mutableIntStateOf(15) }
    var remaining by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    val playing = remaining > 0
    PauseOnBackground { remaining = 0; AmbientAudio.stop() }
    DisposableEffect(Unit) { onDispose { AmbientAudio.stop() } }
    LaunchedEffect(playing) {
        while (remaining > 0) {
            delay(1000); remaining--
            // The sound holds steady, then fades across the last third of the session.
            AmbientAudio.volume = model.soundVolume * min(1f, remaining / (total / 3f)).coerceAtLeast(0f)
            if (remaining == 0) { AmbientAudio.stop(); model.completeActivity("winddown") }
        }
    }
    ActivityLayout(model, actions = {
        CalmButton(if (playing) "Stop sound" else "Start winding down") {
            if (playing) { remaining = 0; AmbientAudio.stop() }
            else { total = minutes * 60; remaining = total; AmbientAudio.play("brown", model.soundVolume) }
        }
    }) {
        WellbeingLabel("Choose how long. The sound softens gradually and then stops by itself.", color = CalmMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 30).forEach { m -> FilterChip(minutes == m, { if (!playing) minutes = m }, label = { WellbeingLabel("$m minutes") }) }
        }
        if (playing) WellbeingLabel("${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}", color = CalmInk, fontSize = 28.sp)
    }
}
private data class InkStroke(val points: List<Offset>,val color: Color)
@Composable
private fun PauseOnBackground(pause: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val currentPause by rememberUpdatedState(pause)
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _,event -> if(event == Lifecycle.Event.ON_STOP) currentPause() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
@Composable
private fun ArtActivity(model: SahayViewModel) {
    val context=LocalContext.current
    val strokes=remember { mutableStateListOf<InkStroke>() }
    var color by remember { mutableStateOf(CalmInk) }
    var width by remember { mutableIntStateOf(800) }; var height by remember { mutableIntStateOf(800) }
    ActivityLayout(model, actions = {
    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        OutlinedButton({ if(strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) },Modifier.weight(1f)) { WellbeingLabel("Undo") }
        OutlinedButton({ strokes.clear() },Modifier.weight(1f)) { WellbeingLabel("Clear") }
    }
    CalmButton("Save my drawing",{
        runCatching {
            val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888); val canvas=android.graphics.Canvas(bitmap); canvas.drawColor(android.graphics.Color.WHITE)
            strokes.forEach { stroke -> val paint=android.graphics.Paint().apply { this.color=stroke.color.toArgb(); strokeWidth=18f; style=android.graphics.Paint.Style.STROKE; strokeCap=android.graphics.Paint.Cap.ROUND; isAntiAlias=true }; val path=android.graphics.Path(); stroke.points.forEachIndexed { i,p -> if(i==0) path.moveTo(p.x,p.y) else path.lineTo(p.x,p.y) }; canvas.drawPath(path,paint) }
            if(Build.VERSION.SDK_INT>=29) {
                val values=ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME,"Sahay-${System.currentTimeMillis()}.png"); put(MediaStore.Images.Media.MIME_TYPE,"image/png"); put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Sahay") }
                val uri=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values) ?: error("Could not create picture")
                context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) } ?: error("Could not save picture")
                model.note("Saved to Pictures / Sahay")
            } else {
                val file=File(context.cacheDir,"sahay-drawing.png"); file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                val uri=FileProvider.getUriForFile(context,"${context.packageName}.files",file)
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),"Save or share drawing"))
            }
            model.completeActivity("art")
        }.onFailure { model.note("Could not save the drawing. Please try again.") }
    },strokes.isNotEmpty())
    }) {
    WellbeingLabel("Make a mark, draw a sky, or follow your hand. You can undo anything.",color=CalmMuted)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("Ink" to CalmInk,"Lilac" to Color(0xFFB49CCA),"Clay" to Color(0xFFD5A477),"Sky" to Color(0xFF729AB7),"Sage" to Color(0xFF9FAF7E)).forEach { (name,shade) ->
        Surface(Modifier.size(48.dp).semantics { contentDescription = "Drawing colour $name, ${if(color==shade) "selected" else "not selected"}" }.clickable { color=shade },shape=CircleShape,color=shade,border=if(color==shade) androidx.compose.foundation.BorderStroke(3.dp,CalmInk) else null) { Box(contentAlignment = Alignment.Center) { if(color==shade) Icon(Icons.Outlined.Check, null, tint = if(shade==CalmInk) Color.White else CalmInk) } }
    } }
    Canvas(Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(22.dp)).background(Color.White).pointerInput(color) {
        detectDragGestures(onDragStart={ strokes.add(InkStroke(listOf(it),color)) }) { change,_ ->
            change.consume(); if(strokes.isNotEmpty()) { val last=strokes.last(); strokes[strokes.lastIndex]=last.copy(points=last.points+change.position) }
        }
    }) {
        width=size.width.toInt(); height=size.height.toInt()
        strokes.forEach { stroke -> val path=Path(); stroke.points.forEachIndexed { i,p -> if(i==0) path.moveTo(p.x,p.y) else path.lineTo(p.x,p.y) }; drawPath(path,stroke.color,style=Stroke(7.dp.toPx(),cap=StrokeCap.Round)) }
    }
    }
}
@Composable
private fun JournalActivity(model: SahayViewModel) {
    ActivityLayout(model, actions = {
    CalmButton("Keep this reflection",{ model.saveJournal(); model.completeActivity("journal") },model.journalDraft.isNotBlank())

    }) {
    WellbeingLabel("A private space for a few words. You don't need to find a positive ending.",color=CalmMuted)
    listOf("Right now, I notice…", "One thing I would like someone to know…", "A small comfort today was…").forEach { prompt ->
        TextButton({ val localized = wellbeingCopy(model.language, prompt); model.journalDraft=if(model.journalDraft.isBlank()) localized else model.journalDraft+"\n"+localized }) { WellbeingLabel(prompt,color=CalmInk) }
    }
    OutlinedTextField(model.journalDraft,{ model.journalDraft=it.take(4000) },Modifier.fillMaxWidth(),minLines=7,label={ WellbeingLabel("Your words") })
    WellbeingLabel(if(model.rememberContent) "Saved only on this device. Clear it in Settings." else "Kept only for this session. Optional device saving is in Settings.",color=CalmMuted,fontSize = 14.sp)
    }
}
@Composable
private fun PeerActivity(model: SahayViewModel) {
    ActivityLayout(model, actions = {
    CalmButton("Write a note for my future self",{ model.openActivity("journal") })
    }) {
    WellbeingLabel("Example encouragement notes",color=CalmInk,fontSize = 20.sp)
    WellbeingLabel("These are fictional examples for the course demo, rather than messages from real patients. A live peer community needs moderation before it is added.",color=CalmMuted,fontSize = 14.sp)
    listOf("You don't have to make this day meaningful. Getting through it can be enough.","It is okay to ask someone to sit with you, even when you don't feel like talking.","Rest can be part of your day. You don't need to earn it.").forEach { note ->
        CalmCard(color=CalmLavender) {
            WellbeingLabel(note,color=CalmInk,fontSize = 20.sp,lineHeight=27.sp)
            TextButton({ model.journalDraft=wellbeingCopy(model.language, note); model.openActivity("journal") }) { WellbeingLabel("Keep this thought") }
        }
    }
    }
}

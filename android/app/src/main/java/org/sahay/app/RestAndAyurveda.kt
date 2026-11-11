package org.sahay.app

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

/* ---------------------------------------------------------------------------------------------
 * Bedtime reminder
 * From 10 pm until 5 am the app gently suggests sleep. The reminder is a card at the top of
 * Today and, once per night, a short message on whatever screen is open. "Not tonight" hides
 * the card until the next evening. Nothing is scheduled outside the app and nothing is stored.
 * ------------------------------------------------------------------------------------------- */

const val BEDTIME_HOUR = 22
private const val MORNING_HOUR = 5

fun isPastBedtime(now: LocalDateTime = LocalDateTime.now()): Boolean =
    now.hour >= BEDTIME_HOUR || now.hour < MORNING_HOUR

/** The evening a late hour belongs to: 1 am on the 12th is still the night of the 11th. */
fun bedtimeNightOf(now: LocalDateTime = LocalDateTime.now()): LocalDate =
    if (now.hour < MORNING_HOUR) now.toLocalDate().minusDays(1) else now.toLocalDate()

/** Re-reads the clock every 30 seconds so the reminder appears at 10 pm while the app is open. */
@Composable
fun rememberClock(): State<LocalDateTime> = produceState(LocalDateTime.now()) {
    while (true) { delay(30_000); value = LocalDateTime.now() }
}

/** Shows a one-line sleep reminder once per night, on any screen, while the app is in use. */
@Composable
fun NightlySleepNudge(model: SahayViewModel) {
    val now by rememberClock()
    var nudgedNight by rememberSaveable { mutableStateOf("") }
    val night = bedtimeNightOf(now).toString()
    val eligible = model.onboarded && model.screen != Screen.WELCOME && model.accountRole !in setOf("clinician", "admin")
    LaunchedEffect(isPastBedtime(now), night, eligible) {
        if (eligible && isPastBedtime(now) && nudgedNight != night) {
            nudgedNight = night
            model.note(wellbeingCopy(model.language, "It's past 10 pm. Your body heals while you sleep, so try to rest soon."))
        }
    }
}

@Composable
fun BedtimeReminder(model: SahayViewModel) {
    val now by rememberClock()
    var dismissedNight by rememberSaveable { mutableStateOf("") }
    val night = bedtimeNightOf(now).toString()
    if (!isPastBedtime(now) || dismissedNight == night) return
    val time = "%d:%02d %s".format(if (now.hour % 12 == 0) 12 else now.hour % 12, now.minute, if (now.hour < 12) "am" else "pm")
    Surface(shape = RoundedCornerShape(28.dp), color = Ember.Night, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Bedtime, null, tint = Ember.Amber)
                WellbeingLabel("It's $time", color = Ember.Steam, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            WellbeingLabel("Time to rest", Modifier.semantics { heading() }, color = Ember.Petal, fontSize = 28.sp, lineHeight = 34.sp)
            WellbeingLabel("It's past 10 pm. Your body heals while you sleep, so try to rest soon.",
                color = Ember.Petal, fontSize = 16.sp, lineHeight = 24.sp)
            WellbeingLabel("Ayurveda sees the hours before 10 pm as the calmest time to fall asleep. A few warm drops of oil rubbed into the soles of your feet can help you settle.",
                color = Ember.Steam, fontSize = 14.sp, lineHeight = 20.sp)
            Button({ model.openActivity("winddown") }, Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ember.Blush, contentColor = Ember.Onyx)) {
                WellbeingLabel("Wind down for sleep", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            TextButton({ dismissedNight = night }, Modifier.heightIn(min = 48.dp)) {
                WellbeingLabel("Not tonight", color = Ember.Steam, fontSize = 14.sp)
            }
        }
    }
}

/* ---------------------------------------------------------------------------------------------
 * Ayurvedic self-care
 * Gentle daily practices (dinacharya) and herbs traditionally used in Ayurveda, for the mind and
 * the body. Herbs are described without doses: several can interact with cancer treatment, so
 * the guide asks people to check with their care team first. This is not medical advice.
 * ------------------------------------------------------------------------------------------- */

enum class AyurvedaFocus(val label: String) { MIND("For the mind"), BODY("For the body"), BOTH("Mind and body") }

data class AyurvedaPractice(val name: String, val local: String, val focus: AyurvedaFocus,
                            val how: String, val care: String? = null)

data class AyurvedaHerb(val name: String, val local: String, val focus: AyurvedaFocus,
                        val traditionalUse: String, val caution: String)

val ayurvedaPractices = listOf(
    AyurvedaPractice("A steady daily rhythm", "Dinacharya", AyurvedaFocus.BOTH,
        "Wake, eat and sleep at roughly the same times each day. A predictable rhythm is the foundation of Ayurvedic self-care and helps both energy and mood."),
    AyurvedaPractice("Alternate-nostril breathing", "Nadi Shodhana", AyurvedaFocus.MIND,
        "Sit comfortably. Close the right nostril with your thumb and breathe in through the left; switch and breathe out through the right. Continue slowly for a few rounds, with no breath holds.",
        "Stop if you feel light-headed or short of breath."),
    AyurvedaPractice("Humming bee breath", "Bhramari", AyurvedaFocus.MIND,
        "Breathe in gently, then hum softly like a bee as you breathe out. Let the vibration settle your face and chest. Five or six rounds are enough."),
    AyurvedaPractice("Warm oil self-massage", "Abhyanga", AyurvedaFocus.BODY,
        "Warm a little sesame or coconut oil and massage it slowly into your arms, legs and shoulders before a warm bath. It is a grounding, nurturing ritual.",
        "Skip skin being treated with radiotherapy, broken skin, and areas around ports or lines. Ask your care team if unsure."),
    AyurvedaPractice("Foot massage before bed", "Padabhyanga", AyurvedaFocus.BOTH,
        "Rub a few drops of warm oil into the soles of your feet at bedtime. Many people find this helps the mind settle for sleep. Wear socks afterwards to avoid slipping."),
    AyurvedaPractice("Warm, simple meals", "Ahara", AyurvedaFocus.BODY,
        "Favour freshly cooked, warm, easy-to-digest food such as khichdi, soups and dal. Make lunch your main meal and keep dinner light and early.",
        "Follow any diet your oncology team or dietitian has given you."),
    AyurvedaPractice("Sip warm water", "Ushnodaka", AyurvedaFocus.BODY,
        "Sip warm or room-temperature water through the day rather than very cold drinks. Ayurveda considers this gentle on digestion."),
    AyurvedaPractice("A calm evening routine", "Ratricharya", AyurvedaFocus.MIND,
        "Dim lights and put screens away an hour before bed. Read, listen to soft sound, or sit quietly, and aim to be asleep before 10 pm."),
    AyurvedaPractice("Gentle restorative yoga", "Yoga", AyurvedaFocus.BOTH,
        "Child's pose (Balasana) or resting with your legs up a wall (Viparita Karani) for a few minutes can ease tension. Use cushions and move only as far as is comfortable.",
        "Check with your care team before new movement if you have bone pain, low blood counts or recent surgery."),
)

val ayurvedaHerbs = listOf(
    AyurvedaHerb("Tulsi", "Holy basil", AyurvedaFocus.BOTH,
        "Drunk as a warm tea to support calm and resilience to stress, and for a soothing throat.",
        "In concentrated forms it may affect blood clotting and blood sugar."),
    AyurvedaHerb("Ashwagandha", "Withania somnifera", AyurvedaFocus.MIND,
        "A rasayana (rejuvenating) herb traditionally used for stress, restful sleep and strength. Some studies suggest it may lower stress.",
        "Can interact with thyroid, sedative, diabetes, blood pressure and immune-suppressing medicines. Rare liver problems have been reported. Avoid in pregnancy."),
    AyurvedaHerb("Brahmi", "Bacopa monnieri", AyurvedaFocus.MIND,
        "Traditionally used to support memory, focus and a quieter mind.",
        "May upset the stomach and can add to the effect of sedatives."),
    AyurvedaHerb("Jatamansi", "Spikenard", AyurvedaFocus.MIND,
        "Traditionally used to calm an unsettled mind and encourage sleep.",
        "Evidence is limited. It may add to the effect of sleeping tablets or sedatives."),
    AyurvedaHerb("Haldi", "Turmeric", AyurvedaFocus.BODY,
        "Used in everyday cooking and warm haldi doodh (golden milk) for its soothing, anti-inflammatory reputation.",
        "Cooking amounts are generally considered safe. Supplements may interact with blood thinners and some chemotherapy drugs."),
    AyurvedaHerb("Shunthi", "Dried ginger", AyurvedaFocus.BODY,
        "Used in tea or food to ease nausea and support digestion. Some studies suggest ginger may help treatment-related nausea.",
        "Larger amounts may increase bleeding risk with blood thinners or low platelets."),
    AyurvedaHerb("Amla", "Indian gooseberry", AyurvedaFocus.BODY,
        "A vitamin-C-rich fruit valued as a rasayana for general vitality and digestion.",
        "Concentrated extracts may affect blood sugar and clotting."),
    AyurvedaHerb("Triphala", "Three fruits", AyurvedaFocus.BODY,
        "A traditional blend used for gentle digestion and bowel regularity.",
        "It can loosen stools. Avoid if you have diarrhoea."),
)

private const val AYURVEDA_SAFETY = "Herbs and supplements can change how chemotherapy, radiotherapy, hormone therapy and other medicines work. Please ask your oncologist or pharmacist before taking any herb as a tea, powder or capsule. These are traditional ideas for comfort, not treatment."

/** A different practice each day on Today, so the suggestion stays fresh without a streak. */
@Composable
fun AyurvedaTipCard(model: SahayViewModel) {
    val tip = ayurvedaPractices[LocalDate.now().dayOfYear % ayurvedaPractices.size]
    CalmCard(color = Ember.LinenChalk) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Spa, null, tint = Ember.Olive)
            WellbeingLabel("FROM AYURVEDA TODAY", color = Ember.Olive, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        WellbeingLabel(tip.name, color = CalmInk, fontSize = 22.sp, lineHeight = 28.sp)
        WellbeingLabel("${tip.local} · ${tip.focus.label}", color = CalmMuted, fontSize = 14.sp)
        WellbeingLabel(tip.how, color = CalmInk, fontSize = 16.sp, lineHeight = 24.sp)
        tip.care?.let { WellbeingLabel(it, color = CalmMuted, fontSize = 14.sp, lineHeight = 20.sp) }
        SettingsLink("More herbs and practices", { model.openActivity("ayurveda") }, "For mental and physical wellbeing", Icons.Outlined.LocalFlorist)
    }
}

@Composable
fun AyurvedaActivity(model: SahayViewModel) {
    var focus by rememberSaveable { mutableStateOf<String?>(null) }
    val matches = { f: AyurvedaFocus -> focus == null || f == AyurvedaFocus.BOTH || f.name == focus }
    ActivityLayout(model, actions = {
        CalmButton("Try a calming breath", { model.openActivity("breathing") })
        TextButton({ model.completeActivity("ayurveda"); model.navigate(Screen.HOME) }, Modifier.fillMaxWidth()) {
            WellbeingLabel("Back to my day")
        }
    }) {
        WellbeingLabel("Ayurveda, India's traditional system of wellbeing, focuses on daily rhythm, warmth and balance. Choose anything that feels comfortable today.",
            color = CalmMuted, fontSize = 16.sp, lineHeight = 24.sp)
        Surface(shape = RoundedCornerShape(20.dp), color = Ember.LinenChalk) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Info, null, tint = Ember.Olive)
                WellbeingLabel(AYURVEDA_SAFETY, color = CalmInk, fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null to "All", AyurvedaFocus.MIND.name to "Mind", AyurvedaFocus.BODY.name to "Body").forEach { (id, label) ->
                FilterChip(focus == id, { focus = id }, label = { WellbeingLabel(label, fontSize = 16.sp) }, modifier = Modifier.heightIn(min = 48.dp))
            }
        }

        WellbeingLabel("Daily practices", Modifier.semantics { heading() }, color = CalmInk, fontSize = 24.sp)
        CalmCard {
            val shown = ayurvedaPractices.filter { matches(it.focus) }
            shown.forEachIndexed { i, practice ->
                AyurvedaRow(practice.name, practice.local, practice.focus, practice.how, practice.care, careIsWarning = false)
                if (i < shown.lastIndex) HorizontalDivider(color = Ember.Line)
            }
        }

        WellbeingLabel("Herbs traditionally used", Modifier.semantics { heading() }, color = CalmInk, fontSize = 24.sp)
        WellbeingLabel("Tap a herb to read about it. No doses are given on purpose.", color = CalmMuted, fontSize = 14.sp)
        CalmCard {
            val shown = ayurvedaHerbs.filter { matches(it.focus) }
            shown.forEachIndexed { i, herb ->
                AyurvedaRow(herb.name, herb.local, herb.focus, herb.traditionalUse, "Check first: ${herb.caution}", careIsWarning = true)
                if (i < shown.lastIndex) HorizontalDivider(color = Ember.Line)
            }
        }
    }
}

@Composable
private fun AyurvedaRow(title: String, local: String, focus: AyurvedaFocus, body: String, care: String?, careIsWarning: Boolean) {
    var open by rememberSaveable(title) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().animateContentSize().clickable(role = Role.Button) { open = !open }.padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                WellbeingLabel(title, color = CalmInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                WellbeingLabel("$local · ${focus.label}", color = CalmMuted, fontSize = 14.sp)
            }
            Icon(if (open) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (open) "Close $title" else "Open $title", tint = CalmMuted)
        }
        if (open) {
            WellbeingLabel(body, color = CalmInk, fontSize = 16.sp, lineHeight = 24.sp)
            care?.let {
                Surface(shape = RoundedCornerShape(14.dp), color = if (careIsWarning) Ember.ErrorSoft else Ember.SandyCream) {
                    WellbeingLabel(it, Modifier.padding(12.dp), color = if (careIsWarning) Ember.Rust else CalmMuted, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

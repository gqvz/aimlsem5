package org.sahay.app

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Core colour tokens. Screens should use these rather than declaring their own colours. */
object Tok {
    val Alabaster = Color(0xFFF7F6F2)
    val Cream = Color(0xFFFAF6F0)
    val OliveTaupe = Color(0xFF727064)
    val DeepCharcoal = Color(0xFF1C1D16)
    val SageTop = Color(0xFFCBC9B6)
    val SageBottom = Color(0xFFDFDEC9)

    val GlassFill = Color.White.copy(alpha = .16f)
    val GlassBorder = Color.White.copy(alpha = .28f)
    val GlassInner = Color(0x731E231C)

    val Blush = Color(0xFFF9EBEB)
    val Mint = Color(0xFFEBF5EE)
    val Lavender = Color(0xFFEFEBF9)
    val IceBlue = Color(0xFFEBF3F9)
    val EditorialSage = Color(0xFFD8E6D9)
    val ButterCard = Color(0xFFFDF0A6)
    val AvatarBlue = Color(0xFFDCE7F5)

    val Charcoal = Color(0xFF1A1A18)
    val Espresso = Color(0xFF381D15)
    val MutedOlive = Color(0xFF6E6D60)
    val ForestTeal = Color(0xFF1E5E4A)
    val Coral = Color(0xFFD96B6B)
    val Terracotta = Color(0xFF742613)
    val ButterActive = Color(0xFFFCE881)

    val ChartLight = Color(0xFFA8A48D)
    val ChartMid = Color(0xFF7E7B66)
    val ChartDark = Color(0xFF68675C)

    val Hairline = Color(0xFFE5DDD5)
    val TimelineBase = Color(0xFFDCE5DF)

    val SageGradient = Brush.verticalGradient(listOf(SageTop, SageBottom))
}

/** The three visual contexts. Each screen picks exactly one. */
enum class SurfaceMode(val background: Brush, val base: Color, val ink: Color, val muted: Color, val action: Color) {
    /** Immersive, onboarding and check-in views. */
    Atmospheric(Tok.SageGradient, Tok.SageBottom, Tok.Charcoal, Tok.Charcoal.copy(alpha = .74f), Tok.ForestTeal),
    /** Dashboards, trackers and account surfaces. */
    Clinical(Brush.verticalGradient(listOf(Tok.Alabaster, Tok.Alabaster)), Tok.Alabaster, Tok.Charcoal, Tok.MutedOlive, Tok.ForestTeal),
    /** Feeds, discovery and reading. */
    Editorial(Brush.verticalGradient(listOf(Tok.Cream, Tok.Cream)), Tok.Cream, Tok.Espresso, Tok.MutedOlive, Tok.Terracotta)
}

val LocalSurfaceMode = staticCompositionLocalOf { SurfaceMode.Clinical }
val LocalReduceMotion = compositionLocalOf { false }

val SahaySans = FontFamily(
    Font(R.font.plus_jakarta_sans_light, FontWeight.Light),
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold))

val SahaySerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic))

private fun TextStyle.sans() = copy(fontFamily = SahaySans)

/** Material components inherit the sans family. Indic scripts fall back to the system's Noto fonts. */
fun sahayTypography(): Typography {
    val b = Typography()
    return Typography(b.displayLarge.sans(), b.displayMedium.sans(), b.displaySmall.sans(),
        b.headlineLarge.sans(), b.headlineMedium.sans(), b.headlineSmall.sans(),
        b.titleLarge.sans(), b.titleMedium.sans(), b.titleSmall.sans(),
        b.bodyLarge.sans(), b.bodyMedium.sans(), b.bodySmall.sans(),
        b.labelLarge.sans(), b.labelMedium.sans(), b.labelSmall.sans())
}

/** Blur-fade entrance with 75ms staggering. Reduce motion shows content immediately. */
@Composable
fun Modifier.entrance(index: Int = 0): Modifier {
    val reduce = LocalReduceMotion.current
    var shown by remember { mutableStateOf(reduce) }
    LaunchedEffect(Unit) { delay(75L * index); shown = true }
    val spec: AnimationSpec<Float> = if (reduce) snap() else tween(550, easing = CubicBezierEasing(.22f, 1f, .36f, 1f))
    val progress by animateFloatAsState(if (shown) 1f else 0f, spec, label = "entrance")
    return this
        .graphicsLayer { alpha = progress; translationY = (1f - progress) * 8.dp.toPx() }
        .blur(((1f - progress) * 12f).dp, BlurredEdgeTreatment.Unbounded)
}

/** Sans headline with one or two serif-italic emotional anchor words, in English only. */
@Composable
fun MixedHeadline(text: String, anchors: List<String>, modifier: Modifier = Modifier,
                  color: Color = LocalSurfaceMode.current.ink, fontSize: TextUnit = 34.sp,
                  textAlign: TextAlign? = null) {
    val language = LocalWellbeingLanguage.current
    val full = wellbeingCopy(language, text)
    val styled = buildAnnotatedString {
        append(full)
        if (language == "en") anchors.forEach { anchor ->
            val start = full.indexOf(anchor)
            if (start >= 0) addStyle(SpanStyle(fontFamily = SahaySerif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal), start, start + anchor.length)
        }
    }
    // Tight display leading clips Indic matras, so non-Latin scripts get more room.
    Text(styled, modifier, color = color, fontSize = fontSize, fontWeight = FontWeight.Medium,
        lineHeight = fontSize * (if (language == "en") 1.12f else 1.4f), textAlign = textAlign)
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = LocalSurfaceMode.current.muted) {
    Text(wellbeingCopy(LocalWellbeingLanguage.current, text).uppercase(), modifier, color = color, fontSize = 11.sp,
        lineHeight = 16.sp, letterSpacing = .08.em, fontWeight = FontWeight.SemiBold)
}

@Composable
fun SahayAvatar(name: String, modifier: Modifier = Modifier) {
    Box(modifier.size(40.dp).clip(CircleShape).background(Tok.AvatarBlue), contentAlignment = Alignment.Center) {
        Text(name.trim().take(1).uppercase().ifBlank { "S" }, color = Tok.Charcoal, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** A translucent card. It needs a dark photographic backdrop to keep white text readable. */
@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(24.dp), color = Tok.GlassFill, contentColor = Color.White,
        border = BorderStroke(1.dp, Tok.GlassBorder)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

/**
 * One image slot per archetype in the design system. Add a licensed file named [asset] to
 * res/drawable-nodpi and it replaces the drawn, person-free placeholder without code changes.
 */
enum class SahayPhoto(val asset: String) {
    Lake("photo_lake"),
    GoldenHour("photo_golden_hour"),
    MacroSensory("photo_macro_sensory"),
    Serenity("photo_serenity"),
    Courtyard("courtyard"),
    PolaroidTwo("photo_polaroid_two"),
    PolaroidThree("photo_polaroid_three")
}

@Composable
fun PhotoSlot(photo: SahayPhoto, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val id = remember(photo) { context.resources.getIdentifier(photo.asset, "drawable", context.packageName) }
    if (id != 0) Image(painterResource(id), null, modifier, contentScale = ContentScale.Crop)
    else Canvas(modifier.clearAndSetSemantics { }) { drawScene(photo) }
}

private fun DrawScope.drawScene(photo: SahayPhoto) {
    val w = size.width
    val h = size.height
    when (photo) {
        SahayPhoto.Lake, SahayPhoto.Courtyard -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF232C28), Color(0xFF36413B), Color(0xFF454E46)), 0f, h * .62f))
            val ridge = Path().apply {
                moveTo(0f, h * .5f); quadraticTo(w * .3f, h * .34f, w * .55f, h * .46f)
                quadraticTo(w * .8f, h * .36f, w, h * .44f); lineTo(w, h * .62f); lineTo(0f, h * .62f); close()
            }
            drawPath(ridge, Color(0xFF2E3833))
            for (i in 0..13) {
                val x = w * i / 13f
                val tall = h * (.09f + .05f * ((i * 7) % 5) / 4f)
                val pine = Path().apply { moveTo(x, h * .62f - tall); lineTo(x + tall * .24f, h * .62f); lineTo(x - tall * .24f, h * .62f); close() }
                drawPath(pine, Color(0xFF262F2B))
            }
            drawRect(Brush.verticalGradient(listOf(Color(0xFF33403A), Color(0xFF1E2622)), h * .62f, h), Offset(0f, h * .62f), Size(w, h * .38f))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = .1f), Color.Transparent), h * .5f, h * .7f), Offset(0f, h * .5f), Size(w, h * .2f))
        }
        SahayPhoto.GoldenHour -> {
            drawRect(Brush.linearGradient(listOf(Color(0xFF5A1712), Color(0xFFA63A1E), Color(0xFFDB8A3A)), Offset.Zero, Offset(w, h)))
            drawCircle(Brush.radialGradient(listOf(Color(0x99FFD08A), Color.Transparent), Offset(w * .72f, h * .3f), h * .9f), h * .9f, Offset(w * .72f, h * .3f))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB3200A06)), h * .3f, h))
        }
        SahayPhoto.MacroSensory -> {
            drawRect(Brush.horizontalGradient(listOf(Color(0xFFE2703A), Color(0xFF8E4A2C), Color(0xFF3B2A22))))
            drawCircle(Brush.radialGradient(listOf(Color(0x80F9C27A), Color.Transparent), Offset(0f, h * .4f), w * .8f), w * .8f, Offset(0f, h * .4f))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC120B08)), h * .35f, h))
        }
        SahayPhoto.Serenity -> {
            drawRect(Tok.Cream)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFE9EC9A), Color.Transparent), Offset(w * .5f, h * .5f), min(w, h) * .6f), min(w, h) * .6f, Offset(w * .5f, h * .5f))
        }
        SahayPhoto.PolaroidTwo -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFE99A5B), Color(0xFF9A4B3A))))
            drawCircle(Color(0xFFFBD78E), min(w, h) * .18f, Offset(w * .5f, h * .6f))
        }
        SahayPhoto.PolaroidThree -> drawRect(Brush.verticalGradient(listOf(Tok.EditorialSage, Tok.ChartMid)))
    }
}

/** 1.5dp dashed contour used around the introspective portrait, or the garden until one is added. */
fun DrawScope.dashedContour(radius: Float) {
    val dash = 6.dp.toPx()
    drawCircle(Color.White, radius, center, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))))
}

/** 54dp full-width pill: text on the left, nested pill or circular action on the right. */
@Composable
fun SplitPill(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, nested: String = "→",
              enabled: Boolean = true, container: Color = LocalSurfaceMode.current.action,
              content: Color = Color.White) {
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 54.dp), enabled = enabled, shape = CircleShape,
        color = if (enabled) container else container.copy(alpha = .4f), contentColor = content) {
        Row(Modifier.heightIn(min = 54.dp).padding(start = 22.dp, end = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            WellbeingLabel(text, Modifier.weight(1f).padding(vertical = 8.dp), color = content, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Surface(shape = CircleShape, color = content.copy(alpha = .16f), contentColor = content) {
                Box(Modifier.heightIn(min = 40.dp).widthIn(min = 40.dp).padding(horizontal = if (nested.length > 1) 14.dp else 0.dp), contentAlignment = Alignment.Center) {
                    WellbeingLabel(nested, color = content, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/** "Today" active pill against "Yesterday" text. */
@Composable
fun PillToggle(first: String, second: String, firstActive: Boolean, onChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(first to true, second to false).forEach { (label, value) ->
            val active = firstActive == value
            Surface(onClick = { onChange(value) }, modifier = Modifier.heightIn(min = 48.dp), shape = CircleShape, color = Color.Transparent) {
                Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.clip(CircleShape).background(if (active) Tok.ForestTeal else Color.Transparent).padding(horizontal = 14.dp, vertical = 6.dp)) {
                        WellbeingLabel(label, color = if (active) Color.White else Tok.MutedOlive, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun DateBadge(day: String, month: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Tok.Mint) {
        Column(Modifier.size(54.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(day, color = Tok.ForestTeal, fontFamily = SahaySerif, fontSize = 22.sp, lineHeight = 24.sp)
            Text(month, color = Tok.MutedOlive, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 1)
        }
    }
}

/** 20 pills, oldest first. A tall teal pill marks a day with a check-in; it is not a score. */
@Composable
fun MicroPillTimeline(days: List<Boolean>, description: String, modifier: Modifier = Modifier) {
    Row(modifier.height(36.dp).semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
        days.forEach { active ->
            Box(Modifier.width(6.dp).height(if (active) 34.dp else 14.dp).clip(CircleShape).background(if (active) Tok.ForestTeal else Tok.TimelineBase))
        }
    }
}

@Composable
fun MicroStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Eyebrow(label, color = Tok.MutedOlive)
        Text(value, color = Tok.Charcoal, fontFamily = SahaySerif, fontSize = 22.sp, lineHeight = 26.sp)
    }
}

/** 76dp ring that interpolates from coral towards teal as [progress] approaches 1. */
@Composable
fun RingGauge(progress: Float, centerText: String, modifier: Modifier = Modifier) {
    val reduce = LocalReduceMotion.current
    val spec: AnimationSpec<Float> = if (reduce) snap() else tween(800, easing = EaseOut)
    val value by animateFloatAsState(progress.coerceIn(0f, 1f), spec, label = "ring")
    Box(modifier.size(76.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(76.dp).clearAndSetSemantics { }) {
            val stroke = 8.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Tok.TimelineBase, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            drawArc(lerp(Tok.Coral, Tok.ForestTeal, value), -90f, 360f * value, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Text(centerText, color = Tok.Charcoal, fontFamily = SahaySerif, fontSize = 22.sp)
    }
}

@Composable
fun AssessmentRingCard(eyebrow: String, headline: String, anchors: List<String>, progress: Float, centerText: String,
                       stats: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White) {
        Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RingGauge(progress, centerText)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Eyebrow(eyebrow, color = Tok.MutedOlive)
                MixedHeadline(headline, anchors, color = Tok.Charcoal, fontSize = 20.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    stats.forEach { (label, value) -> MicroStat(label, value, Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Four or fewer stats split by 1dp hairlines. */
@Composable
fun StatStrip(stats: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        stats.forEachIndexed { index, (label, value) ->
            if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(Tok.Hairline))
            Column(Modifier.weight(1f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, color = Tok.Charcoal, fontFamily = SahaySerif, fontSize = 24.sp, lineHeight = 28.sp)
                Eyebrow(label, color = Tok.MutedOlive)
            }
        }
    }
}

@Composable
fun BentoTile(title: String, minutes: Int, tint: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val reduce = LocalReduceMotion.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val spec: AnimationSpec<Float> = if (reduce) snap() else spring(dampingRatio = Spring.DampingRatioLowBouncy)
    val pressScale by animateFloatAsState(if (pressed) .97f else 1f, spec, label = "tile")
    Surface(onClick = onClick, modifier = modifier.graphicsLayer { scaleX = pressScale; scaleY = pressScale },
        shape = RoundedCornerShape(18.dp), color = tint, interactionSource = source) {
        Column(Modifier.fillMaxHeight().heightIn(min = 115.dp).padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color.White) {
                    Text("$minutes ${wellbeingCopy(LocalWellbeingLanguage.current, "MIN")}", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Tok.Espresso, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .08.em)
                }
                Surface(shape = CircleShape, color = Tok.Espresso) {
                    Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.PlayArrow, null, Modifier.size(16.dp), tint = Color.White)
                    }
                }
            }
            WellbeingLabel(title, Modifier.padding(top = 12.dp), color = Tok.Espresso, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp)
        }
    }
}

/** Non-overlapping circles with areas proportional to their counts, 46dp to 110dp across. */
@Composable
fun PackedBubbles(items: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    val shades = listOf(Tok.ChartDark, Tok.ChartLight, Tok.ChartMid)
    val largest = max(items.maxOf { it.second }, 1)
    val reduce = LocalReduceMotion.current
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val limit = min(110f, (maxWidth.value - 12f * (items.size - 1)) / items.size)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.Bottom) {
            items.forEachIndexed { index, (label, count) ->
                val diameter = max(46f, limit * sqrt(count.toFloat() / largest))
                val shade = shades[index % shades.size]
                var shown by remember { mutableStateOf(reduce) }
                LaunchedEffect(Unit) { shown = true }
                val spec: AnimationSpec<Float> = if (reduce) snap() else spring(dampingRatio = Spring.DampingRatioLowBouncy)
                val scale by animateFloatAsState(if (shown) 1f else .6f, spec, label = "bubble")
                Column(Modifier.widthIn(max = limit.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(diameter.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(CircleShape).background(shade), contentAlignment = Alignment.Center) {
                        Text("$count", color = if (shade.luminance() > .3f) Tok.Charcoal else Color.White, fontFamily = SahaySerif, fontSize = 22.sp)
                    }
                    WellbeingLabel(label, color = Tok.MutedOlive, fontSize = 14.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

/** Three tilted squares with 6dp white borders above a dashed arch. Purely decorative. */
@Composable
fun PolaroidCollage(modifier: Modifier = Modifier) {
    val tiles = listOf(Triple(SahayPhoto.Courtyard, -8f, -1), Triple(SahayPhoto.PolaroidTwo, 2f, 0), Triple(SahayPhoto.PolaroidThree, 8f, 1))
    Box(modifier.fillMaxWidth().height(168.dp).clearAndSetSemantics { }, contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxWidth().height(168.dp)) {
            val arch = Path().apply { moveTo(0f, size.height); quadraticTo(size.width / 2, size.height * .55f, size.width, size.height) }
            val dash = 6.dp.toPx()
            drawPath(arch, Tok.ChartLight, style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))))
        }
        tiles.forEach { (photo, angle, slot) ->
            Surface(Modifier.offset(x = 92.dp * slot, y = 8.dp).rotate(angle).size(108.dp), color = Color.White, shadowElevation = 3.dp) {
                PhotoSlot(photo, Modifier.padding(6.dp).size(96.dp))
            }
        }
    }
}

/** Decorative words that drift upward as the page scrolls, and stay still with reduce motion. */
@Composable
fun FloatingKeywords(scroll: () -> Int, modifier: Modifier = Modifier) {
    if (LocalWellbeingLanguage.current != "en") return
    val reduce = LocalReduceMotion.current
    val words = listOf(Triple("...feel", 20, 24), Triple("I learned...", 170, 62), Triple("...believe...", 72, 112))
    Box(modifier.clearAndSetSemantics { }) {
        words.forEachIndexed { index, (word, x, y) ->
            val factor = .18f + index * .1f
            Text(word, Modifier.offset { IntOffset((x * density).toInt(), (y * density).toInt() - if (reduce) 0 else (scroll() * factor).toInt()) },
                color = Color.White.copy(alpha = .9f), fontFamily = SahaySerif, fontStyle = FontStyle.Italic, fontSize = 18.sp)
        }
    }
}

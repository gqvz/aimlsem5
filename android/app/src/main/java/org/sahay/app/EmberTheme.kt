package org.sahay.app

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

/*
 * Ember: the terracotta palette from the "Sahay app UI" design (Ember boards).
 * Same components and layout as before; only colour and type change.
 * Inter for interface text, DM Serif Display for headings.
 */
object Ember {
    val Petal = Color(0xFFFCF8F5)        // phone background
    val SandyCream = Color(0xFFF1E6DB)   // page and surface fill behind cards
    val LinenChalk = Color(0xFFE7E4D0)   // help, live and fixed tags; olive-tinted cards
    val Blush = Color(0xFFF2CDBD)        // primary button fill, selected tabs and chips
    val Terracotta = Color(0xFFD67A53)   // illustration body, icon accents
    val BurntSienna = Color(0xFFA75A4A)  // edges of selected items, switches, focus
    val Clay = Color(0xFF8B372D)         // links, accent text, errors
    val Rust = Color(0xFF6A2D20)         // deep accent, urgent actions
    val Onyx = Color(0xFF330601)         // all main text
    val Amber = Color(0xFFF6A14B)        // diya flame, highlights
    val Olive = Color(0xFF5F5B3A)        // help icons, live status text
    val WarmGray = Color(0xFF6A463D)     // secondary text

    val Raise = Color(0xFFFFFFFF)
    val Line = Color(0xFFE8D8CC)
    val LineStrong = Color(0xFF8A6A5E)
    val SiennaSoft = Color(0xFFF4DED5)   // neem-s: selected / soft primary tint
    val ClaySoft = Color(0xFFF6DDD0)
    val AmberSoft = Color(0xFFFBE7D0)
    val RoseSoft = Color(0xFFF6DDD8)
    val ErrorSoft = Color(0xFFF4D9D2)
    val Aura = Color(0xFFF6D4B8)
    val Steam = Color(0xFFE3CDBE)
    val Night = Color(0xFF3E241B)        // aura1 (dark) for the bedtime card

    // Pastel card tints (p-rose, p-blue, p-lilac, p-mint, p-sand in Ember)
    val TintRose = Color(0xFFF7E0D9)
    val TintLinen = Color(0xFFE8E4D2)
    val TintLilac = Color(0xFFF0DCD4)
    val TintMoss = Color(0xFFE3E1C9)
    val TintSand = Color(0xFFF7E8D5)

    // Mood tints
    val MoodCalm = Color(0xFFB8BC8C)
    val MoodLow = Color(0xFFC2B6A8)
    val MoodWorry = Color(0xFFEDA98A)
}

val EmberColorScheme: ColorScheme = lightColorScheme(
    primary = Ember.BurntSienna, onPrimary = Color.White,
    primaryContainer = Ember.Blush, onPrimaryContainer = Ember.Onyx,
    secondary = Ember.Clay, onSecondary = Color.White,
    secondaryContainer = Ember.Blush, onSecondaryContainer = Ember.Onyx,
    tertiary = Ember.Olive, onTertiary = Color.White,
    tertiaryContainer = Ember.LinenChalk, onTertiaryContainer = Ember.Onyx,
    background = Ember.Petal, onBackground = Ember.Onyx,
    surface = Ember.Raise, onSurface = Ember.Onyx,
    surfaceVariant = Ember.SandyCream, onSurfaceVariant = Ember.WarmGray,
    surfaceContainerLowest = Ember.Raise, surfaceContainerLow = Ember.Petal,
    surfaceContainer = Ember.Petal, surfaceContainerHigh = Ember.SandyCream,
    surfaceContainerHighest = Ember.SandyCream,
    inverseSurface = Ember.Onyx, inverseOnSurface = Ember.Petal, inversePrimary = Ember.Blush,
    outline = Ember.LineStrong, outlineVariant = Ember.Line,
    error = Ember.Clay, onError = Color.White, errorContainer = Ember.ErrorSoft, onErrorContainer = Ember.Rust,
)

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: FontWeight) = Font(R.font.inter_variable, weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val InterFamily = FontFamily(
    inter(FontWeight.Normal), inter(FontWeight.Medium), inter(FontWeight.SemiBold), inter(FontWeight.Bold))

val SerifDisplayFamily = FontFamily(
    Font(R.font.dm_serif_display, FontWeight.Normal),
    Font(R.font.dm_serif_display_italic, FontWeight.Normal, FontStyle.Italic))

/** DM Serif Display has Latin glyphs only, so headings in Hindi, Bengali and Tamil stay in the sans. */
fun headingFamily(language: String): FontFamily = if (language == "en") SerifDisplayFamily else InterFamily

val EmberTypography: Typography = Typography().let { base ->
    fun TextStyle.sans() = copy(fontFamily = InterFamily)
    fun TextStyle.serif() = copy(fontFamily = SerifDisplayFamily, fontWeight = FontWeight.Normal)
    Typography(
        displayLarge = base.displayLarge.serif(), displayMedium = base.displayMedium.serif(), displaySmall = base.displaySmall.serif(),
        headlineLarge = base.headlineLarge.serif(), headlineMedium = base.headlineMedium.serif(), headlineSmall = base.headlineSmall.serif(),
        titleLarge = base.titleLarge.serif(), titleMedium = base.titleMedium.sans(), titleSmall = base.titleSmall.sans(),
        bodyLarge = base.bodyLarge.sans(), bodyMedium = base.bodyMedium.sans(), bodySmall = base.bodySmall.sans(),
        labelLarge = base.labelLarge.sans(), labelMedium = base.labelMedium.sans(), labelSmall = base.labelSmall.sans(),
    )
}

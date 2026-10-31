package org.sahay.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalActionAreaHeight = staticCompositionLocalOf<(Int) -> Unit> { {} }

// Shared rhythm: 8 within controls, 16 between related items, 24 between sections.
object UiSpace {
    val small = 8.dp
    val item = 16.dp
    val edge = 20.dp
    val section = 24.dp
}

@Composable
fun CalmTopBar(title: String, model: SahayViewModel, back: (() -> Unit)? = null,
               settings: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(back) { Icon(Icons.Outlined.ArrowBack, wellbeingCopy(model.language, "Back"), tint = CalmInk) }
        WellbeingLabel(title, Modifier.weight(1f).padding(start = if (back == null) 12.dp else 4.dp, end = 8.dp),
            color = CalmInk, fontSize = 20.sp, fontWeight = FontWeight.Medium, maxLines = 2)
        if (model.screen != Screen.HELP) IconButton({ model.navigate(Screen.HELP) }) { Icon(Icons.Outlined.HelpOutline, model.t("help"), tint = CalmInk) }
        // Settings is already a bottom destination for patients; clinicians access it here.
        if (settings != null) IconButton(settings) { Icon(Icons.Outlined.Settings, wellbeingText(model.language, "settings"), tint = CalmInk) }
        IconButton(model::showCover) { Icon(Icons.Outlined.VisibilityOff, model.t("quickHide"), tint = CalmInk) }
    }
}

@Composable
fun CalmFrame(title: String, back: () -> Unit, model: SahayViewModel, mode: SurfaceMode = SurfaceMode.Clinical,
              bottomBar: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val reportHeight = LocalActionAreaHeight.current
    if (bottomBar != null) DisposableEffect(reportHeight) { onDispose { reportHeight(0) } }
    CompositionLocalProvider(LocalSurfaceMode provides mode) {
        Column(Modifier.fillMaxSize().background(mode.background)) {
            CalmTopBar(title, model, back)
            Column(Modifier.weight(1f).fillMaxWidth(), content = content)
            if (bottomBar != null) {
                Surface(modifier = Modifier.onSizeChanged { reportHeight(it.height) }, color = mode.base) {
                    Column {
                        HorizontalDivider(color = Tok.Hairline)
                        Column(Modifier.fillMaxWidth().padding(horizontal = UiSpace.edge, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) { bottomBar() }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsLink(title: String, onClick: () -> Unit, detail: String? = null,
                 icon: ImageVector? = null, destructive: Boolean = false) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button, onClick = onClick)
        .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        val tint = if (destructive) MaterialTheme.colorScheme.error else CalmInk
        if (icon != null) Icon(icon, null, tint = tint)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            WellbeingLabel(title, color = tint, fontSize = 16.sp)
            if (detail != null) WellbeingLabel(detail, color = LocalSurfaceMode.current.muted, fontSize = 14.sp, lineHeight = 20.sp)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = LocalSurfaceMode.current.muted)
    }
}

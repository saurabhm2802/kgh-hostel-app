package com.kgh.hostel.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// KGH brand palette — a warm maroon/gold pairing suited to a girls' hostel,
// distinct from generic Material defaults.
val KghMaroon = Color(0xFF7A1F2B)
val KghMaroonDark = Color(0xFF4E1119)
val KghGold = Color(0xFFC9A227)
val KghCream = Color(0xFFFFF8F0)
val KghSurface = Color(0xFFFFFFFF)
val KghError = Color(0xFFB3261E)

private val LightColors = lightColorScheme(
    primary = KghMaroon,
    onPrimary = Color.White,
    secondary = KghGold,
    onSecondary = Color.Black,
    background = KghCream,
    surface = KghSurface,
    error = KghError
)

private val DarkColors = darkColorScheme(
    primary = KghGold,
    onPrimary = Color.Black,
    secondary = KghMaroon,
    background = Color(0xFF1B1113),
    surface = Color(0xFF241619)
)

@Composable
fun KGHTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? Activity
        activity?.window?.let { window ->
            window.statusBarColor = colors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}

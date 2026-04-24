package fr.antoinehory.bonnefete.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.glance.material3.ColorProviders
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Thème principal axé sur le bleu profond et l'or
private val MedievalColorScheme = darkColorScheme(
    primary = AntiqueGold,
    onPrimary = MedievalBlueDark,
    secondary = RubyRed,
    onSecondary = Parchment,
    tertiary = LightGold,
    background = MedievalBlue,
    onBackground = AntiqueGold,
    surface = MedievalBlueDark,
    onSurface = Parchment,
    surfaceVariant = DarkParchment,
    onSurfaceVariant = AntiqueGold
)

val MedievalGlanceColorScheme = ColorProviders(
    light = MedievalColorScheme,
    dark = MedievalColorScheme
)

@Composable
fun BonneFeteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Vous pouvez forcer 'true' pour un look toujours sombre
    content: @Composable () -> Unit
) {
    val colorScheme = MedievalColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

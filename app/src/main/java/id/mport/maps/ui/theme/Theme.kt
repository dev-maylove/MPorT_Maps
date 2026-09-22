package id.mport.maps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Neon palette (Phase 6E direction)
private val NeonCyan = Color(0xFF00E5FF)
private val NeonGreen = Color(0xFF00E676)
private val NeonPurple = Color(0xFFB388FF)
private val DeepBlack = Color(0xFF000814)
private val SurfaceDark = Color(0xFF0A1628)

private val DarkColors = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    secondary = NeonGreen,
    tertiary = NeonPurple,
    background = DeepBlack,
    surface = SurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFFF5252)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0277BD),
    onPrimary = Color.White,
    secondary = Color(0xFF00897B),
    background = Color(0xFFF5F7FA),
    surface = Color.White
)

@Composable
fun MPorTSurveyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

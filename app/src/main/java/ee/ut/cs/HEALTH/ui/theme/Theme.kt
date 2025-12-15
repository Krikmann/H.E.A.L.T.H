package ee.ut.cs.HEALTH.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ee.ut.cs.HEALTH.ui.theme.BrightOrange
/**
 * Dark Theme Color Scheme
 *
 * This theme uses a dark background with BrightOrange as the primary accent color
 * for key elements like buttons, icons, and titles.
 * The primary container is a softer orange for less prominent buttons.
 */
private val DarkColorScheme = darkColorScheme(
    primary = BrightOrange,
    onPrimary = Color.Black,
    primaryContainer = MediumOrange,
    onPrimaryContainer = Color.Black,

    secondary = MediumBlue,
    onSecondary = Color.White,

    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),

    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF9E9E9E)
)

/**
 * Light Theme Color Scheme
 *
 * This theme uses a classic light background with white cards and a strong,
 * dark blue-grey for text to ensure high readability and contrast.
 * The primary container is a light, friendly blue.
 */
private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = LightBlue40,
    tertiary = LightBlue40,
    primaryContainer = VeryLightBlue,
    onPrimaryContainer = BlueGrey40,

    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),

    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = BlueGrey40,
    onSurface = BlueGrey40,
    onSurfaceVariant = Color(0xFF6c757d)


)
/**
 * The main theme composable for the application.
 *
 * It applies the correct color scheme based on the system settings (light/dark) and
 * whether dynamic color is enabled. Dynamic color is turned off by default to
 * ensure consistent branding and contrast across all devices.
 *
 * @param darkTheme Whether the theme should be in dark mode.
 * @param dynamicColor Whether to use Material You dynamic colors (Android 12+). Default is false.
 * @param content The content to which the theme will be applied.
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
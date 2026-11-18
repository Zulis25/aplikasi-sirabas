package com.stb.sirabas.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WarnaTerangSirabas = lightColorScheme(
    primary = Color(0xFF236547),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFDDEFE4),
    onPrimaryContainer = Color(0xFF123F2E),

    secondary = Color(0xFF526B5C),
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFEAF4EE),
    onSecondaryContainer = Color(0xFF183B30),

    tertiary = Color(0xFF805B15),

    background = Color(0xFFF7F7F0),
    onBackground = Color(0xFF183B30),

    surface = Color.White,
    onSurface = Color(0xFF183B30),

    surfaceVariant = Color(0xFFEAF4EE),
    onSurfaceVariant = Color(0xFF627369),

    surfaceContainer = Color(0xFFF0F4EB),
    surfaceContainerHigh = Color(0xFFEAF4EE),
    surfaceContainerHighest = Color(0xFFE1E6DB),

    outline = Color(0xFF8A9D90),
    outlineVariant = Color(0xFFE1E6DB),

    error = Color(0xFFB3261E),
    onError = Color.White
)

private val WarnaGelapSirabas = darkColorScheme(
    primary = Color(0xFFA8D5B5),
    onPrimary = Color(0xFF123F2E),

    primaryContainer = Color(0xFF236547),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFFB7CCBA),
    onSecondary = Color(0xFF183B30),

    secondaryContainer = Color(0xFF254635),
    onSecondaryContainer = Color(0xFFE4EEE5),

    tertiary = Color(0xFFE3CB91),

    background = Color(0xFF121C16),
    onBackground = Color(0xFFE4EEE5),

    surface = Color(0xFF1C2A21),
    onSurface = Color(0xFFE4EEE5),

    surfaceVariant = Color(0xFF2A3D30),
    onSurfaceVariant = Color(0xFFBBCBBE),

    surfaceContainer = Color(0xFF1C2A21),
    surfaceContainerHigh = Color(0xFF25372B),
    surfaceContainerHighest = Color(0xFF2A3D30),

    outline = Color(0xFF8A9D90),
    outlineVariant = Color(0xFF3D5142),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun SIRABASTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Warna mengikuti identitas SIRABAS.
    // Parameter dynamicColor dipertahankan agar pemanggilan lama tetap cocok.
    val warna = if (darkTheme) {
        WarnaGelapSirabas
    } else {
        WarnaTerangSirabas
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity

            if (activity != null) {
                WindowCompat.getInsetsController(
                    activity.window,
                    view
                ).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = warna,
        typography = Typography,
        content = content
    )
}
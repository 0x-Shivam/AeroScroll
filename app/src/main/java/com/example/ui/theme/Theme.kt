package com.example.ui.theme

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

private val AeroColorScheme = darkColorScheme(
    primary = AeroPrimary,
    secondary = AeroSecondary,
    tertiary = AeroTertiary,
    background = AeroBackground,
    surface = AeroSurface,
    surfaceVariant = AeroSurfaceVariant,
    onPrimary = AeroOnPrimary,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = AeroOnBackground,
    onSurface = AeroOnSurface,
    onSurfaceVariant = AeroOnSurfaceVariant,
    error = AeroError,
    onError = Color.White,
    errorContainer = AeroError.copy(alpha = 0.2f),
    onErrorContainer = AeroError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Allow dynamic color for users who want it, but default to our Aero "vibe"
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> AeroColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

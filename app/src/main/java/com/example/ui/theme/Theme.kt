package com.example.ui.theme

import android.hardware.lights.Light
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewDynamicColors

private val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
        background = Color(0xFF1C1B1F),
        surface = Color(0xFF1C1B1F),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color(0xFFE6E1E5),
        onSurface = Color(0xFFE6E1E5)
        )

private val LightColorScheme =
    LightColorScheme(
        primary = SleekPrimary,
        onprimary = Color.White,
        secondary = SleekAccent,
        onsecondary = Color.White,
        background = SleekBackground,
        onBackground = SleekText,
        surface = Color.White,
        onSurface = SleekText,
        surfaceVariant = SleekSurfaceVariant,
        onSurfaceVariant = SleekOnSurfaceVariant,
        outline = SleekOutline,
        secondaryContainer = SleekSecondaryContainer,
        onSecondaryContainer = SleekOnSecondaryContainer
    )


@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color by default to preserve custom Apple-inspired design branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {


            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
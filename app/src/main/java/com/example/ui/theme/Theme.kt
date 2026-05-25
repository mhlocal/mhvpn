package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPink,
    tertiary = NeonPurple,
    background = CyberBlack,
    surface = CyberDarkGray,
    onPrimary = CyberBlack,
    onSecondary = CyberWhite,
    onTertiary = CyberWhite,
    onBackground = CyberWhite,
    onSurface = CyberWhite
)

private val LightColorScheme = lightColorScheme(
    primary = NeonCyan,
    secondary = NeonPink,
    tertiary = NeonPurple,
    background = CyberDarkGray,
    surface = CyberNavy,
    onPrimary = CyberBlack,
    onSecondary = CyberWhite,
    onTertiary = CyberWhite,
    onBackground = CyberWhite,
    onSurface = CyberWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to gorgeous dark cyber theme
    dynamicColor: Boolean = false, // Force custom polished cyber identity
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


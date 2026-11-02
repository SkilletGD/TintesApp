package com.skillet.tintes.ui.theme

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
import com.skillet.tintes.core.theme.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrangeVivid,
    onPrimary = Color.White,
    primaryContainer = BrandOrangeDarkContainer,
    onPrimaryContainer = Color(0xFFFFEDD5),
    secondary = BrandAmber,
    onSecondary = Slate900,
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = WarningAmberTextDark,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate600,
    error = CriticalRedTextDark,
    onError = Color.White,
    errorContainer = CriticalRedBgDark,
    onErrorContainer = CriticalRedTextDark
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    onPrimary = Color.White,
    primaryContainer = BrandOrangeLight,
    onPrimaryContainer = BrandOrange,
    secondary = BrandAmber,
    onSecondary = Slate900,
    secondaryContainer = BrandAmberContainer,
    onSecondaryContainer = WarningAmberText,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate200,
    outlineVariant = Slate300,
    error = CriticalRedText,
    onError = Color.White,
    errorContainer = CriticalRedBg,
    onErrorContainer = CriticalRedText
)

@Composable
fun TintesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

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
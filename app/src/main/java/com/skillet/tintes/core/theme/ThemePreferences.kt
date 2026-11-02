package com.skillet.tintes.core.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String, val icon: String) {
    SYSTEM("Sistema", "📱"),
    LIGHT("Claro", "☀️"),
    DARK("Oscuro", "🌙")
}

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("tintes_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private fun getSavedThemeMode(): ThemeMode {
        val saved = prefs.getString("key_theme_mode", ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(saved ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("key_theme_mode", mode.name).apply()
        _themeMode.value = mode
    }
}

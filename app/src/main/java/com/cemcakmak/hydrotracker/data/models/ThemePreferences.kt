package com.cemcakmak.hydrotracker.data.models

/**
 * Material 3 Expressive theme preferences
 * Manages dynamic color settings and theme customization
 */
data class ThemePreferences(
    val useDynamicColor: Boolean = false, // Default to our water theme
    val darkMode: DarkModePreference = DarkModePreference.SYSTEM,
    val colorSource: ColorSource = ColorSource.HYDRO_THEME
)

enum class DarkModePreference {
    SYSTEM,     // Follow system setting
    LIGHT,      // Always light mode
    DARK;       // Always dark mode

    fun getDisplayName(): String {
        return when (this) {
            SYSTEM -> "System Default"
            LIGHT -> "Light Mode"
            DARK -> "Dark Mode"
        }
    }

    fun getDescription(): String {
        return when (this) {
            SYSTEM -> "Follows your device settings"
            LIGHT -> "Always use light theme"
            DARK -> "Always use dark theme"
        }
    }
}

enum class ColorSource {
    HYDRO_THEME,    // Our default water-themed colors
    DYNAMIC_COLOR,  // Material You dynamic colors from wallpaper
    CUSTOM;         // Future: Custom color picker

    fun getDisplayName(): String {
        return when (this) {
            HYDRO_THEME -> "HydroTracker Blue"
            DYNAMIC_COLOR -> "Dynamic Colors"
            CUSTOM -> "Custom Colors"
        }
    }

    fun getDescription(): String {
        return when (this) {
            HYDRO_THEME -> "Beautiful water-themed blue palette"
            DYNAMIC_COLOR -> "Colors from your wallpaper"
            CUSTOM -> "Create your own color scheme"
        }
    }

    fun requiresAndroid12(): Boolean {
        return this == DYNAMIC_COLOR
    }
}
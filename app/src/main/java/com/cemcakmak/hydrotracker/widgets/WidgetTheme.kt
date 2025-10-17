package com.cemcakmak.hydrotracker.widgets

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.widget.RemoteViews

/**
 * Material 3 theming manager for widgets
 * Provides consistent color application across all widget types
 * with support for dynamic colors (Android 12+) and custom palettes (Android 11-)
 */
object WidgetTheme {

    /**
     * Apply Material 3 theme to widget RemoteViews
     * Automatically selects between dynamic colors and custom palette based on Android version
     */
    fun applyTheme(
        context: Context,
        views: RemoteViews,
        textViewIds: List<Int> = emptyList(),
        accentTextViewIds: List<Int> = emptyList(),
        variantTextViewIds: List<Int> = emptyList(),
        buttonTextViewIds: List<Int> = emptyList()
    ) {
        val isDarkMode = isDarkMode(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: Use dynamic system colors (Material You)
            applyDynamicColors(context, views, textViewIds, accentTextViewIds, variantTextViewIds, buttonTextViewIds, isDarkMode)
        } else {
            // Android 11 and below: Use custom color palette
            applyCustomColors(views, textViewIds, accentTextViewIds, variantTextViewIds, buttonTextViewIds, isDarkMode)
        }
    }

    /**
     * Apply dynamic system colors for Android 12+
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
    private fun applyDynamicColors(
        context: Context,
        views: RemoteViews,
        textViewIds: List<Int>,
        accentTextViewIds: List<Int>,
        variantTextViewIds: List<Int>,
        buttonTextViewIds: List<Int>,
        isDarkMode: Boolean
    ) {
        try {
            // onSurface color (for Title and Progress text)
            val onSurfaceColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: Use semantic color names
                if (isDarkMode) {
                    context.getColor(android.R.color.system_on_surface_dark)
                } else {
                    context.getColor(android.R.color.system_on_surface_light)
                }
            } else {
                // Android 12-13: Use numbered tone system
                if (isDarkMode) {
                    context.getColor(android.R.color.system_neutral1_100)
                } else {
                    context.getColor(android.R.color.system_neutral1_900)
                }
            }

            // Primary color (for Percentage)
            val primaryColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: Use semantic color names
                if (isDarkMode) {
                    context.getColor(android.R.color.system_primary_dark)
                } else {
                    context.getColor(android.R.color.system_primary_light)
                }
            } else {
                // Android 12-13: Use numbered tone system
                if (isDarkMode) {
                    context.getColor(android.R.color.system_accent1_200)
                } else {
                    context.getColor(android.R.color.system_accent1_600)
                }
            }

            // onSurfaceVariant color (for Last Updated text)
            val onSurfaceVariantColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: Use semantic color names
                if (isDarkMode) {
                    context.getColor(android.R.color.system_on_surface_variant_dark)
                } else {
                    context.getColor(android.R.color.system_on_surface_variant_light)
                }
            } else {
                // Android 12-13: Use numbered tone system
                if (isDarkMode) {
                    context.getColor(android.R.color.system_neutral2_200)
                } else {
                    context.getColor(android.R.color.system_neutral2_700)
                }
            }

            // onSecondaryContainer color (for Button text)
            val onSecondaryContainerColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: Use semantic color names
                if (isDarkMode) {
                    context.getColor(android.R.color.system_on_secondary_container_dark)
                } else {
                    context.getColor(android.R.color.system_on_secondary_container_light)
                }
            } else {
                // Android 12-13: Use numbered tone system
                if (isDarkMode) {
                    context.getColor(android.R.color.system_accent2_100)
                } else {
                    context.getColor(android.R.color.system_accent2_800)
                }
            }

            // Apply onSurface to text views (Title and Progress text)
            textViewIds.forEach { id ->
                views.setTextColor(id, onSurfaceColor)
            }

            // Apply Primary to accent text views (Percentage)
            accentTextViewIds.forEach { id ->
                views.setTextColor(id, primaryColor)
            }

            // Apply onSurfaceVariant to variant text views (Last Updated)
            variantTextViewIds.forEach { id ->
                views.setTextColor(id, onSurfaceVariantColor)
            }

            // Apply onSecondaryContainer to button text views
            buttonTextViewIds.forEach { id ->
                views.setTextColor(id, onSecondaryContainerColor)
            }

        } catch (_: Exception) {
            // Fallback if dynamic colors fail
            applyCustomColors(views, textViewIds, accentTextViewIds, variantTextViewIds, buttonTextViewIds, isDarkMode)
        }
    }

    /**
     * Apply custom color palette for Android 11 and below
     */
    private fun applyCustomColors(
        views: RemoteViews,
        textViewIds: List<Int>,
        accentTextViewIds: List<Int>,
        variantTextViewIds: List<Int>,
        buttonTextViewIds: List<Int>,
        isDarkMode: Boolean
    ) {
        // onSurface color (for Title and Progress text)
        val onSurfaceColor = if (isDarkMode) {
            0xFFE2E2E5.toInt()  // Light gray on dark
        } else {
            0xFF1A1C1E.toInt()  // Dark gray on light
        }

        // Primary color (for Percentage)
        val primaryColor = if (isDarkMode) {
            0xFF9ACBFF.toInt()  // Light blue on dark
        } else {
            0xFF0077BE.toInt()  // Dark blue on light
        }

        // onSurfaceVariant color (for Last Updated text)
        val onSurfaceVariantColor = if (isDarkMode) {
            0xFFC4C6D0.toInt()  // Muted gray on dark
        } else {
            0xFF44474E.toInt()  // Muted dark gray on light
        }

        // onSecondaryContainer color (for Button text)
        val onSecondaryContainerColor = if (isDarkMode) {
            0xFFD3E4FF.toInt()  // Light blue-gray on dark
        } else {
            0xFF003258.toInt()  // Dark blue-gray on light
        }

        // Apply onSurface to text views (Title and Progress text)
        textViewIds.forEach { id ->
            views.setTextColor(id, onSurfaceColor)
        }

        // Apply Primary to accent text views (Percentage)
        accentTextViewIds.forEach { id ->
            views.setTextColor(id, primaryColor)
        }

        // Apply onSurfaceVariant to variant text views (Last Updated)
        variantTextViewIds.forEach { id ->
            views.setTextColor(id, onSurfaceVariantColor)
        }

        // Apply onSecondaryContainer to button text views
        buttonTextViewIds.forEach { id ->
            views.setTextColor(id, onSecondaryContainerColor)
        }
    }

    /**
     * Check if system is in dark mode
     */
    private fun isDarkMode(context: Context): Boolean {
        return context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}

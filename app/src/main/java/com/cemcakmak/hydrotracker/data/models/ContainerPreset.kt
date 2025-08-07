package com.cemcakmak.hydrotracker.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Predefined container sizes for quick water logging
 * Updated with new volumes and Material Icons
 */
data class ContainerPreset(
    val id: Long = 0,
    val name: String,
    val volume: Double, // in milliliters
    val isDefault: Boolean = false,
    val icon: ImageVector? = null, // Changed to use Material Icons
    val isCustom: Boolean = false
) {

    /**
     * Gets formatted volume for display
     */
    fun getFormattedVolume(): String {
        return when {
            volume >= 1000 -> "${(volume / 1000).format(1)} L"
            else -> "${volume.toInt()} ml"
        }
    }

    /**
     * Gets display text combining name and volume
     */
    fun getDisplayText(): String {
        return "$name (${getFormattedVolume()})"
    }

    companion object {
        /**
         * Default container presets with new volumes and Material Icons
         */
        fun getDefaultPresets(): List<ContainerPreset> {
            return listOf(
                ContainerPreset(
                    id = 1,
                    name = "Coffee Cup",
                    volume = 100.0,
                    isDefault = true,
                    icon = Icons.Default.LocalCafe
                ),
                ContainerPreset(
                    id = 2,
                    name = "Small Glass",
                    volume = 150.0,
                    isDefault = true,
                    icon = Icons.Default.LocalBar
                ),
                ContainerPreset(
                    id = 3,
                    name = "Tea Cup",
                    volume = 175.0,
                    isDefault = true,
                    icon = Icons.Default.EmojiFoodBeverage
                ),
                ContainerPreset(
                    id = 4,
                    name = "Medium Glass",
                    volume = 200.0,
                    isDefault = true,
                    icon = Icons.Default.LocalDrink
                ),
                ContainerPreset(
                    id = 5,
                    name = "Large Glass",
                    volume = 300.0,
                    isDefault = true,
                    icon = Icons.Default.LocalDrink
                ),
                ContainerPreset(
                    id = 6,
                    name = "Water Bottle",
                    volume = 500.0,
                    isDefault = true,
                    icon = Icons.Default.WaterDrop
                ),
                ContainerPreset(
                    id = 7,
                    name = "Large Bottle",
                    volume = 1000.0,
                    isDefault = true,
                    icon = Icons.Default.Opacity
                )
            )
        }
    }
}

// Extension function to format Double with specified decimal places
private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
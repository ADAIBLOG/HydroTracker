// HydroSnackbarHost.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/common/HydroSnackbarHost.kt

package com.cemcakmak.hydrotracker.presentation.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

/**
 * Enhanced Material 3 Snackbar Host with stacking support and dynamic colors
 * Combines traditional snackbar with global stacking system
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HydroSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    // Use both the traditional snackbar and the new stacking system
    Box(modifier = modifier) {
        // Traditional snackbar (now hidden, but still functional for compatibility)
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier.alpha(0f), // Hidden but functional
            snackbar = { snackbarData ->
                // Convert to stacked snackbar when shown
                LaunchedEffect(snackbarData) {
                    SnackbarQueue.addSnackbar(
                        HydroSnackbar(
                            message = snackbarData.visuals.message,
                            type = when {
                                snackbarData.visuals.message.contains("success", ignoreCase = true) ||
                                snackbarData.visuals.message.contains("added", ignoreCase = true) ||
                                snackbarData.visuals.message.contains("updated", ignoreCase = true) -> HydroSnackbarType.SUCCESS
                                
                                snackbarData.visuals.message.contains("error", ignoreCase = true) ||
                                snackbarData.visuals.message.contains("failed", ignoreCase = true) -> HydroSnackbarType.ERROR
                                
                                snackbarData.visuals.message.contains("warning", ignoreCase = true) ||
                                snackbarData.visuals.message.contains("remember", ignoreCase = true) -> HydroSnackbarType.WARNING
                                
                                else -> HydroSnackbarType.INFO
                            },
                            duration = snackbarData.visuals.duration,
                            actionLabel = snackbarData.visuals.actionLabel,
                            onAction = if (snackbarData.visuals.actionLabel != null) {
                                { snackbarData.performAction() }
                            } else null
                        )
                    )
                    snackbarData.dismiss() // Immediately dismiss the traditional snackbar
                }
                Spacer(modifier = Modifier.size(0.dp)) // Empty composable
            }
        )
        
        // The actual stacked snackbar display
        StackedSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter))
    }
}



/**
 * Extension functions for easier snackbar usage with enhanced styling
 */
suspend fun SnackbarHostState.showSuccessSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration
    )
}

suspend fun SnackbarHostState.showErrorSnackbar(
    message: String,
    actionLabel: String? = "Retry",
    withDismissAction: Boolean = true,
    duration: SnackbarDuration = SnackbarDuration.Long
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration
    )
}

suspend fun SnackbarHostState.showWarningSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Long
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration
    )
}

suspend fun SnackbarHostState.showInfoSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration
    )
}


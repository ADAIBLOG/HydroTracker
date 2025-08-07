// NotificationSettingsComponents.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/settings/NotificationSettingsComponents.kt

package com.cemcakmak.hydrotracker.presentation.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.notifications.NotificationPermissionManager
import com.cemcakmak.hydrotracker.notifications.HydroNotificationScheduler
import kotlinx.coroutines.launch

/**
 * Notification Settings Section for the Settings Screen
 * Material 3 Expressive design with animated components
 */
@Composable
fun NotificationSettingsSection(
    userProfile: UserProfile?,
    onRequestPermission: () -> Unit,
    isVisible: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(NotificationPermissionManager.hasNotificationPermission(context))
    }

    var isNotificationsEnabled by remember {
        mutableStateOf(hasPermission && userProfile?.isOnboardingCompleted == true)
    }

    // Update states when userProfile changes
    LaunchedEffect(userProfile) {
        hasPermission = NotificationPermissionManager.hasNotificationPermission(context)
        isNotificationsEnabled = hasPermission && userProfile?.isOnboardingCompleted == true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hydration Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Permission Status Card
                NotificationPermissionCard(
                    hasPermission = hasPermission,
                    onRequestPermission = {
                        onRequestPermission()
                        // Update state after permission request
                        hasPermission = NotificationPermissionManager.hasNotificationPermission(context)
                        if (hasPermission && userProfile != null) {
                            isNotificationsEnabled = true
                            HydroNotificationScheduler.startNotifications(context, userProfile)
                        }
                    }
                )

                // Notification Settings (only show if permission granted)
                if (hasPermission && userProfile != null) {
                    NotificationControlsCard(
                        userProfile = userProfile,
                        isEnabled = isNotificationsEnabled,
                        onToggleNotifications = { enabled ->
                            isNotificationsEnabled = enabled
                            coroutineScope.launch {
                                if (enabled) {
                                    HydroNotificationScheduler.startNotifications(context, userProfile)
                                } else {
                                    HydroNotificationScheduler.stopNotifications(context)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasPermission) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasPermission) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (hasPermission) "Notifications Enabled" else "Permission Required",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (hasPermission) {
                        "You'll receive hydration reminders"
                    } else {
                        "Allow notifications to get hydration reminders"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!hasPermission) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Allow")
                }
            }
        }
    }
}

@Composable
private fun NotificationControlsCard(
    userProfile: UserProfile,
    isEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hydration Reminders",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Get reminded every ${userProfile.reminderInterval} minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleNotifications
                )
            }

            if (isEnabled) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Reminder details
                NotificationDetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Reminder Frequency",
                    value = "Every ${userProfile.reminderInterval} minutes"
                )

                NotificationDetailRow(
                    icon = Icons.Default.WbSunny,
                    label = "Active Hours",
                    value = "${userProfile.wakeUpTime} - ${userProfile.sleepTime}"
                )

                NotificationDetailRow(
                    icon = Icons.Default.Style,
                    label = "Reminder Style",
                    value = userProfile.reminderStyle.getDisplayName()
                )

                // Next notification info
                val nextNotificationTime = remember {
                    HydroNotificationScheduler.getNextScheduledTime(context, userProfile)
                }

                if (nextNotificationTime != null) {
                    NotificationDetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Next Reminder",
                        value = nextNotificationTime
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
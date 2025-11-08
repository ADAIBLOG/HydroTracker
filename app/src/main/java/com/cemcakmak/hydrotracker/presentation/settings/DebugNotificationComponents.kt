// DebugNotificationComponents.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/settings/DebugNotificationComponents.kt

package com.cemcakmak.hydrotracker.presentation.settings

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.cemcakmak.hydrotracker.R
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.models.Gender
import com.cemcakmak.hydrotracker.data.models.AgeGroup
import com.cemcakmak.hydrotracker.data.models.ActivityLevel
import com.cemcakmak.hydrotracker.data.database.repository.WaterProgress
import com.cemcakmak.hydrotracker.data.database.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.notifications.HydroNotificationService
import com.cemcakmak.hydrotracker.notifications.HydroNotificationScheduler
import com.cemcakmak.hydrotracker.notifications.NotificationPermissionManager
import com.cemcakmak.hydrotracker.presentation.common.showSuccessSnackbar
import com.cemcakmak.hydrotracker.presentation.common.showStackedSuccessSnackbar
import com.cemcakmak.hydrotracker.presentation.common.showStackedErrorSnackbar
import com.cemcakmak.hydrotracker.presentation.common.showStackedWarningSnackbar
import com.cemcakmak.hydrotracker.presentation.common.showStackedInfoSnackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * Debug notification tools for the Settings screen
 * Provides testing and debugging capabilities for the notification system
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DebugNotificationSection(
    userProfile: UserProfile?,
    waterIntakeRepository: WaterIntakeRepository,
    snackbarHostState: SnackbarHostState,
    isVisible: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(600, delayMillis = 500))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
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
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                    text = stringResource(R.string.settings_notification_debug_tools),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                }

                Text(
                    text = stringResource(R.string.settings_notification_debug_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )

                // Test Notification Button
                DebugActionButton(
                    title = stringResource(R.string.settings_send_test_notification),
                    description = stringResource(R.string.settings_show_hydration_reminder),
                    icon = Icons.Default.NotificationImportant,
                    snackbarHostState = snackbarHostState,
                    onClick = {
                        if (userProfile != null) {
                            // Get current water progress for realistic test
                            coroutineScope.launch {
                                val currentProgress = try {
                                    waterIntakeRepository.getTodayProgress().first()
                                } catch (e: Exception) {
                                    // Fallback progress if repository fails
                                    WaterProgress(
                                        currentIntake = 1134.0,
                                        dailyGoal = userProfile.dailyWaterGoal,
                                        progress = 0.42f,
                                        isGoalAchieved = false,
                                        remainingAmount = userProfile.dailyWaterGoal - 1134.0
                                    )
                                }

                                val notificationService = HydroNotificationService(context)
                                notificationService.showTestNotification(userProfile, currentProgress)
                            }
                        } else {
                            // Fallback if no user profile
                            val notificationService = HydroNotificationService(context)
                            val fallbackProfile = UserProfile(
                                name = "Test User",
                                gender = Gender.MALE,
                                ageGroup = AgeGroup.ADULT_31_50,
                                activityLevel = ActivityLevel.MODERATE,
                                wakeUpTime = "07:00",
                                sleepTime = "23:00",
                                dailyWaterGoal = 2700.0,
                                reminderInterval = 120,
                                isOnboardingCompleted = true
                            )
                            val fallbackProgress = WaterProgress(
                                currentIntake = 1134.0,
                                dailyGoal = 2700.0,
                                progress = 0.42f,
                                isGoalAchieved = false,
                                remainingAmount = 1566.0
                            )
                            notificationService.showTestNotification(fallbackProfile, fallbackProgress)
                        }
                    },
                    confirmationMessage = stringResource(R.string.settings_test_notification_sent)
                )

                // Permission Status Button
                DebugInfoButton(
                    title = stringResource(R.string.settings_check_notification_permission),
                    description = stringResource(R.string.settings_display_permission_status),
                    icon = Icons.Default.Security,
                    onClick = {
                        coroutineScope.launch {
                            val hasPermission = NotificationPermissionManager.hasNotificationPermission(context)
                            snackbarHostState.showSnackbar(
                                message = stringResource(R.string.settings_notification_permission_status, if (hasPermission) stringResource(R.string.settings_granted) else stringResource(R.string.settings_denied))
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                )

                // Restart Notifications Button
                if (userProfile != null) {
                    DebugActionButton(
                        title = stringResource(R.string.settings_restart_notification_schedule),
                        description = stringResource(R.string.settings_cancel_reschedule_notifications),
                        icon = Icons.Default.RestartAlt,
                        snackbarHostState = snackbarHostState,
                        onClick = {
                            HydroNotificationScheduler.rescheduleNotifications(context, userProfile)
                        },
                        confirmationMessage = stringResource(R.string.settings_notifications_rescheduled)
                    )

                    // Next Notification Info
                    DebugInfoButton(
                        title = stringResource(R.string.settings_next_scheduled_notification),
                        description = stringResource(R.string.settings_show_next_notification_time),
                        icon = Icons.Default.Schedule,
                        onClick = {
                            coroutineScope.launch {
                                val nextTime = HydroNotificationScheduler.getNextScheduledTime(context, userProfile)
                                val message = if (nextTime != null) {
                                    stringResource(R.string.settings_next_notification_time, nextTime)
                                } else {
                                    stringResource(R.string.settings_no_notification_scheduled)
                                }
                                snackbarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    )

                    // Debug System Info Button
                    DebugInfoButton(
                        title = stringResource(R.string.settings_system_alarm_info),
                        description = stringResource(R.string.settings_check_exact_alarm_scheduling),
                        icon = Icons.Default.AlarmOn,
                        onClick = {
                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                            val canSchedule = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                alarmManager.canScheduleExactAlarms()
                            } else {
                                true
                            }
                            
                            coroutineScope.launch {
                                val message = buildString {
                                    append(stringResource(R.string.settings_can_schedule_exact_alarms, canSchedule) + "\n")
                                    append(stringResource(R.string.settings_android_version, android.os.Build.VERSION.SDK_INT) + "\n")
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                        val nextAlarm = alarmManager.nextAlarmClock
                                        if (nextAlarm != null) {
                                            append(stringResource(R.string.settings_next_system_alarm, java.util.Date(nextAlarm.triggerTime)))
                                        } else {
                                            append(stringResource(R.string.settings_no_system_alarms))
                                        }
                                    }
                                }
                                snackbarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    )
                }

                // 1-Minute Debug Interval Button
                if (userProfile != null) {
                    DebugActionButton(
                        title = stringResource(R.string.settings_enable_1minute_interval),
                        description = stringResource(R.string.settings_set_notification_interval_1minute),
                        icon = Icons.Default.Timer,
                        snackbarHostState = snackbarHostState,
                        onClick = {
                            val testProfile = userProfile.copy(reminderInterval = 1)
                            HydroNotificationScheduler.rescheduleNotifications(context, testProfile)
                        },
                        confirmationMessage = stringResource(R.string.settings_notifications_set_to_1minute)
                    )
                }

                // Stop All Notifications Button
                DebugActionButton(
                    title = stringResource(R.string.settings_stop_all_notifications),
                    description = stringResource(R.string.settings_cancel_clear_notifications),
                    icon = Icons.Default.NotificationsOff,
                    snackbarHostState = snackbarHostState,
                    onClick = {
                        HydroNotificationScheduler.stopNotifications(context)
                    },
                    confirmationMessage = stringResource(R.string.settings_all_notifications_stopped)
                )

                // Test Stacking Snackbars Button
                DebugActionButton(
                    title = stringResource(R.string.settings_test_stacking_snackbars),
                    description = stringResource(R.string.settings_show_multiple_snackbars),
                    icon = Icons.Default.Layers,
                    snackbarHostState = snackbarHostState,
                    onClick = {
                        // Show multiple stacked snackbars
                        showStackedSuccessSnackbar(stringResource(R.string.settings_first_success_message))
                        showStackedInfoSnackbar(stringResource(R.string.settings_second_info_message))
                        showStackedWarningSnackbar(stringResource(R.string.settings_third_warning_message))
                        showStackedErrorSnackbar(stringResource(R.string.settings_fourth_error_message))
                    },
                    confirmationMessage = stringResource(R.string.settings_stacked_snackbars_shown)
                )

                // Current Status Card
                if (userProfile != null) {
                    NotificationStatusCard(userProfile = userProfile)
                }
            }
        }
    }
}

@Composable
private fun DebugActionButton(
    title: String,
    description: String,
    icon: ImageVector,
    snackbarHostState: SnackbarHostState,
    onClick: () -> Unit,
    confirmationMessage: String
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "debug_button_press"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Show confirmation snackbar
    LaunchedEffect(isPressed) {
        if (isPressed) {
            coroutineScope.launch {
                snackbarHostState.showSuccessSnackbar(
                    message = confirmationMessage
                )
            }
            delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun DebugInfoButton(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NotificationStatusCard(userProfile: UserProfile) {
    val context = LocalContext.current
    val hasPermission = NotificationPermissionManager.hasNotificationPermission(context)
    val shouldEnable = HydroNotificationScheduler.shouldEnableNotifications(context, userProfile)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_current_notification_status),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = stringResource(R.string.settings_permission_status, if (hasPermission) stringResource(R.string.settings_granted) else stringResource(R.string.settings_denied)),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = stringResource(R.string.settings_should_enable, if (shouldEnable) stringResource(R.string.settings_yes) else stringResource(R.string.settings_no)),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = stringResource(R.string.settings_reminder_interval, userProfile.reminderInterval),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = stringResource(R.string.settings_reminder_style, userProfile.reminderStyle.getDisplayName()),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = stringResource(R.string.settings_active_hours, userProfile.wakeUpTime, userProfile.sleepTime),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

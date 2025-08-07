// HydroNotificationScheduler.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/notifications/HydroNotificationScheduler.kt

package com.cemcakmak.hydrotracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.repository.WaterIntakeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Manages scheduling of hydration reminder notifications using AlarmManager
 * Handles precise timing, sleep hours, and rescheduling based on user activity
 */
object HydroNotificationScheduler {

    private const val NOTIFICATION_REQUEST_CODE = 2001
    private const val TAG = "HydroNotificationScheduler"

    /**
     * Start notification scheduling for a user
     * Called when onboarding is completed and permissions are granted
     */
    fun startNotifications(context: Context, userProfile: UserProfile) {
        Log.d(TAG, "Starting notifications for user")
        
        if (!NotificationPermissionManager.hasNotificationPermission(context)) {
            Log.w(TAG, "Cannot start notifications: permission not granted")
            return
        }

        if (!userProfile.isOnboardingCompleted) {
            Log.w(TAG, "Cannot start notifications: onboarding not completed")
            return
        }

        // Cancel any existing notifications first
        stopNotifications(context)

        // Schedule the first notification
        scheduleNextReminder(context, userProfile)
        Log.d(TAG, "Notifications started successfully")
    }

    /**
     * Schedule the next reminder based on user profile and current progress
     */
    fun scheduleNextReminder(context: Context, userProfile: UserProfile) {
        Log.d(TAG, "Scheduling next reminder")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userRepository = UserRepository(context)
                val waterIntakeRepository = com.cemcakmak.hydrotracker.data.database.DatabaseInitializer
                    .getWaterIntakeRepository(context, userRepository)

                // Get current progress to check if goal is achieved
                val currentProgress = waterIntakeRepository.getTodayProgress().first()
                Log.d(TAG, "Current progress: ${currentProgress.progress}, goal achieved: ${currentProgress.isGoalAchieved}")

                // Don't schedule if goal is achieved
                if (currentProgress.isGoalAchieved) {
                    Log.d(TAG, "Goal achieved, not scheduling next reminder")
                    return@launch
                }

                val nextReminderTime = calculateNextReminderTime(userProfile)

                if (nextReminderTime == null) {
                    Log.w(TAG, "Could not calculate next reminder time")
                    return@launch
                }

                // Only schedule if the time is within waking hours
                if (isWithinWakingHours(nextReminderTime, userProfile)) {
                    scheduleNotification(context, nextReminderTime)
                    Log.d(TAG, "Next reminder scheduled for: ${nextReminderTime.time}")
                } else {
                    Log.d(TAG, "Next reminder time ${nextReminderTime.time} is outside waking hours, not scheduling")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling next reminder", e)
            }
        }
    }

    /**
     * Calculate the next reminder time based on user's schedule and interval
     */
    private fun calculateNextReminderTime(userProfile: UserProfile): Calendar? {
        val now = Calendar.getInstance()
        val currentTime = LocalTime.now()

        val wakeUpTime = parseTime(userProfile.wakeUpTime)
        val sleepTime = parseTime(userProfile.sleepTime)

        if (wakeUpTime == null || sleepTime == null) {
            Log.e(TAG, "Failed to parse wake up time (${userProfile.wakeUpTime}) or sleep time (${userProfile.sleepTime})")
            return null
        }

        Log.d(TAG, "Current time: $currentTime, wake up: $wakeUpTime, sleep: $sleepTime, interval: ${userProfile.reminderInterval}min")

        // Check if we're currently in sleep hours
        if (isInSleepHours(currentTime, wakeUpTime, sleepTime)) {
            Log.d(TAG, "Currently in sleep hours, scheduling for tomorrow after wake up")
            // Schedule first notification after wake up time + interval
            val tomorrow = now.clone() as Calendar
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)
            tomorrow.set(Calendar.HOUR_OF_DAY, wakeUpTime.hour)
            tomorrow.set(Calendar.MINUTE, wakeUpTime.minute)
            tomorrow.set(Calendar.SECOND, 0)
            tomorrow.set(Calendar.MILLISECOND, 0)

            // Add the reminder interval to wake up time
            tomorrow.add(Calendar.MINUTE, userProfile.reminderInterval)
            return tomorrow
        }

        // Calculate next reminder time
        val nextReminder = now.clone() as Calendar
        nextReminder.add(Calendar.MINUTE, userProfile.reminderInterval)

        val nextReminderTime = LocalTime.of(
            nextReminder.get(Calendar.HOUR_OF_DAY),
            nextReminder.get(Calendar.MINUTE)
        )

        Log.d(TAG, "Next reminder would be at: $nextReminderTime")

        // Check if next reminder would be in sleep hours
        if (isInSleepHours(nextReminderTime, wakeUpTime, sleepTime)) {
            Log.d(TAG, "Next reminder would be in sleep hours, scheduling for tomorrow after wake up")
            // Schedule for tomorrow after wake up + interval
            val tomorrow = now.clone() as Calendar
            tomorrow.add(Calendar.DAY_OF_YEAR, 1)
            tomorrow.set(Calendar.HOUR_OF_DAY, wakeUpTime.hour)
            tomorrow.set(Calendar.MINUTE, wakeUpTime.minute)
            tomorrow.set(Calendar.SECOND, 0)
            tomorrow.set(Calendar.MILLISECOND, 0)
            tomorrow.add(Calendar.MINUTE, userProfile.reminderInterval)
            return tomorrow
        }

        Log.d(TAG, "Scheduling next reminder for: ${nextReminder.time}")
        return nextReminder
    }

    /**
     * Check if current time is in sleep hours
     */
    private fun isInSleepHours(currentTime: LocalTime, wakeUpTime: LocalTime, sleepTime: LocalTime): Boolean {
        return if (sleepTime.isAfter(wakeUpTime)) {
            // Same day sleep (e.g., wake 07:00, sleep 23:00)
            currentTime.isBefore(wakeUpTime) || currentTime.isAfter(sleepTime)
        } else {
            // Next day sleep (e.g., wake 07:00, sleep 01:00)
            currentTime.isBefore(wakeUpTime) && currentTime.isAfter(sleepTime)
        }
    }

    /**
     * Check if the given time is within waking hours
     */
    private fun isWithinWakingHours(calendar: Calendar, userProfile: UserProfile): Boolean {
        val time = LocalTime.of(
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        val wakeUpTime = parseTime(userProfile.wakeUpTime)
        val sleepTime = parseTime(userProfile.sleepTime)

        if (wakeUpTime == null || sleepTime == null) {
            return true // Default to allowing if parsing fails
        }

        return !isInSleepHours(time, wakeUpTime, sleepTime)
    }

    /**
     * Schedule notification using AlarmManager
     */
    private fun scheduleNotification(context: Context, triggerTime: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, HydroNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Check if we can schedule exact alarms (needed for Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e(TAG, "Cannot schedule exact alarms - permission not granted")
                    return
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm (allow while idle) for: ${triggerTime.time}")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact alarm for: ${triggerTime.time}")
            }

            // Verify the alarm was scheduled by checking next alarm clock info
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val nextAlarm = alarmManager.nextAlarmClock
                if (nextAlarm != null) {
                    Log.d(TAG, "Next system alarm: ${Date(nextAlarm.triggerTime)}")
                }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule notification due to security exception", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule notification", e)
        }
    }

    /**
     * Stop all scheduled notifications
     */
    fun stopNotifications(context: Context) {
        Log.d(TAG, "Stopping all notifications")
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, HydroNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled scheduled alarms")

        // Also cancel any visible notifications
        val notificationService = HydroNotificationService(context)
        notificationService.cancelAllNotifications()
        Log.d(TAG, "Cancelled visible notifications")

        Log.d(TAG, "All notifications stopped")
    }

    /**
     * Reschedule notifications when user profile changes
     */
    fun rescheduleNotifications(context: Context, userProfile: UserProfile) {
        stopNotifications(context)
        startNotifications(context, userProfile)
    }

    /**
     * Parse time string (HH:mm) to LocalTime
     */
    private fun parseTime(timeString: String): LocalTime? {
        return try {
            LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if notifications should be enabled for this user
     */
    fun shouldEnableNotifications(context: Context, userProfile: UserProfile): Boolean {
        return NotificationPermissionManager.hasNotificationPermission(context) &&
                userProfile.isOnboardingCompleted
    }

    /**
     * Get next scheduled notification time for debugging
     */
    fun getNextScheduledTime(context: Context, userProfile: UserProfile): String? {
        val nextTime = calculateNextReminderTime(userProfile)
        return nextTime?.let {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            LocalDateTime.ofInstant(it.toInstant(), it.timeZone.toZoneId()).format(formatter)
        }
    }
}
package com.cemcakmak.hydrotracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.cemcakmak.hydrotracker.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing user data persistence
 * Following Android Architecture Components patterns
 */
class UserRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "hydrotracker_prefs",
        Context.MODE_PRIVATE
    )

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        // Add debug logging
        println("UserRepository: Initializing...")
        val currentCompletionStatus = prefs.getBoolean("onboarding_completed", false)
        println("UserRepository: Current onboarding_completed = $currentCompletionStatus")

        loadUserProfile()
        // Ensure we emit the initial state immediately
        _isOnboardingCompleted.value = prefs.getBoolean("onboarding_completed", false)

        println("UserRepository: Initialization complete. Final status = ${_isOnboardingCompleted.value}")
    }

    /**
     * Save user profile to SharedPreferences
     */
    fun saveUserProfile(profile: UserProfile) {
        println("UserRepository: Saving user profile...")
        println("UserRepository: Profile onboarding completed = ${profile.isOnboardingCompleted}")

        prefs.edit().apply {
            putString("gender", profile.gender.name)
            putString("age_group", profile.ageGroup.name)
            putString("activity_level", profile.activityLevel.name)
            putString("wake_up_time", profile.wakeUpTime)
            putString("sleep_time", profile.sleepTime)
            putFloat("daily_water_goal", profile.dailyWaterGoal.toFloat())
            putInt("reminder_interval", profile.reminderInterval)
            putBoolean("onboarding_completed", profile.isOnboardingCompleted)
            putString("reminder_style", profile.reminderStyle.name)

            // Optional fields
            profile.weight?.let { putFloat("weight", it.toFloat()) }
            profile.preferredThemeColor?.let { putString("preferred_theme_color", it) }
            putBoolean("use_system_theme", profile.useSystemTheme)

            apply()
        }

        println("UserRepository: Profile saved to SharedPreferences")

        // Immediately update StateFlows
        _userProfile.value = profile
        _isOnboardingCompleted.value = profile.isOnboardingCompleted

        println("UserRepository: StateFlows updated - isOnboardingCompleted = ${_isOnboardingCompleted.value}")

        // Verify the save worked
        val savedValue = prefs.getBoolean("onboarding_completed", false)
        println("UserRepository: Verification - SharedPreferences onboarding_completed = $savedValue")
    }

    /**
     * Load user profile from SharedPreferences
     */
    private fun loadUserProfile() {
        try {
            val isCompleted = prefs.getBoolean("onboarding_completed", false)
            _isOnboardingCompleted.value = isCompleted

            if (isCompleted) {
                val genderName = prefs.getString("gender", null)
                val ageGroupName = prefs.getString("age_group", null)
                val activityLevelName = prefs.getString("activity_level", null)

                if (genderName != null && ageGroupName != null && activityLevelName != null) {
                    val profile = UserProfile(
                        gender = Gender.valueOf(genderName),
                        ageGroup = AgeGroup.valueOf(ageGroupName),
                        activityLevel = ActivityLevel.valueOf(activityLevelName),
                        wakeUpTime = prefs.getString("wake_up_time", "07:00") ?: "07:00",
                        sleepTime = prefs.getString("sleep_time", "23:00") ?: "23:00",
                        dailyWaterGoal = prefs.getFloat("daily_water_goal", 2700f).toDouble(),
                        reminderInterval = prefs.getInt("reminder_interval", 120),
                        isOnboardingCompleted = true,
                        weight = prefs.getFloat("weight", 0f).let { if (it > 0) it.toDouble() else null },
                        preferredThemeColor = prefs.getString("preferred_theme_color", null),
                        useSystemTheme = prefs.getBoolean("use_system_theme", true),
                        reminderStyle = ReminderStyle.valueOf(
                            prefs.getString("reminder_style", ReminderStyle.GENTLE.name) ?: ReminderStyle.GENTLE.name
                        )
                    )

                    _userProfile.value = profile
                } else {
                    // Data is corrupted, reset onboarding
                    _isOnboardingCompleted.value = false
                    clearUserProfile()
                }
            }
        } catch (e: Exception) {
            // Handle any corrupted data by resetting
            _isOnboardingCompleted.value = false
            clearUserProfile()
        }
    }

    /**
     * Clear user profile (for testing or reset purposes)
     */
    fun clearUserProfile() {
        println("UserRepository: Clearing all user data...")
        prefs.edit().clear().apply()
        _userProfile.value = null
        _isOnboardingCompleted.value = false
        println("UserRepository: User data cleared. Onboarding status = ${_isOnboardingCompleted.value}")
    }

    /**
     * Force reset onboarding for testing
     */
    fun resetOnboarding() {
        println("UserRepository: Resetting onboarding...")
        prefs.edit().putBoolean("onboarding_completed", false).apply()
        _isOnboardingCompleted.value = false
        _userProfile.value = null
        println("UserRepository: Onboarding reset complete")
    }

    /**
     * Update theme preferences
     */
    fun updateThemePreferences(themePreferences: ThemePreferences) {
        prefs.edit().apply {
            putBoolean("use_dynamic_color", themePreferences.useDynamicColor)
            putString("dark_mode", themePreferences.darkMode.name)
            putString("color_source", themePreferences.colorSource.name)
            putString("week_start_day", themePreferences.weekStartDay.name)
            apply()
        }
    }

    /**
     * Load theme preferences
     */
    fun loadThemePreferences(): ThemePreferences {
        return ThemePreferences(
            useDynamicColor = prefs.getBoolean("use_dynamic_color", false),
            darkMode = DarkModePreference.valueOf(
                prefs.getString("dark_mode", DarkModePreference.SYSTEM.name) ?: DarkModePreference.SYSTEM.name
            ),
            colorSource = ColorSource.valueOf(
                prefs.getString("color_source", ColorSource.HYDRO_THEME.name) ?: ColorSource.HYDRO_THEME.name
            ),
            weekStartDay = WeekStartDay.valueOf(
                prefs.getString("week_start_day", WeekStartDay.MONDAY.name) ?: WeekStartDay.MONDAY.name
            )
        )
    }
}
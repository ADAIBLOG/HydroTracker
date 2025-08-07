package com.cemcakmak.hydrotracker.data.models

/**
 * User profile data model following Material 3 Expressive principles
 * Supports personalized theming and compassionate health design
 */
data class UserProfile(
    val id: Int = 1,
    val gender: Gender,
    val ageGroup: AgeGroup,
    val weight: Double? = null, // in kg (optional for more precise calculation)
    val activityLevel: ActivityLevel,
    val wakeUpTime: String, // HH:mm format (e.g., "07:00")
    val sleepTime: String, // HH:mm format (e.g., "23:00")
    val dailyWaterGoal: Double, // calculated target in milliliters
    val reminderInterval: Int, // reminder frequency in minutes
    val isOnboardingCompleted: Boolean = false,
    val preferredThemeColor: String? = null, // For custom color themes
    val useSystemTheme: Boolean = true, // Material 3 dynamic color
    val reminderStyle: ReminderStyle = ReminderStyle.GENTLE
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER;

    fun getDisplayName(): String {
        return when (this) {
            MALE -> "Male"
            FEMALE -> "Female"
            OTHER -> "Prefer not to say"
        }
    }

    // MD3 Expressive: Use inclusive, compassionate language
    fun getPersonalizedGreeting(): String {
        return when (this) {
            MALE -> "Stay hydrated, champion!"
            FEMALE -> "Keep glowing with hydration!"
            OTHER -> "You've got this, hydration hero!"
        }
    }
}

enum class AgeGroup {
    YOUNG_ADULT_18_30,
    ADULT_31_50,
    MIDDLE_AGED_51_60,
    SENIOR_60_PLUS;

    fun getDisplayName(): String {
        return when (this) {
            YOUNG_ADULT_18_30 -> "18-30 years"
            ADULT_31_50 -> "31-50 years"
            MIDDLE_AGED_51_60 -> "51-60 years"
            SENIOR_60_PLUS -> "60+ years"
        }
    }

    fun getAgeRange(): Pair<Int, Int> {
        return when (this) {
            YOUNG_ADULT_18_30 -> 18 to 30
            ADULT_31_50 -> 31 to 50
            MIDDLE_AGED_51_60 -> 51 to 60
            SENIOR_60_PLUS -> 60 to 100
        }
    }

    // MD3 Expressive: Age-appropriate messaging
    fun getMotivationalMessage(): String {
        return when (this) {
            YOUNG_ADULT_18_30 -> "Your body is building its foundation - keep it hydrated!"
            ADULT_31_50 -> "Hydration is your energy source - fuel your busy life!"
            MIDDLE_AGED_51_60 -> "Stay refreshed and vibrant with proper hydration!"
            SENIOR_60_PLUS -> "Hydration supports your wellness journey every day!"
        }
    }
}

enum class ActivityLevel {
    SEDENTARY,      // Little to no exercise
    LIGHT,          // Light exercise 1-3 days/week
    MODERATE,       // Moderate exercise 3-5 days/week
    ACTIVE,         // Heavy exercise 6-7 days/week
    VERY_ACTIVE;    // Very heavy exercise, physical job

    fun getDisplayName(): String {
        return when (this) {
            SEDENTARY -> "Sedentary"
            LIGHT -> "Light Activity"
            MODERATE -> "Moderate Activity"
            ACTIVE -> "Active"
            VERY_ACTIVE -> "Very Active"
        }
    }

    fun getDescription(): String {
        return when (this) {
            SEDENTARY -> "Little to no exercise"
            LIGHT -> "Light exercise 1-3 days/week"
            MODERATE -> "Moderate exercise 3-5 days/week"
            ACTIVE -> "Heavy exercise 6-7 days/week"
            VERY_ACTIVE -> "Very heavy exercise or physical job"
        }
    }

    // Multiplier for water intake calculation
    fun getActivityMultiplier(): Double {
        return when (this) {
            SEDENTARY -> 1.0
            LIGHT -> 1.1
            MODERATE -> 1.2
            ACTIVE -> 1.3
            VERY_ACTIVE -> 1.5
        }
    }

    // MD3 Expressive: Activity-specific encouragement
    fun getHydrationTip(): String {
        return when (this) {
            SEDENTARY -> "Even at rest, your body needs consistent hydration!"
            LIGHT -> "Light activity still means you need extra hydration support!"
            MODERATE -> "Regular exercise requires regular hydration - you're doing great!"
            ACTIVE -> "Your active lifestyle deserves premium hydration care!"
            VERY_ACTIVE -> "High performance demands high hydration - you're a champion!"
        }
    }
}

// MD3 Expressive: Compassionate reminder styles
enum class ReminderStyle {
    GENTLE,      // Soft, encouraging reminders
    MOTIVATING,  // Energetic, goal-focused reminders
    MINIMAL;     // Simple, unobtrusive reminders

    fun getDisplayName(): String {
        return when (this) {
            GENTLE -> "Gentle & Caring"
            MOTIVATING -> "Motivating & Energetic"
            MINIMAL -> "Simple & Clean"
        }
    }

    fun getSampleReminder(): String {
        return when (this) {
            GENTLE -> "💧 Time for a refreshing sip of water"
            MOTIVATING -> "🚀 Fuel your success with hydration!"
            MINIMAL -> "💧 Water reminder"
        }
    }
}
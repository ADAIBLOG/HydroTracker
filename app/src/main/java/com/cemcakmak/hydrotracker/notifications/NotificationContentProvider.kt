// NotificationContentProvider.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/notifications/NotificationContentProvider.kt

package com.cemcakmak.hydrotracker.notifications

import com.cemcakmak.hydrotracker.data.models.ReminderStyle
import kotlin.random.Random

/**
 * Provides creative and personalized notification content
 * Including water puns, facts, and motivational messages
 */
object NotificationContentProvider {

    /**
     * Get notification content based on user's reminder style and progress
     */
    fun getNotificationContent(
        reminderStyle: ReminderStyle,
        userName: String? = null,
        currentProgress: Float = 0f,
        dailyGoal: Double = 2700.0
    ): NotificationContent {
        return when (reminderStyle) {
            ReminderStyle.GENTLE -> getGentleContent(userName, currentProgress, dailyGoal)
            ReminderStyle.MOTIVATING -> getMotivatingContent(userName, currentProgress, dailyGoal)
            ReminderStyle.MINIMAL -> getMinimalContent(currentProgress, dailyGoal)
        }
    }

    private fun getGentleContent(userName: String?, progress: Float, goal: Double): NotificationContent {
        val titles = listOf(
            "💧 Gentle Hydration Reminder",
            "🌊 Time for Some Water",
            "💙 Your Body is Calling",
            "🌸 Gentle Hydration Break",
            "💧 Soft Reminder from HydroTracker"
        )

        val messages = when {
            progress < 0.25f -> listOf(
                "Your body would love some refreshing water right now ✨",
                "How about a sip of something refreshing? 💧",
                "A gentle reminder to nurture yourself with water 🌱",
                "Time to give your body the hydration it deserves 💙",
                "Let's start building that healthy hydration habit 🌊"
            )
            progress < 0.5f -> listOf(
                "You're making great progress! Keep it flowing 🌊",
                "Halfway there! Your body appreciates every drop 💧",
                "Looking good! Time for another refreshing moment ✨",
                "Your hydration journey is flowing beautifully 🌸",
                "Keep up the wonderful work! Another sip awaits 💙"
            )
            progress < 0.75f -> listOf(
                "Almost there! You're doing wonderfully 🌟",
                "Your dedication to hydration is inspiring! 💧",
                "So close to your goal! Keep flowing forward 🌊",
                "Your body is thanking you for this care 💙",
                "Beautiful progress! Just a bit more to go ✨"
            )
            else -> listOf(
                "You're so close to achieving your daily goal! 🎉",
                "Final stretch! Your consistency is amazing 💧",
                "Almost at the finish line! You've got this 🌟",
                "Your dedication today has been incredible 💙",
                "One more push to complete your hydration victory! 🏆"
            )
        }

        val funFacts = getRandomWaterFact()

        return NotificationContent(
            title = titles.random(),
            message = "${messages.random()}\n\n💡 $funFacts",
            progress = progress
        )
    }

    private fun getMotivatingContent(userName: String?, progress: Float, goal: Double): NotificationContent {
        val titles = listOf(
            "💪 Hydration Champion!",
            "🚀 Water Warrior Alert!",
            "⚡ Power Up with H2O!",
            "🏆 Hydration Hero Time!",
            "🔥 Fuel Your Success!"
        )

        val messages = when {
            progress < 0.25f -> listOf(
                "Time to CRUSH your hydration goals! Let's GO! 🚀",
                "Your SUCCESS starts with the next sip! 💪",
                "Champions hydrate! Are you ready to DOMINATE? ⚡",
                "FUEL your potential with premium H2O! 🔥",
                "Winners stay hydrated! Time to LEVEL UP! 🏆"
            )
            progress < 0.5f -> listOf(
                "UNSTOPPABLE! You're building momentum! 🚀",
                "CRUSHING IT! Halfway to hydration victory! 💪",
                "POWERFUL progress! Keep that energy flowing! ⚡",
                "AMAZING work! You're on fire today! 🔥",
                "CHAMPION mindset! Push forward! 🏆"
            )
            progress < 0.75f -> listOf(
                "INCREDIBLE dedication! Victory is within reach! 🚀",
                "OUTSTANDING! You're in the winner's zone! 💪",
                "PHENOMENAL! Final quarter - you've got this! ⚡",
                "EXCELLENCE in action! Keep dominating! 🔥",
                "LEGENDARY persistence! Almost at the summit! 🏆"
            )
            else -> listOf(
                "FINAL PUSH! Greatness awaits! 🚀",
                "SO CLOSE to TOTAL VICTORY! 💪",
                "MAXIMUM effort for MAXIMUM results! ⚡",
                "ULTIMATE hydration hero! Finish strong! 🔥",
                "LEGENDARY status incoming! Complete the mission! 🏆"
            )
        }

        val puns = getRandomWaterPun()

        return NotificationContent(
            title = titles.random(),
            message = "${messages.random()}\n\n😄 $puns",
            progress = progress
        )
    }

    private fun getMinimalContent(progress: Float, goal: Double): NotificationContent {
        val remaining = ((1 - progress) * goal).toInt()

        return NotificationContent(
            title = "💧 Water reminder",
            message = when {
                progress < 0.5f -> "Time to hydrate"
                progress < 0.8f -> "Continue hydrating"
                else -> "${remaining}ml remaining"
            },
            progress = progress
        )
    }

    private fun getRandomWaterFact(): String {
        val facts = listOf(
            "Your brain is 75% water - feed it well!",
            "Water helps regulate your body temperature",
            "Proper hydration can improve your mood and concentration",
            "Your muscles are 75% water",
            "Water helps transport nutrients throughout your body",
            "Staying hydrated can boost your energy levels",
            "Water helps your kidneys filter waste efficiently",
            "Proper hydration supports healthy skin",
            "Your blood is 90% water",
            "Water helps lubricate your joints",
            "Hydration can improve your physical performance",
            "Water aids in digestion and nutrient absorption",
            "Staying hydrated helps maintain healthy blood pressure",
            "Water helps prevent kidney stones",
            "Your heart works more efficiently when you're hydrated"
        )
        return facts.random()
    }

    private fun getRandomWaterPun(): String {
        val puns = listOf(
            "Water you waiting for? Let's hydrate!",
            "Don't let your hydration goals go down the drain!",
            "H2-Oh yeah! Time for water!",
            "You're one in a mill-ion! Stay hydrated!",
            "Water wonderful day to stay hydrated!",
            "Sea what happens when you drink more water!",
            "Don't be a drip - drink up!",
            "Water pressure? We prefer hydration pleasure!",
            "Make waves with your hydration game!",
            "Pool your energy and drink some water!",
            "Current mood: Needs more water!",
            "Tide yourself over with some H2O!",
            "Water good choice to stay hydrated!",
            "Flow with the hydration rhythm!",
            "Sink or swim - we choose hydrate!"
        )
        return puns.random()
    }
}

/**
 * Data class for notification content
 */
data class NotificationContent(
    val title: String,
    val message: String,
    val progress: Float
)
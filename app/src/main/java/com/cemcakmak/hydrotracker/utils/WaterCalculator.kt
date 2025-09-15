package com.cemcakmak.hydrotracker.utils

import android.util.Log
import com.cemcakmak.hydrotracker.data.models.ActivityLevel
import com.cemcakmak.hydrotracker.data.models.AgeGroup
import com.cemcakmak.hydrotracker.data.models.Gender
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

/**
 * Utility class for calculating daily water intake goals based on scientific research
 *
 * Based on:
 * - Institute of Medicine (IOM) recommendations
 * - European Food Safety Authority (EFSA) guidelines
 * - Activity level adjustments from sports medicine research
 */
object WaterCalculator {

    // Base recommendations in milliliters (IOM guidelines)
    private const val BASE_MALE_INTAKE = 3700.0 // 3.7L for men
    private const val BASE_FEMALE_INTAKE = 2700.0 // 2.7L for women

    // Weight-based calculation factor (0.67 * weight in pounds converted to kg)
    private const val WEIGHT_FACTOR_ML_PER_KG = 30.0 // approximately 0.67 * 2.205 * 20

    /**
     * Calculates daily water intake goal based on user profile
     *
     * @param gender User's gender
     * @param ageGroup User's age group
     * @param activityLevel User's activity level
     * @param weight User's weight in kg (optional for more precise calculation)
     * @return Daily water intake goal in milliliters
     */
    fun calculateDailyWaterGoal(
        gender: Gender,
        ageGroup: AgeGroup,
        activityLevel: ActivityLevel,
        weight: Double? = null
    ): Double {

        // Step 1: Get base intake based on gender and IOM guidelines
        val baseIntake = when (gender) {
            Gender.MALE -> BASE_MALE_INTAKE
            Gender.FEMALE -> BASE_FEMALE_INTAKE
            Gender.OTHER -> (BASE_MALE_INTAKE + BASE_FEMALE_INTAKE) / 2 // Average
        }

        // Step 2: Apply age group adjustment
        val ageAdjustedIntake = applyAgeAdjustment(baseIntake, ageGroup)

        // Step 3: Apply activity level multiplier
        val activityAdjustedIntake = ageAdjustedIntake * activityLevel.getActivityMultiplier()

        // Step 4: If weight is provided, use weight-based validation
        val finalIntake = weight?.let { weightKg ->
            val weightBasedIntake = weightKg * WEIGHT_FACTOR_ML_PER_KG
            // Use the higher of the two calculations for safety
            max(activityAdjustedIntake, weightBasedIntake)
        } ?: activityAdjustedIntake

        // Step 5: Ensure reasonable bounds (1.5L minimum, 5L maximum for safety)
        return finalIntake.coerceIn(1500.0, 5000.0)
    }

    /**
     * Applies age-based adjustments to water intake
     * Research shows older adults typically need slightly less water due to reduced metabolism
     */
    private fun applyAgeAdjustment(baseIntake: Double, ageGroup: AgeGroup): Double {
        return when (ageGroup) {
            AgeGroup.YOUNG_ADULT_18_30 -> baseIntake * 1.0 // No adjustment
            AgeGroup.ADULT_31_50 -> baseIntake * 0.98 // Slight reduction
            AgeGroup.MIDDLE_AGED_51_60 -> baseIntake * 0.95 // Moderate reduction
            AgeGroup.SENIOR_60_PLUS -> baseIntake * 0.9 // More significant reduction
        }
    }

    /**
     * Calculates optimal reminder interval based on awake hours and daily goal
     *
     * @param wakeUpTime Wake up time in HH:mm format
     * @param sleepTime Sleep time in HH:mm format
     * @param dailyGoal Daily water goal in milliliters
     * @return Optimal reminder interval in minutes
     */
    fun calculateReminderInterval(
        wakeUpTime: String,
        sleepTime: String,
        dailyGoal: Double
    ): Int {
        val awakeHours = calculateAwakeHours(wakeUpTime, sleepTime)

        Log.d("Awake Hours", "Awake hours: $awakeHours")

        // Target 8-12 reminders per day for optimal hydration
        val targetReminders = when {
            dailyGoal < 2000 -> 8 // Lower goal = fewer reminders
            dailyGoal < 3000 -> 10 // Medium goal = medium reminders
            else -> 12 // High goal = more frequent reminders
        }

        val intervalMinutes = ((awakeHours * 60) / targetReminders).toInt()

        return intervalMinutes
    }

    /**
     * Calculates awake hours from wake up and sleep times
     */
    private fun calculateAwakeHours(wakeUpTime: String, sleepTime: String): Double {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val wakeUp = LocalTime.parse(wakeUpTime, formatter)
            val sleep = LocalTime.parse(sleepTime, formatter)

            val awakeMinutes = if (sleep.isAfter(wakeUp)) {
                // Same day (e.g., wake at 07:00, sleep at 23:00)
                sleep.toSecondOfDay() - wakeUp.toSecondOfDay()
            } else {
                // Next day (e.g., wake at 07:00, sleep at 01:00 next day)
                (24 * 3600) - wakeUp.toSecondOfDay() + sleep.toSecondOfDay()
            } / 60

            awakeMinutes / 60.0

        } catch (_: Exception) {
            // Fallback to 16 hours if time parsing fails
            16.0
        }
    }

    /**
     * Formats water amount for display
     *
     * @param amountMl Amount in milliliters
     * @return Formatted string (e.g., "2.5 L" or "750 ml")
     */
    fun formatWaterAmount(amountMl: Double): String {
        return when {
            amountMl >= 1000 -> "${(amountMl / 1000).format(1)} L"
            else -> "${amountMl.toInt()} ml"
        }
    }


    /**
     * Extension function to format Double with specified decimal places
     */
    private fun Double.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
}
// WaterIntakeRepository.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/data/repository/WaterIntakeRepository.kt

package com.cemcakmak.hydrotracker.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cemcakmak.hydrotracker.data.database.dao.WaterIntakeDao
import com.cemcakmak.hydrotracker.data.database.dao.DailySummaryDao
import com.cemcakmak.hydrotracker.data.database.dao.DailyTotal
import com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry
import com.cemcakmak.hydrotracker.data.database.entities.DailySummary
import com.cemcakmak.hydrotracker.data.models.ContainerPreset
import java.text.SimpleDateFormat
import java.util.*
import com.cemcakmak.hydrotracker.widgets.WidgetUpdateHelper
import com.cemcakmak.hydrotracker.utils.UserDayCalculator
import android.content.Context
import android.content.SharedPreferences

class WaterIntakeRepository(
    private val waterIntakeDao: WaterIntakeDao,
    private val dailySummaryDao: DailySummaryDao,
    private val userRepository: UserRepository,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "water_intake_prefs", Context.MODE_PRIVATE
    )

    // Get today's date string based on calendar day
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Get today's user day string based on wake-up time
    private suspend fun getTodayUserDayString(): String {
        val userProfile = userRepository.userProfile.value
        val wakeUpTime = userProfile?.wakeUpTime ?: "07:00"
        return UserDayCalculator.getCurrentUserDayString(wakeUpTime)
    }

    /**
     * Check if a new user day has started and handle the transition
     * Should be called when the app starts or becomes foreground
     */
    suspend fun checkAndHandleNewUserDay() = withContext(Dispatchers.IO) {
        val userProfile = userRepository.userProfile.value ?: return@withContext
        val wakeUpTime = userProfile.wakeUpTime
        val currentTime = System.currentTimeMillis()
        val lastCheckTime = prefs.getLong("last_day_check_time", 0L)

        if (lastCheckTime == 0L) {
            // First time running, just store current time
            prefs.edit().putLong("last_day_check_time", currentTime).apply()
            return@withContext
        }

        if (UserDayCalculator.hasNewUserDayStarted(lastCheckTime, wakeUpTime)) {
            // New user day has started, update widgets to reflect reset
            WidgetUpdateHelper.updateAllWidgets(context)
            
            // Store the new check time
            prefs.edit().putLong("last_day_check_time", currentTime).apply()
        }
    }

    // ===== WATER INTAKE OPERATIONS =====

    suspend fun addWaterIntake(
        amount: Double,
        containerPreset: ContainerPreset,
        note: String? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val userDayString = getTodayUserDayString()
            val entry = WaterIntakeEntry(
                amount = amount,
                timestamp = System.currentTimeMillis(),
                date = userDayString,
                containerType = containerPreset.name,
                containerVolume = containerPreset.volume,
                note = note,
                createdAt = System.currentTimeMillis()
            )

            val entryId = waterIntakeDao.insertEntry(entry)
            updateDailySummaryForDate(userDayString)
            
            // Update widgets after successful water intake
            WidgetUpdateHelper.updateAllWidgets(context)
            
            Result.success(entryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomWaterIntake(
        amount: Double,
        note: String? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val userDayString = getTodayUserDayString()
            val entry = WaterIntakeEntry(
                amount = amount,
                timestamp = System.currentTimeMillis(),
                date = userDayString,
                containerType = "Custom",
                containerVolume = amount,
                note = note,
                createdAt = System.currentTimeMillis()
            )

            val entryId = waterIntakeDao.insertEntry(entry)
            updateDailySummaryForDate(userDayString)
            
            // Update widgets after successful water intake
            WidgetUpdateHelper.updateAllWidgets(context)
            
            Result.success(entryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWaterIntake(entry: WaterIntakeEntry): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            waterIntakeDao.deleteEntry(entry)
            updateDailySummaryForDate(entry.date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWaterIntake(entry: WaterIntakeEntry): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            waterIntakeDao.updateEntry(entry)
            updateDailySummaryForDate(entry.date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== QUERY OPERATIONS =====

    fun getTodayEntries(): Flow<List<WaterIntakeEntry>> {
        return kotlinx.coroutines.flow.flow {
            val userDayString = getTodayUserDayString()
            waterIntakeDao.getEntriesForDate(userDayString).collect { emit(it) }
        }
    }

    fun getTodayTotalIntake(): Flow<Double> {
        return kotlinx.coroutines.flow.flow {
            val userDayString = getTodayUserDayString()
            waterIntakeDao.getTotalIntakeForDate(userDayString).collect { emit(it) }
        }
    }

    fun getEntriesForDate(date: String): Flow<List<WaterIntakeEntry>> {
        return waterIntakeDao.getEntriesForDate(date)
    }

    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<WaterIntakeEntry>> {
        return waterIntakeDao.getEntriesForDateRange(startDate, endDate)
    }

    fun getLast30DaysEntries(): Flow<List<WaterIntakeEntry>> {
        return waterIntakeDao.getLast30DaysEntries()
    }

    // ===== PROGRESS & STATISTICS =====

    fun getTodayProgress(): Flow<WaterProgress> {
        return combine(
            getTodayTotalIntake(),
            userRepository.userProfile
        ) { totalIntake, userProfile ->
            val goal = userProfile?.dailyWaterGoal ?: 2700.0
            WaterProgress(
                currentIntake = totalIntake,
                dailyGoal = goal,
                progress = (totalIntake / goal).toFloat().coerceIn(0f, 1f),
                isGoalAchieved = totalIntake >= goal,
                remainingAmount = maxOf(0.0, goal - totalIntake)
            )
        }
    }

    fun getTodayStatistics(): Flow<TodayStatistics> {
        return combine(
            getTodayEntries(),
            getTodayProgress()
        ) { entries, progress ->
            TodayStatistics(
                totalIntake = progress.currentIntake,
                goalProgress = progress.progress,
                entryCount = entries.size,
                averageIntake = if (entries.isNotEmpty()) progress.currentIntake / entries.size else 0.0,
                largestIntake = entries.maxOfOrNull { it.amount } ?: 0.0,
                firstIntakeTime = entries.minByOrNull { it.timestamp }?.getFormattedTime(),
                lastIntakeTime = entries.maxByOrNull { it.timestamp }?.getFormattedTime(),
                isGoalAchieved = progress.isGoalAchieved,
                remainingAmount = progress.remainingAmount
            )
        }
    }

    suspend fun getWeeklyStatistics(): WeeklyStatistics = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val dailyTotals = waterIntakeDao.getDailyTotals(startDate, endDate)

        val totalIntake = dailyTotals.sumOf { it.totalAmount }
        val averageDaily = if (dailyTotals.isNotEmpty()) totalIntake / 7 else 0.0
        val bestDay = dailyTotals.maxByOrNull { it.totalAmount }
        val totalEntries = dailyTotals.sumOf { it.entryCount }

        WeeklyStatistics(
            totalIntake = totalIntake,
            averageDailyIntake = averageDaily,
            bestDayAmount = bestDay?.totalAmount ?: 0.0,
            bestDayDate = bestDay?.date ?: "",
            totalEntries = totalEntries,
            daysWithData = dailyTotals.size,
            dailyTotals = dailyTotals
        )
    }

    // ===== DAILY SUMMARY OPERATIONS =====

    fun getTodaySummary(): Flow<DailySummary?> {
        return kotlinx.coroutines.flow.flow {
            val userDayString = getTodayUserDayString()
            dailySummaryDao.getSummaryForDate(userDayString).collect { emit(it) }
        }
    }

    fun getLast30DaysSummaries(): Flow<List<DailySummary>> {
        return dailySummaryDao.getLast30DaysSummaries()
    }

    private suspend fun updateDailySummaryForToday() {
        val userDayString = getTodayUserDayString()
        updateDailySummaryForDate(userDayString)
    }

    private suspend fun updateDailySummaryForDate(date: String) = withContext(Dispatchers.IO) {
        try {
            val userProfile = userRepository.userProfile.value
            val dailyGoal = userProfile?.dailyWaterGoal ?: 2700.0

            // Get the daily total for this specific date
            val dailyTotal = waterIntakeDao.getDailyTotals(date, date).firstOrNull()

            if (dailyTotal != null) {
                val totalIntake = dailyTotal.totalAmount
                val entryCount = dailyTotal.entryCount
                val goalPercentage = (totalIntake / dailyGoal).toFloat()
                val goalAchieved = totalIntake >= dailyGoal

                // Get entries for this date to calculate additional stats
                val entries = waterIntakeDao.getEntriesForDate(date)
                // Since this is a Flow, we need to collect it once. For now, we'll use basic stats
                val averageIntake = if (entryCount > 0) totalIntake / entryCount else 0.0

                val summary = DailySummary(
                    date = date,
                    totalIntake = totalIntake,
                    dailyGoal = dailyGoal,
                    goalAchieved = goalAchieved,
                    goalPercentage = goalPercentage,
                    entryCount = entryCount,
                    firstIntakeTime = null, // Could be calculated from entries if needed
                    lastIntakeTime = null,  // Could be calculated from entries if needed
                    largestIntake = 0.0,    // Could be calculated from entries if needed
                    averageIntake = averageIntake
                )

                dailySummaryDao.insertSummary(summary)
            }
        } catch (e: Exception) {
            // Handle error - in production you might want to log this
            println("Error updating daily summary for $date: ${e.message}")
        }
    }

    // ===== BULK OPERATIONS =====

    suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            waterIntakeDao.deleteAllEntries()
            dailySummaryDao.deleteAllSummaries()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Debug function to inject realistic water intake data for the past 30 days
     * This helps test the History & Statistics screen with meaningful data
     * FIXED: Now also creates DailySummary records for 30-day view
     */
    suspend fun injectDebugData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val random = kotlin.random.Random

            val containerTypes = listOf(
                "Small Glass" to 200.0,
                "Large Glass" to 300.0,
                "Water Bottle" to 500.0,
                "Large Bottle" to 750.0,
                "Coffee Mug" to 250.0,
                "Sports Bottle" to 1000.0
            )

            val entries = mutableListOf<WaterIntakeEntry>()
            val summaries = mutableListOf<DailySummary>()

            // Generate data for the past 30 days
            for (dayOffset in 0..29) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)

                val dateString = dateFormat.format(calendar.time)
                val baseDate = calendar.timeInMillis

                // Simulate realistic daily patterns
                val dailyGoal = userRepository.userProfile.value?.dailyWaterGoal ?: 2700.0
                val achievementRate = random.nextFloat()

                // Create different daily patterns
                val dailyIntakeTarget = when {
                    achievementRate > 0.9f -> dailyGoal * (1.0 + random.nextFloat() * 0.3) // Exceed goal
                    achievementRate > 0.7f -> dailyGoal * (0.8 + random.nextFloat() * 0.3) // Meet or close to goal
                    achievementRate > 0.4f -> dailyGoal * (0.5 + random.nextFloat() * 0.3) // Moderate intake
                    else -> dailyGoal * (0.2 + random.nextFloat() * 0.3) // Low intake days
                }

                // Generate 3-8 entries per day with realistic timing
                val entriesPerDay = random.nextInt(3, 9)
                var totalDailyIntake = 0.0
                var largestIntake = 0.0
                var firstIntakeTime: Long? = null
                var lastIntakeTime: Long? = null

                // Create entries distributed throughout the day
                val wakeUpHour = 6 + random.nextInt(0, 4) // 6-9 AM
                val sleepHour = 21 + random.nextInt(0, 4) // 9 PM - 1 AM
                val activeHours = sleepHour - wakeUpHour

                for (entryIndex in 0 until entriesPerDay) {
                    // Distribute entries throughout active hours
                    val hourOffset = (activeHours.toFloat() / entriesPerDay) * entryIndex + random.nextFloat() * 2
                    val entryHour = (wakeUpHour + hourOffset).toInt().coerceIn(wakeUpHour, sleepHour)
                    val entryMinute = random.nextInt(0, 60)

                    // Set time for this entry
                    calendar.time = Date(baseDate)
                    calendar.set(Calendar.HOUR_OF_DAY, entryHour)
                    calendar.set(Calendar.MINUTE, entryMinute)
                    calendar.set(Calendar.SECOND, random.nextInt(0, 60))

                    val entryTimestamp = calendar.timeInMillis

                    // Track first and last intake times
                    if (firstIntakeTime == null || entryTimestamp < firstIntakeTime) {
                        firstIntakeTime = entryTimestamp
                    }
                    if (lastIntakeTime == null || entryTimestamp > lastIntakeTime) {
                        lastIntakeTime = entryTimestamp
                    }

                    // Choose container type and amount
                    val (containerName, baseAmount) = containerTypes.random()
                    val amount = baseAmount + random.nextFloat() * 50 - 25 // Add some variation

                    // Ensure we don't exceed the daily target too much
                    val remainingTarget = dailyIntakeTarget - totalDailyIntake
                    val finalAmount = if (entryIndex == entriesPerDay - 1) {
                        // Last entry of the day - try to hit target
                        maxOf(100.0, minOf(amount, remainingTarget))
                    } else {
                        minOf(amount, remainingTarget / (entriesPerDay - entryIndex))
                    }.coerceAtLeast(50.0) // Minimum 50ml per entry

                    totalDailyIntake += finalAmount
                    if (finalAmount > largestIntake) {
                        largestIntake = finalAmount
                    }

                    val entry = WaterIntakeEntry(
                        amount = finalAmount,
                        timestamp = entryTimestamp,
                        date = dateString,
                        containerType = containerName,
                        containerVolume = baseAmount,
                        note = if (random.nextFloat() > 0.8f) {
                            listOf("After workout", "With meal", "Morning hydration", "Feeling thirsty", "Reminder").random()
                        } else null,
                        createdAt = entryTimestamp
                    )

                    entries.add(entry)
                }

                // Create corresponding DailySummary
                val goalPercentage = (totalDailyIntake / dailyGoal).toFloat()
                val goalAchieved = totalDailyIntake >= dailyGoal
                val averageIntake = if (entriesPerDay > 0) totalDailyIntake / entriesPerDay else 0.0

                val summary = DailySummary(
                    date = dateString,
                    totalIntake = totalDailyIntake,
                    dailyGoal = dailyGoal,
                    goalAchieved = goalAchieved,
                    goalPercentage = goalPercentage,
                    entryCount = entriesPerDay,
                    firstIntakeTime = firstIntakeTime,
                    lastIntakeTime = lastIntakeTime,
                    largestIntake = largestIntake,
                    averageIntake = averageIntake
                )

                summaries.add(summary)
            }

            // Insert all entries and summaries
            waterIntakeDao.insertEntries(entries)
            dailySummaryDao.insertSummaries(summaries)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ===== DATA CLASSES =====

data class WaterProgress(
    val currentIntake: Double,
    val dailyGoal: Double,
    val progress: Float,
    val isGoalAchieved: Boolean,
    val remainingAmount: Double
) {
    fun getProgressPercentage(): Int = (progress * 100).toInt()

    fun getFormattedCurrent(): String {
        return when {
            currentIntake >= 1000 -> "${String.format("%.1f", currentIntake / 1000)} L"
            else -> "${currentIntake.toInt()} ml"
        }
    }

    fun getFormattedGoal(): String {
        return when {
            dailyGoal >= 1000 -> "${String.format("%.1f", dailyGoal / 1000)} L"
            else -> "${dailyGoal.toInt()} ml"
        }
    }

    fun getFormattedRemaining(): String {
        return when {
            remainingAmount >= 1000 -> "${String.format("%.1f", remainingAmount / 1000)} L"
            else -> "${remainingAmount.toInt()} ml"
        }
    }
}

data class TodayStatistics(
    val totalIntake: Double,
    val goalProgress: Float,
    val entryCount: Int,
    val averageIntake: Double,
    val largestIntake: Double,
    val firstIntakeTime: String?,
    val lastIntakeTime: String?,
    val isGoalAchieved: Boolean,
    val remainingAmount: Double
)

data class WeeklyStatistics(
    val totalIntake: Double,
    val averageDailyIntake: Double,
    val bestDayAmount: Double,
    val bestDayDate: String,
    val totalEntries: Int,
    val daysWithData: Int,
    val dailyTotals: List<DailyTotal>
) {
    fun getFormattedTotal(): String {
        return when {
            totalIntake >= 1000 -> "${String.format("%.1f", totalIntake / 1000)} L"
            else -> "${totalIntake.toInt()} ml"
        }
    }

    fun getFormattedAverage(): String {
        return when {
            averageDailyIntake >= 1000 -> "${String.format("%.1f", averageDailyIntake / 1000)} L"
            else -> "${averageDailyIntake.toInt()} ml"
        }
    }

    fun getFormattedBestDay(): String {
        return when {
            bestDayAmount >= 1000 -> "${String.format("%.1f", bestDayAmount / 1000)} L"
            else -> "${bestDayAmount.toInt()} ml"
        }
    }
}
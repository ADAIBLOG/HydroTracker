package com.cemcakmak.hydrotracker.presentation.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.database.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.database.repository.TodayStatistics
import com.cemcakmak.hydrotracker.utils.WaterCalculator
import com.cemcakmak.hydrotracker.data.models.ActivityLevel
import com.cemcakmak.hydrotracker.data.models.AgeGroup
import com.cemcakmak.hydrotracker.data.models.Gender
import com.cemcakmak.hydrotracker.data.models.ReminderStyle
import com.cemcakmak.hydrotracker.presentation.common.HydroSnackbarHost
import com.cemcakmak.hydrotracker.presentation.common.showSuccessSnackbar

/**
 * Main Profile Screen with Material 3 Expressive Design
 * Modular architecture with separate components
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    userRepository: UserRepository,
    waterIntakeRepository: WaterIntakeRepository,
    onNavigateBack: () -> Unit = {}
) {
    // Collect statistics data
    val todayStatistics by waterIntakeRepository.getTodayStatistics().collectAsState(
        initial = TodayStatistics(0.0, 0f, 0, 0.0, 0.0, null, null, false, 0.0)
    )

    val last30DaysEntries by waterIntakeRepository.getLast30DaysEntries().collectAsState(
        initial = emptyList()
    )

    // State management
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showGoalDialog by remember { mutableStateOf(false) }
    var showActivityDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showPersonalInfoDialog by remember { mutableStateOf(false) }

    // Animation state
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun updateUserProfile(updatedProfile: UserProfile) {
        coroutineScope.launch {
            userRepository.saveUserProfile(updatedProfile)
            snackbarHostState.showSuccessSnackbar(
                message = "Profile updated successfully!"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { HydroSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp), // Add bottom padding for navigation bar
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Profile Header Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(600))
            ) {
                ProfileHeaderCard(
                    userProfile = userProfile,
                    todayStatistics = todayStatistics,
                    totalDaysTracked = last30DaysEntries.groupBy { it.date }.size
                )
            }

            // Personal Information Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                PersonalInformationCard(
                    userProfile = userProfile,
                    onEditGoal = { showGoalDialog = true },
                    onEditActivity = { showActivityDialog = true },
                    onEditSchedule = { showScheduleDialog = true },
                    onEditPersonalInfo = { showPersonalInfoDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showGoalDialog) {
        GoalEditDialog(
            currentGoal = userProfile.dailyWaterGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                updateUserProfile(userProfile.copy(dailyWaterGoal = newGoal))
                showGoalDialog = false
            }
        )
    }

    if (showActivityDialog) {
        ActivityLevelDialog(
            currentLevel = userProfile.activityLevel,
            onDismiss = { showActivityDialog = false },
            onConfirm = { newLevel ->
                // Recalculate goal with new activity level
                val newGoal = WaterCalculator.calculateDailyWaterGoal(
                    gender = userProfile.gender,
                    ageGroup = userProfile.ageGroup,
                    activityLevel = newLevel,
                    weight = userProfile.weight
                )
                updateUserProfile(
                    userProfile.copy(
                        activityLevel = newLevel,
                        dailyWaterGoal = newGoal
                    )
                )
                showActivityDialog = false
            }
        )
    }

    if (showScheduleDialog) {
        ScheduleEditDialog(
            currentWakeUpTime = userProfile.wakeUpTime,
            currentSleepTime = userProfile.sleepTime,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { wakeUp, sleep ->
                // Recalculate reminder interval
                val newInterval = WaterCalculator.calculateReminderInterval(
                    wakeUpTime = wakeUp,
                    sleepTime = sleep,
                    dailyGoal = userProfile.dailyWaterGoal
                )
                updateUserProfile(
                    userProfile.copy(
                        wakeUpTime = wakeUp,
                        sleepTime = sleep,
                        reminderInterval = newInterval
                    )
                )
                showScheduleDialog = false
            }
        )
    }

    if (showPersonalInfoDialog) {
        PersonalInfoDialog(
            userProfile = userProfile,
            onDismiss = { showPersonalInfoDialog = false },
            onConfirm = { gender, ageGroup, weight ->
                // Recalculate goal with new personal info
                val newGoal = WaterCalculator.calculateDailyWaterGoal(
                    gender = gender,
                    ageGroup = ageGroup,
                    activityLevel = userProfile.activityLevel,
                    weight = weight
                )
                updateUserProfile(
                    userProfile.copy(
                        gender = gender,
                        ageGroup = ageGroup,
                        weight = weight,
                        dailyWaterGoal = newGoal
                    )
                )
                showPersonalInfoDialog = false
            }
        )
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val userProfile = UserProfile(
        gender = Gender.MALE,
        ageGroup = AgeGroup.YOUNG_ADULT_18_30,
        activityLevel = ActivityLevel.MODERATE,
        wakeUpTime = "07:00",
        sleepTime = "23:00",
        dailyWaterGoal = 2500.0,
        reminderInterval = 60,
        reminderStyle = ReminderStyle.GENTLE
    )
    val userRepository = UserRepository(androidx.compose.ui.platform.LocalContext.current)
    val waterIntakeRepository = WaterIntakeRepository(
        waterIntakeDao = object : com.cemcakmak.hydrotracker.data.database.dao.WaterIntakeDao {
            override suspend fun insertEntry(entry: com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry): Long = 0
            override suspend fun insertEntries(entries: List<com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry>) {}
            override fun getEntriesForDate(date: String): kotlinx.coroutines.flow.Flow<List<com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override fun getEntriesForDateRange(startDate: String, endDate: String): kotlinx.coroutines.flow.Flow<List<com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override fun getTotalIntakeForDate(date: String): kotlinx.coroutines.flow.Flow<Double> = kotlinx.coroutines.flow.flowOf(0.0)
            override suspend fun getEntryCountForDate(date: String): Int = 0
            override fun getLast30DaysEntries(): kotlinx.coroutines.flow.Flow<List<com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun updateEntry(entry: com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry) {}
            override suspend fun deleteEntry(entry: com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry) {}
            override suspend fun deleteEntryById(entryId: Long) {}
            override suspend fun deleteAllEntries() {}
            override suspend fun getDailyTotals(startDate: String, endDate: String): List<com.cemcakmak.hydrotracker.data.database.dao.DailyTotal> = emptyList()
        },
        dailySummaryDao = object : com.cemcakmak.hydrotracker.data.database.dao.DailySummaryDao {
            override suspend fun insertSummary(summary: com.cemcakmak.hydrotracker.data.database.entities.DailySummary) {}
            override suspend fun insertSummaries(summaries: List<com.cemcakmak.hydrotracker.data.database.entities.DailySummary>) {}
            override fun getSummaryForDate(date: String): kotlinx.coroutines.flow.Flow<com.cemcakmak.hydrotracker.data.database.entities.DailySummary?> = kotlinx.coroutines.flow.flowOf(null)
            override fun getSummariesForRange(startDate: String, endDate: String): kotlinx.coroutines.flow.Flow<List<com.cemcakmak.hydrotracker.data.database.entities.DailySummary>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override fun getLast30DaysSummaries(): kotlinx.coroutines.flow.Flow<List<com.cemcakmak.hydrotracker.data.database.entities.DailySummary>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun updateSummary(summary: com.cemcakmak.hydrotracker.data.database.entities.DailySummary) {}
            override suspend fun deleteSummaryForDate(date: String) {}
            override suspend fun deleteAllSummaries() {}
        },
        userRepository = userRepository,
        context = androidx.compose.ui.platform.LocalContext.current
    )
    ProfileScreen(userProfile = userProfile, userRepository = userRepository, waterIntakeRepository = waterIntakeRepository)
}
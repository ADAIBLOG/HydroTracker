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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.repository.TodayStatistics
import com.cemcakmak.hydrotracker.utils.WaterCalculator
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

    // Calculate additional statistics
    val totalWaterLogged = remember(last30DaysEntries) {
        last30DaysEntries.sumOf { it.amount }
    }

    val averageDailyIntake = remember(last30DaysEntries) {
        if (last30DaysEntries.isNotEmpty()) {
            val days = last30DaysEntries.groupBy { it.date }.size
            if (days > 0) totalWaterLogged / days else 0.0
        } else 0.0
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

            // Your Hydration Plan Section
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
                HydrationPlanCard(
                    userProfile = userProfile,
                    onEditGoal = { showGoalDialog = true },
                    onEditActivity = { showActivityDialog = true },
                    onEditSchedule = { showScheduleDialog = true }
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
                ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
            ) {
                PersonalInformationCard(
                    userProfile = userProfile,
                    onEdit = { showPersonalInfoDialog = true }
                )
            }

            // Statistics & Achievements Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
            ) {
                StatisticsCard(
                    todayStatistics = todayStatistics,
                    totalWaterLogged = totalWaterLogged,
                    averageDailyIntake = averageDailyIntake,
                    totalEntries = last30DaysEntries.size
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
// ===== FILE: HistoryScreen.kt =====
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/history/HistoryScreen.kt

package com.cemcakmak.hydrotracker.presentation.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.data.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.repository.WeeklyStatistics
import com.cemcakmak.hydrotracker.data.repository.TodayStatistics
import com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry
import com.cemcakmak.hydrotracker.data.database.entities.DailySummary
import com.cemcakmak.hydrotracker.ui.theme.HydroTrackerTheme

/**
 * History & Statistics Screen with Material 3 Expressive Design
 * Shows comprehensive water intake analytics and charts
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    waterIntakeRepository: WaterIntakeRepository,
    onNavigateBack: () -> Unit = {}
) {
    // State for different time periods
    var selectedPeriod by remember { mutableStateOf(TimePeriod.WEEK) }

    // Collect data from repository
    val todayStatistics by waterIntakeRepository.getTodayStatistics().collectAsState(
        initial = TodayStatistics(0.0, 0f, 0, 0.0, 0.0, null, null, false, 0.0)
    )

    val last30DaysSummaries by waterIntakeRepository.getLast30DaysSummaries().collectAsState(
        initial = emptyList()
    )

    val last30DaysEntries by waterIntakeRepository.getLast30DaysEntries().collectAsState(
        initial = emptyList()
    )

    // Calculate weekly statistics
    var weeklyStats by remember { mutableStateOf<WeeklyStatistics?>(null) }

    LaunchedEffect(Unit) {
        weeklyStats = waterIntakeRepository.getWeeklyStatistics()
    }

    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History & Statistics",
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
        }
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

        // Period Selector
        AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(500))
            ) {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }

            // Today's Quick Stats
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                TodayStatsSection(todayStatistics)
            }

            // Main Chart Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(700, delayMillis = 300))
            ) {
                when (selectedPeriod) {
                    TimePeriod.WEEK -> WeeklyChartSection(weeklyStats)
                    TimePeriod.MONTH -> MonthlyChartSection(last30DaysSummaries)
                }
            }

            // Statistics Grid
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
            ) {
                StatisticsGrid(
                    weeklyStats = weeklyStats,
                    summaries = last30DaysSummaries,
                    entries = last30DaysEntries
                )
            }

            // Recent Entries Detail
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(600, delayMillis = 500))
            ) {
                RecentEntriesSection(last30DaysEntries.take(10))
            }

            // Goal Achievement Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(600, delayMillis = 600))
            ) {
                GoalAchievementSection(last30DaysSummaries)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

enum class TimePeriod(val displayName: String) {
    WEEK("7 Days"),
    MONTH("30 Days")
}

@Composable
private fun PeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimePeriod.values().forEach { period ->
                val isSelected = selectedPeriod == period

                FilterChip(
                    onClick = { onPeriodSelected(period) },
                    label = { Text(period.displayName) },
                    selected = isSelected,
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun TodayStatsSection(todayStats: TodayStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Progress indicator
            val progress = todayStats.goalProgress
            LinearWavyProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                stroke = WavyProgressIndicatorDefaults.linearIndicatorStroke,
                trackStroke = WavyProgressIndicatorDefaults.linearTrackStroke,
                amplitude = WavyProgressIndicatorDefaults.indicatorAmplitude,
                wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
                waveSpeed = WavyProgressIndicatorDefaults.LinearDeterminateWavelength
            )

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TodayStatItem(
                    icon = "💧",
                    value = "${todayStats.totalIntake.toInt()} ml",
                    label = "Total Intake"
                )
                TodayStatItem(
                    icon = "🎯",
                    value = "${(progress * 100).toInt()}%",
                    label = "Goal Progress"
                )
                TodayStatItem(
                    icon = "📊",
                    value = "${todayStats.entryCount}",
                    label = "Entries"
                )
            }
        }
    }
}

@Composable
private fun TodayStatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WeeklyChartSection(weeklyStats: WeeklyStatistics?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "7-Day Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (weeklyStats != null) {
                // Simple bar chart representation
                WeeklyBarChart(weeklyStats.dailyTotals)

                // Weekly summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeeklyStatItem(
                        label = "Total",
                        value = weeklyStats.getFormattedTotal()
                    )
                    WeeklyStatItem(
                        label = "Average",
                        value = weeklyStats.getFormattedAverage()
                    )
                    WeeklyStatItem(
                        label = "Best Day",
                        value = weeklyStats.getFormattedBestDay()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(dailyTotals: List<com.cemcakmak.hydrotracker.data.database.dao.DailyTotal>) {
    if (dailyTotals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxAmount = dailyTotals.maxOfOrNull { it.totalAmount } ?: 1.0

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bar chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            dailyTotals.forEach { dayTotal ->
                val height = ((dayTotal.totalAmount / maxAmount) * 120).dp

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dayTotal.date.takeLast(2), // Show day number
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthlyChartSection(summaries: List<DailySummary>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "30-Day Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (summaries.isNotEmpty()) {
                // Monthly heatmap-style visualization
                MonthlyHeatmap(summaries)

                // Monthly stats
                val totalDays = summaries.size
                val goalAchievedDays = summaries.count { it.goalAchieved }
                val averageIntake = summaries.map { it.totalIntake }.average()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeeklyStatItem(
                        label = "Days Tracked",
                        value = "$totalDays"
                    )
                    WeeklyStatItem(
                        label = "Goals Met",
                        value = "$goalAchievedDays"
                    )
                    WeeklyStatItem(
                        label = "Success Rate",
                        value = "${((goalAchievedDays.toFloat() / totalDays) * 100).toInt()}%"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available for the last 30 days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyHeatmap(summaries: List<DailySummary>) {
    // Simple grid showing goal achievement
    val rows = 6 // Roughly 5 weeks
    val cols = 7 // Days of week

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(rows) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(cols) { col ->
                    val dayIndex = row * cols + col
                    val summary = summaries.getOrNull(dayIndex)

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(
                                when {
                                    summary == null -> MaterialTheme.colorScheme.surfaceVariant
                                    summary.goalAchieved -> MaterialTheme.colorScheme.primary
                                    summary.goalPercentage >= 0.7f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    summary.goalPercentage >= 0.4f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                }
                            )
                    )
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(0.2f, 0.4f, 0.7f, 1.0f).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                    )
                }
            }
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatisticsGrid(
    weeklyStats: WeeklyStatistics?,
    summaries: List<DailySummary>,
    entries: List<WaterIntakeEntry>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Grid of stat cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Current Streak",
                    value = calculateStreak(summaries).toString(),
                    subtitle = "days",
                    color = MaterialTheme.colorScheme.tertiary
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TrendingUp,
                    title = "Best Streak",
                    value = calculateBestStreak(summaries).toString(),
                    subtitle = "days",
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Stars,
                    title = "Total Entries",
                    value = entries.size.toString(),
                    subtitle = "logged",
                    color = MaterialTheme.colorScheme.error
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WaterDrop,
                    title = "Total Intake",
                    value = "${(entries.sumOf { it.amount } / 1000).toInt()}",
                    subtitle = "liters",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecentEntriesSection(entries: List<WaterIntakeEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recent Entries",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (entries.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entries.take(5).forEach { entry ->
                        HistoryEntryItem(entry = entry)
                    }

                    if (entries.size > 5) {
                        Text(
                            text = "... and ${entries.size - 5} more entries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Text(
                    text = "No entries yet. Start logging your water intake!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HistoryEntryItem(entry: WaterIntakeEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💧",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Column {
                Text(
                    text = entry.containerType,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${entry.date} at ${entry.getFormattedTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = entry.getFormattedAmount(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun GoalAchievementSection(summaries: List<DailySummary>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Goal Achievement",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (summaries.isNotEmpty()) {
                val achievementRate = summaries.count { it.goalAchieved }.toFloat() / summaries.size

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Large achievement percentage
                    Text(
                        text = "${(achievementRate * 100).toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "of days you met your goal",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Achievement progress bar
                    LinearWavyProgressIndicator(
                        progress = { achievementRate },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        stroke = WavyProgressIndicatorDefaults.linearIndicatorStroke,
                        trackStroke = WavyProgressIndicatorDefaults.linearTrackStroke,
                        amplitude = WavyProgressIndicatorDefaults.indicatorAmplitude,
                        wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
                        waveSpeed = WavyProgressIndicatorDefaults.LinearDeterminateWavelength
                    )

                    // Motivational message
                    Text(
                        text = getAchievementMessage(achievementRate),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            } else {
                Text(
                    text = "Start tracking your water intake to see your goal achievement rate!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// Helper functions
private fun calculateStreak(summaries: List<DailySummary>): Int {
    var streak = 0
    val sortedSummaries = summaries.sortedByDescending { it.date }

    for (summary in sortedSummaries) {
        if (summary.goalAchieved) {
            streak++
        } else {
            break
        }
    }
    return streak
}

private fun calculateBestStreak(summaries: List<DailySummary>): Int {
    var maxStreak = 0
    var currentStreak = 0
    val sortedSummaries = summaries.sortedBy { it.date }

    for (summary in sortedSummaries) {
        if (summary.goalAchieved) {
            currentStreak++
            maxStreak = maxOf(maxStreak, currentStreak)
        } else {
            currentStreak = 0
        }
    }
    return maxStreak
}

private fun getAchievementMessage(rate: Float): String {
    return when {
        rate >= 0.9f -> "🏆 Outstanding! You're a hydration champion!"
        rate >= 0.7f -> "🌟 Excellent work! Keep up the great consistency!"
        rate >= 0.5f -> "💪 Good progress! You're building a solid habit!"
        rate >= 0.3f -> "👍 Making progress! Stay focused on your goals!"
        else -> "🚀 Every day is a new opportunity to hydrate better!"
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    HydroTrackerTheme {
        // Note: Preview won't work with repository parameter
        // You would need to create a fake repository for preview
    }
}
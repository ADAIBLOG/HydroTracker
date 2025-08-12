// ===== FILE: HistoryScreen.kt =====
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/history/HistoryScreen.kt

package com.cemcakmak.hydrotracker.presentation.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
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
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.data.database.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.database.repository.WeeklyStatistics
import com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry
import com.cemcakmak.hydrotracker.data.database.entities.DailySummary
import com.cemcakmak.hydrotracker.utils.WaterCalculator
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    waterIntakeRepository: WaterIntakeRepository,
    onNavigateBack: () -> Unit = {}
) {
    // State for different time periods
    var selectedPeriod by remember { mutableStateOf(TimePeriod.LAST_7_DAYS) }

    // Collect data from repository
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Period Selector
            item {
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
            }

            // Main Chart Section
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(animationSpec = tween(700, delayMillis = 200))
                ) {
                    when (selectedPeriod) {
                        TimePeriod.YESTERDAY, TimePeriod.LAST_3_DAYS, TimePeriod.THIS_WEEK, TimePeriod.LAST_7_DAYS -> {
                            WeeklyChartSection(
                                weeklyStats = weeklyStats,
                                selectedPeriod = selectedPeriod,
                            )
                        }
                        TimePeriod.THIS_MONTH, TimePeriod.LAST_30_DAYS -> {
                            MonthlyChartSection(
                                summaries = last30DaysSummaries.take(getDataLimitForPeriod(selectedPeriod)),
                                selectedPeriod = selectedPeriod
                            )
                        }
                    }
                }
            }

            // Statistics Overview
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
                ) {
                    StatisticsGrid(
                        summaries = last30DaysSummaries.take(getDataLimitForPeriod(selectedPeriod)),
                        entries = last30DaysEntries.take(getDataLimitForPeriod(selectedPeriod) * 5), // Approximate entries per day
                        selectedPeriod = selectedPeriod
                    )
                }
            }

            // Goal Achievement
            item {
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
                    GoalAchievementSection(last30DaysSummaries.take(getDataLimitForPeriod(selectedPeriod)))
                }
            }

            // Bottom spacer for navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

enum class TimePeriod(val displayName: String, val description: String) {
    YESTERDAY("Yesterday", "Previous day"),
    LAST_3_DAYS("3 Days", "Last 3 days"),
    THIS_WEEK("This Week", "Current week"),
    LAST_7_DAYS("7 Days", "Last 7 days"),
    THIS_MONTH("This Month", "Current month"),
    LAST_30_DAYS("30 Days", "Last 30 days")
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Time Period",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            // First row: Quick periods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(TimePeriod.YESTERDAY, TimePeriod.LAST_3_DAYS, TimePeriod.THIS_WEEK).forEach { period ->
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
            
            // Second row: Longer periods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(TimePeriod.LAST_7_DAYS, TimePeriod.THIS_MONTH, TimePeriod.LAST_30_DAYS).forEach { period ->
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
            
            // Description text
            Text(
                text = selectedPeriod.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun WeeklyChartSection(
    weeklyStats: WeeklyStatistics?,
    selectedPeriod: TimePeriod,
) {
    var selectedDayData by remember { mutableStateOf<com.cemcakmak.hydrotracker.data.database.dao.DailyTotal?>(null) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = getPeriodTitle(selectedPeriod),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Use filtered data based on selected period
            val filteredDailyTotals = when {
                weeklyStats != null -> weeklyStats.dailyTotals.take(getDataLimitForPeriod(selectedPeriod))
                else -> emptyList()
            }
            
            if (filteredDailyTotals.isNotEmpty()) {
                // Simple bar chart representation
                WeeklyBarChart(
                    dailyTotals = filteredDailyTotals,
                    onBarClick = { dayTotal -> selectedDayData = dayTotal }
                )
                
                // Inline detail panel with animation
                AnimatedVisibility(
                    visible = selectedDayData != null,
                    enter = slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(200))
                ) {
                    selectedDayData?.let { dayData ->
                        InlineDetailPanel(
                            data = ChartDetailData(
                                date = dayData.date,
                                amount = dayData.totalAmount,
                                goal = null,
                                goalPercentage = null
                            ),
                            onDismiss = { selectedDayData = null }
                        )
                    }
                }

                // Period-specific summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val totalAmount = filteredDailyTotals.sumOf { it.totalAmount }
                    val avgAmount = totalAmount / filteredDailyTotals.size
                    val bestAmount = filteredDailyTotals.maxOfOrNull { it.totalAmount } ?: 0.0
                    
                    WeeklyStatItem(
                        label = "Total",
                        value = WaterCalculator.formatWaterAmount(totalAmount)
                    )
                    WeeklyStatItem(
                        label = "Average",
                        value = WaterCalculator.formatWaterAmount(avgAmount)
                    )
                    WeeklyStatItem(
                        label = "Best Day",
                        value = WaterCalculator.formatWaterAmount(bestAmount)
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
private fun WeeklyBarChart(
    dailyTotals: List<com.cemcakmak.hydrotracker.data.database.dao.DailyTotal>,
    onBarClick: (com.cemcakmak.hydrotracker.data.database.dao.DailyTotal) -> Unit
) {
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
                            .clickable { onBarClick(dayTotal) }
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (height > 30.dp) {
                            Text(
                                text = WaterCalculator.formatWaterAmount(dayTotal.totalAmount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dailyTotals.forEach { dayTotal ->
                Text(
                    text = dayTotal.date.takeLast(2), // Show day number
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
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
private fun MonthlyChartSection(
    summaries: List<DailySummary>,
    selectedPeriod: TimePeriod
) {
    var selectedSummary by remember { mutableStateOf<DailySummary?>(null) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = getPeriodTitle(selectedPeriod),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (summaries.isNotEmpty()) {
                // Monthly heatmap-style visualization
                MonthlyHeatmap(
                    summaries = summaries,
                    onCellClick = { summary -> selectedSummary = summary }
                )
                
                // Inline detail panel with animation
                AnimatedVisibility(
                    visible = selectedSummary != null,
                    enter = slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(200))
                ) {
                    selectedSummary?.let { summary ->
                        InlineDetailPanel(
                            data = ChartDetailData(
                                date = summary.date,
                                amount = summary.totalIntake,
                                goal = summary.dailyGoal,
                                goalPercentage = summary.goalPercentage
                            ),
                            onDismiss = { selectedSummary = null }
                        )
                    }
                }

                // Monthly stats
                val totalDays = summaries.size
                val goalAchievedDays = summaries.count { it.goalAchieved }

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
private fun MonthlyHeatmap(
    summaries: List<DailySummary>,
    onCellClick: (DailySummary) -> Unit
) {
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
                            .size(18.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable(enabled = summary != null) { 
                                summary?.let { onCellClick(it) }
                            }
                            .background(
                                when {
                                    summary == null -> MaterialTheme.colorScheme.surfaceVariant
                                    summary.goalAchieved -> MaterialTheme.colorScheme.primary
                                    summary.goalPercentage >= 0.8f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    summary.goalPercentage >= 0.6f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    summary.goalPercentage >= 0.4f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    summary.goalPercentage >= 0.2f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(0.1f, 0.25f, 0.4f, 0.6f, 0.8f, 1.0f).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(MaterialTheme.shapes.small)
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
    summaries: List<DailySummary>,
    entries: List<WaterIntakeEntry>,
    selectedPeriod: TimePeriod
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Calculate trend data
        val currentStreak = calculateStreak(summaries)
        val bestStreak = calculateBestStreak(summaries)
        val totalEntries = entries.size
        val totalIntake = entries.sumOf { it.amount }
        
        val (streakTrend, intakeTrend) = calculateTrends(summaries, selectedPeriod)
        
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
                    value = currentStreak.toString(),
                    subtitle = "days",
                    color = MaterialTheme.colorScheme.tertiary,
                    trend = streakTrend
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "Best Streak",
                    value = bestStreak.toString(),
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
                    value = totalEntries.toString(),
                    subtitle = "logged",
                    color = MaterialTheme.colorScheme.error
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WaterDrop,
                    title = "Total Intake",
                    value = "${(totalIntake / 1000).toInt()}",
                    subtitle = "liters",
                    color = MaterialTheme.colorScheme.primary,
                    trend = intakeTrend
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
    color: Color,
    trend: TrendInfo? = null
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                
                trend?.let { trendInfo ->
                    Icon(
                        imageVector = when (trendInfo.direction) {
                            TrendDirection.UP -> Icons.AutoMirrored.Filled.TrendingUp
                            TrendDirection.DOWN -> Icons.AutoMirrored.Filled.TrendingDown
                            TrendDirection.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                        },
                        contentDescription = null,
                        tint = when (trendInfo.direction) {
                            TrendDirection.UP -> MaterialTheme.colorScheme.primary
                            TrendDirection.DOWN -> MaterialTheme.colorScheme.error
                            TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                trend?.let { trendInfo ->
                    Text(
                        text = trendInfo.percentageText,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (trendInfo.direction) {
                            TrendDirection.UP -> MaterialTheme.colorScheme.primary
                            TrendDirection.DOWN -> MaterialTheme.colorScheme.error
                            TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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

data class ChartDetailData(
    val date: String,
    val amount: Double,
    val goal: Double?,
    val goalPercentage: Float?
)

enum class TrendDirection {
    UP, DOWN, STABLE
}

data class TrendInfo(
    val direction: TrendDirection,
    val percentage: Double,
    val percentageText: String
)

@Composable
private fun InlineDetailPanel(
    data: ChartDetailData,
    onDismiss: () -> Unit
) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDisplayDate(data.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // Content in a more compact layout
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Water amount - prominent display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Water Intake",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = WaterCalculator.formatWaterAmount(data.amount),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Goal information (if available)
                    data.goal?.let { goal ->
                        data.goalPercentage?.let { percentage ->
                            // Compact progress display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Goal: ${WaterCalculator.formatWaterAmount(goal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Progress: ${(percentage * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                // Achievement status icon
                                val (status) = when {
                                    percentage >= 1.0f -> "🎉" to MaterialTheme.colorScheme.primary
                                    percentage >= 0.8f -> "🌟" to MaterialTheme.colorScheme.tertiary
                                    percentage >= 0.5f -> "💪" to MaterialTheme.colorScheme.secondary
                                    else -> "📈" to MaterialTheme.colorScheme.outline
                                }

                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            
                            // Compact progress bar
                            LinearProgressIndicator(
                                progress = { percentage.coerceAtMost(1.0f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(MaterialTheme.shapes.small),
                                color = if (percentage >= 1.0f) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

private fun formatDisplayDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (_: Exception) {
        dateString
    }
}

private fun calculateTrends(summaries: List<DailySummary>, period: TimePeriod): Pair<TrendInfo?, TrendInfo?> {
    if (summaries.isEmpty()) return Pair(null, null)
    
    val sortedSummaries = summaries.sortedBy { it.date }
    
    // Split data into current and previous periods for comparison
    val splitPoint = when (period) {
        TimePeriod.YESTERDAY -> 1
        TimePeriod.LAST_3_DAYS -> 3
        TimePeriod.THIS_WEEK, TimePeriod.LAST_7_DAYS -> 7
        TimePeriod.THIS_MONTH, TimePeriod.LAST_30_DAYS -> 15 // Compare last 15 days with previous 15
    }
    
    if (sortedSummaries.size < splitPoint * 2) {
        return Pair(null, null) // Not enough data for trend analysis
    }
    
    val currentPeriod = sortedSummaries.takeLast(splitPoint)
    val previousPeriod = sortedSummaries.dropLast(splitPoint).takeLast(splitPoint)
    
    // Calculate streak trend
    val currentStreakRate = currentPeriod.count { it.goalAchieved }.toDouble() / currentPeriod.size
    val previousStreakRate = previousPeriod.count { it.goalAchieved }.toDouble() / previousPeriod.size
    val streakTrend = calculateTrendInfo(currentStreakRate, previousStreakRate)
    
    // Calculate intake trend
    val currentAvgIntake = currentPeriod.map { it.totalIntake }.average()
    val previousAvgIntake = previousPeriod.map { it.totalIntake }.average()
    val intakeTrend = calculateTrendInfo(currentAvgIntake, previousAvgIntake)
    
    return Pair(streakTrend, intakeTrend)
}

private fun calculateTrendInfo(current: Double, previous: Double): TrendInfo? {
    if (previous == 0.0) return null
    
    val changePercent = ((current - previous) / previous) * 100
    val absChange = kotlin.math.abs(changePercent)
    
    return when {
        absChange < 5.0 -> TrendInfo(
            direction = TrendDirection.STABLE,
            percentage = changePercent,
            percentageText = "±${absChange.toInt()}%"
        )
        changePercent > 0 -> TrendInfo(
            direction = TrendDirection.UP,
            percentage = changePercent,
            percentageText = "+${absChange.toInt()}%"
        )
        else -> TrendInfo(
            direction = TrendDirection.DOWN,
            percentage = changePercent,
            percentageText = "-${absChange.toInt()}%"
        )
    }
}

private fun getPeriodTitle(period: TimePeriod): String {
    return when (period) {
        TimePeriod.YESTERDAY -> "Yesterday's Activity"
        TimePeriod.LAST_3_DAYS -> "Last 3 Days"
        TimePeriod.THIS_WEEK -> "This Week"
        TimePeriod.LAST_7_DAYS -> "7-Day Overview"
        TimePeriod.THIS_MONTH -> "This Month"
        TimePeriod.LAST_30_DAYS -> "30-Day Overview"
    }
}

private fun getDataLimitForPeriod(period: TimePeriod): Int {
    return when (period) {
        TimePeriod.YESTERDAY -> 1
        TimePeriod.LAST_3_DAYS -> 3
        TimePeriod.THIS_WEEK -> 7
        TimePeriod.LAST_7_DAYS -> 7
        TimePeriod.THIS_MONTH -> 30
        TimePeriod.LAST_30_DAYS -> 30
    }
}

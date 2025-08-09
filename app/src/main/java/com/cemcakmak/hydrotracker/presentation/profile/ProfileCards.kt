package com.cemcakmak.hydrotracker.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.database.repository.TodayStatistics
import com.cemcakmak.hydrotracker.utils.WaterCalculator

/**
 * Profile Header Card with user avatar and quick stats
 */
@Composable
fun ProfileHeaderCard(
    userProfile: UserProfile,
    todayStatistics: TodayStatistics,
    totalDaysTracked: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Avatar
            Surface(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Greeting
            Text(
                text = userProfile.gender.getPersonalizedGreeting(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    value = "${todayStatistics.entryCount}",
                    label = "Today's Entries"
                )
                QuickStatItem(
                    value = "$totalDaysTracked",
                    label = "Days Tracked"
                )
                QuickStatItem(
                    value = "${(todayStatistics.goalProgress * 100).toInt()}%",
                    label = "Today's Goal"
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
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

/**
 * Hydration Plan Card with editable settings
 */
@Composable
fun HydrationPlanCard(
    userProfile: UserProfile,
    onEditGoal: () -> Unit,
    onEditActivity: () -> Unit,
    onEditSchedule: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Hydration Plan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Daily Goal
            EditableInfoRow(
                icon = Icons.Default.Flag,
                label = "Daily Goal",
                value = WaterCalculator.formatWaterAmount(userProfile.dailyWaterGoal),
                onClick = onEditGoal
            )

            // Activity Level
            EditableInfoRow(
                icon = Icons.Default.FitnessCenter,
                label = "Activity Level",
                value = userProfile.activityLevel.getDisplayName(),
                onClick = onEditActivity
            )

            // Schedule
            EditableInfoRow(
                icon = Icons.Default.Schedule,
                label = "Active Hours",
                value = "${userProfile.wakeUpTime} - ${userProfile.sleepTime}",
                onClick = onEditSchedule
            )

            // Reminder Frequency
            InfoRow(
                icon = Icons.Default.Notifications,
                label = "Reminders",
                value = "Every ${userProfile.reminderInterval} minutes"
            )
        }
    }
}

/**
 * Personal Information Card
 */
@Composable
fun PersonalInformationCard(
    userProfile: UserProfile,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Personal Info"
                    )
                }
            }

            InfoRow(
                icon = Icons.Default.Person,
                label = "Gender",
                value = userProfile.gender.getDisplayName()
            )

            InfoRow(
                icon = Icons.Default.Cake,
                label = "Age Group",
                value = userProfile.ageGroup.getDisplayName()
            )

            if (userProfile.weight != null) {
                InfoRow(
                    icon = Icons.Default.MonitorWeight,
                    label = "Weight",
                    value = "${userProfile.weight!!.toInt()} kg"
                )
            }
        }
    }
}

/**
 * Statistics and Achievements Card
 */
@Composable
fun StatisticsCard(
    todayStatistics: TodayStatistics,
    totalWaterLogged: Double,
    averageDailyIntake: Double,
    totalEntries: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistics & Achievements",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Statistics Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.WaterDrop,
                        title = "Total Water",
                        value = WaterCalculator.formatWaterAmount(totalWaterLogged),
                        subtitle = "Last 30 days"
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        title = "Daily Average",
                        value = WaterCalculator.formatWaterAmount(averageDailyIntake),
                        subtitle = "Last 30 days"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FormatListNumbered,
                        title = "Total Entries",
                        value = "$totalEntries",
                        subtitle = "All time"
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Today,
                        title = "Today's Best",
                        value = WaterCalculator.formatWaterAmount(todayStatistics.largestIntake),
                        subtitle = "Single entry"
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
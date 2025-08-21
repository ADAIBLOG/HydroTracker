package com.cemcakmak.hydrotracker.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.utils.ImageUtils
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    totalDaysTracked: Int,
    onEditProfilePicture: () -> Unit = {},
    onEditUsername: () -> Unit = {}
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
            ProfileAvatar(
                profileImagePath = userProfile.profileImagePath,
                name = userProfile.name,
                size = 80.dp,
                onClick = onEditProfilePicture
            )

            // Personalized Greeting
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = getTimeBasedGreeting(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userProfile.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(
                        onClick = onEditUsername,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Name",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

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
 * Profile Avatar Component with image support and fallback initials
 */
@Composable
fun ProfileAvatar(
    profileImagePath: String?,
    name: String,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var profileBitmap by remember(profileImagePath) { mutableStateOf<ImageBitmap?>(null) }
    
    // Load the image when profileImagePath changes
    LaunchedEffect(profileImagePath) {
        profileBitmap = if (profileImagePath != null && File(profileImagePath).exists()) {
            ImageUtils.loadProfileImageBitmap(context)?.asImageBitmap()
        } else {
            null
        }
    }
    
    Surface(
        modifier = modifier
            .size(size)
            .let { mod -> 
                onClick?.let { mod.clickable { it() } } ?: mod 
            },
        shape = CircleShape,
        color = if (profileBitmap != null) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        },
        border = if (profileBitmap != null) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        } else null
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            profileBitmap?.let { bitmap ->
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                // Show initials as fallback
                Text(
                    text = getInitials(name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Get user's initials from their name
 */
private fun getInitials(name: String): String {
    return name.trim()
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "U" } // Fallback to "U" for User
}

/**
 * Get time-based greeting message
 */
private fun getTimeBasedGreeting(): String {
    val currentHour = LocalTime.now().hour
    return when (currentHour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..21 -> "Good evening,"
        else -> "Hello,"
    }
}


/**
 * Personal Information Card with all user customizable settings from onboarding
 */
@Composable
fun PersonalInformationCard(
    userProfile: UserProfile,
    onEditGoal: () -> Unit,
    onEditActivity: () -> Unit,
    onEditSchedule: () -> Unit,
    onEditPersonalInfo: () -> Unit
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
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Gender
            EditableInfoRow(
                icon = Icons.Default.Person,
                label = "Gender",
                value = userProfile.gender.getDisplayName(),
                onClick = onEditPersonalInfo
            )

            // Age Group
            EditableInfoRow(
                icon = Icons.Default.Cake,
                label = "Age Group", 
                value = userProfile.ageGroup.getDisplayName(),
                onClick = onEditPersonalInfo
            )

            // Weight (if available)
            if (userProfile.weight != null) {
                EditableInfoRow(
                    icon = Icons.Default.MonitorWeight,
                    label = "Weight",
                    value = "${userProfile.weight!!.toInt()} kg",
                    onClick = onEditPersonalInfo
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

            // Reminder Frequency (Read-only)
            InfoRow(
                icon = Icons.Default.Notifications,
                label = "Reminders",
                value = "Every ${userProfile.reminderInterval} minutes"
            )
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
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
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
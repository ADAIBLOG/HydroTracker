package com.cemcakmak.hydrotracker.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.utils.ImageUtils
import java.io.File
import java.time.LocalTime
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
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                size = 120.dp,
                onClick = onEditProfilePicture
            )

            // Personalized Greeting
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = getTimeBasedGreeting(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline()
                )

                val haptics = LocalHapticFeedback.current
                IconButton(
                    onClick = {haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        onEditUsername()},
                    modifier = Modifier
                        .size(20.dp)
                        .alignByBaseline()
                        .offset(y = (10).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
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

            Spacer(
                modifier = Modifier.height(12.dp)
            )
            HorizontalDivider()
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
            style = MaterialTheme.typography.headlineMediumEmphasized,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
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
 * Profile Details Card - Personal information
 */
@Composable
fun ProfileDetailsCard(
    userProfile: UserProfile,
    onEditGender: () -> Unit,
    onEditAgeGroup: () -> Unit,
    onEditWeight: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile Details",
                    style = MaterialTheme.typography.titleLargeEmphasized
                )
            }

            val haptics = LocalHapticFeedback.current
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Gender
                EditableInfoRow(
                    icon = Icons.Default.Person,
                    label = "Gender",
                    value = userProfile.gender.getDisplayName(),
                    onClick = {haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onEditGender() }
                )

                // Age Group
                EditableInfoRow(
                    icon = Icons.Default.Cake,
                    label = "Age Group", 
                    value = userProfile.ageGroup.getDisplayName(),
                    onClick = {haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onEditGender() }
                )

                // Weight
                EditableInfoRow(
                    icon = Icons.Default.MonitorWeight,
                    label = "Weight",
                    value = if (userProfile.weight != null) "${userProfile.weight.toInt()} kg" else "Not set",
                    onClick = {haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onEditWeight() }
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                HorizontalDivider()
            }
        }
    }
}

/**
 * Daily Goals Card - Water goals and activity level
 */
@Composable
fun DailyGoalsCard(
    userProfile: UserProfile,
    onEditGoal: () -> Unit,
    onEditActivity: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val haptics = LocalHapticFeedback.current
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Daily Goal
                EditableInfoRow(
                    icon = Icons.Default.WaterDrop,
                    label = "Daily Water Goal",
                    value = WaterCalculator.formatWaterAmount(userProfile.dailyWaterGoal),
                    onClick = {haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onEditGoal() }
                )

                // Activity Level
                EditableInfoRow(
                    icon = Icons.Default.FitnessCenter,
                    label = "Activity Level",
                    value = userProfile.activityLevel.getDisplayName(),
                    onClick = {haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onEditActivity() }
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
                HorizontalDivider()
            }
        }
    }
}

/**
 * Active Schedule Card - Wake/sleep times and reminders
 */
@Composable
fun ActiveScheduleCard(
    userProfile: UserProfile,
    onEditSchedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            val haptics = LocalHapticFeedback.current
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Schedule
                EditableInfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Active Hours",
                    value = "${userProfile.wakeUpTime} - ${userProfile.sleepTime}",
                    onClick = {haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onEditSchedule() }
                )

                // Reminder Frequency (Read-only)
                InfoRow(
                    icon = Icons.Default.Notifications,
                    label = "Reminder Interval",
                    value = "Every ${userProfile.reminderInterval} minutes"
                )
            }
        }
    }
}


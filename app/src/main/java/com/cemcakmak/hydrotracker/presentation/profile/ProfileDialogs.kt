package com.cemcakmak.hydrotracker.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cemcakmak.hydrotracker.data.models.ActivityLevel
import com.cemcakmak.hydrotracker.data.models.Gender
import com.cemcakmak.hydrotracker.data.models.AgeGroup
import com.cemcakmak.hydrotracker.data.models.UserProfile

/**
 * Profile Edit Dialogs
 * Material 3 dialogs for editing profile information
 */

/**
 * Dialog for editing daily water goal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditDialog(
    currentGoal: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var goalText by remember { mutableStateOf(String.format("%.2f", currentGoal / 1000)) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Daily Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Set your daily water intake goal. This will be used to track your progress and calculate reminders.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = goalText,
                    onValueChange = {
                        goalText = it
                        isError = false
                    },
                    label = { Text("Daily Goal (Liters)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Please enter a valid amount (0.5-10 L)") }
                    } else {
                        { Text("Recommended: 1.5 - 4.0 liters per day") }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val goalLiters = goalText.toDoubleOrNull()
                            if (goalLiters != null && goalLiters >= 0.5 && goalLiters <= 10.0) {
                                onConfirm(goalLiters * 1000) // Convert to ml
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for selecting activity level
 */
@Composable
fun ActivityLevelDialog(
    currentLevel: ActivityLevel,
    onDismiss: () -> Unit,
    onConfirm: (ActivityLevel) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Activity Level",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your activity level affects your daily water needs. Changing this will automatically update your daily goal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActivityLevel.values().forEach { level ->
                        Card(
                            onClick = { onConfirm(level) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (level == currentLevel) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = level.getDisplayName(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = level.getDescription(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for editing wake/sleep schedule
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditDialog(
    currentWakeUpTime: String,
    currentSleepTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var wakeUpTime by remember { mutableStateOf(currentWakeUpTime) }
    var sleepTime by remember { mutableStateOf(currentSleepTime) }
    var isWakeUpError by remember { mutableStateOf(false) }
    var isSleepError by remember { mutableStateOf(false) }

    fun validateTime(time: String): Boolean {
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return false
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            hour in 0..23 && minute in 0..59
        } catch (e: Exception) {
            false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Set your daily schedule to get reminders only during your active hours. This will automatically adjust your reminder frequency.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = wakeUpTime,
                    onValueChange = {
                        wakeUpTime = it
                        isWakeUpError = false
                    },
                    label = { Text("Wake Up Time (HH:MM)") },
                    isError = isWakeUpError,
                    supportingText = if (isWakeUpError) {
                        { Text("Please enter a valid time (e.g., 07:30)") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sleepTime,
                    onValueChange = {
                        sleepTime = it
                        isSleepError = false
                    },
                    label = { Text("Sleep Time (HH:MM)") },
                    isError = isSleepError,
                    supportingText = if (isSleepError) {
                        { Text("Please enter a valid time (e.g., 23:00)") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val wakeUpValid = validateTime(wakeUpTime)
                            val sleepValid = validateTime(sleepTime)

                            isWakeUpError = !wakeUpValid
                            isSleepError = !sleepValid

                            if (wakeUpValid && sleepValid) {
                                onConfirm(wakeUpTime, sleepTime)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for editing personal information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (Gender, AgeGroup, Double?) -> Unit
) {
    var selectedGender by remember { mutableStateOf(userProfile.gender) }
    var selectedAgeGroup by remember { mutableStateOf(userProfile.ageGroup) }
    var weightText by remember { mutableStateOf(userProfile.weight?.toString() ?: "") }
    var isWeightError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your personal information helps us calculate a more accurate daily water goal based on scientific recommendations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Gender Selection
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Gender.values().forEach { gender ->
                        Card(
                            onClick = { selectedGender = gender },
                            colors = CardDefaults.cardColors(
                                containerColor = if (gender == selectedGender) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Text(
                                text = gender.getDisplayName(),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Age Group Selection
                Text(
                    text = "Age Group",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgeGroup.values().forEach { ageGroup ->
                        Card(
                            onClick = { selectedAgeGroup = ageGroup },
                            colors = CardDefaults.cardColors(
                                containerColor = if (ageGroup == selectedAgeGroup) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Text(
                                text = ageGroup.getDisplayName(),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Weight Input (Optional)
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        isWeightError = false
                    },
                    label = { Text("Weight (kg) - Optional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isWeightError,
                    supportingText = if (isWeightError) {
                        { Text("Please enter a valid weight (30-300 kg)") }
                    } else {
                        { Text("Adding weight provides more accurate goal calculations") }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val weight = if (weightText.isBlank()) {
                                null
                            } else {
                                weightText.toDoubleOrNull()
                            }

                            if (weight != null && (weight < 30 || weight > 300)) {
                                isWeightError = true
                            } else {
                                onConfirm(selectedGender, selectedAgeGroup, weight)
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
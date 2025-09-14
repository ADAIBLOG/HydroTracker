package com.cemcakmak.hydrotracker.presentation.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cemcakmak.hydrotracker.data.models.ThemePreferences
import com.cemcakmak.hydrotracker.data.models.DarkModePreference
import com.cemcakmak.hydrotracker.data.models.ColorSource
import com.cemcakmak.hydrotracker.data.models.WeekStartDay
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.database.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.presentation.common.HydroSnackbarHost
import com.cemcakmak.hydrotracker.ui.theme.HydroTrackerTheme
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    themePreferences: ThemePreferences = ThemePreferences(),
    userProfile: UserProfile? = null,
    userRepository: UserRepository? = null,
    waterIntakeRepository: WaterIntakeRepository? = null,
    onThemeToggle: (Boolean) -> Unit = {},
    onDarkModeChange: (DarkModePreference) -> Unit = {},
    onColorSourceChange: (ColorSource) -> Unit = {},
    onWeekStartDayChange: (WeekStartDay) -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    isDynamicColorAvailable: Boolean = true
) {
    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    
    // Developer options state
    var developerOptionsEnabled by remember { 
        mutableStateOf(
            userRepository?.loadDeveloperOptionsEnabled() ?: false
        )
    }
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    // Snackbar state for Material 3 Expressive feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Customization Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffsetY = { -it / 3 }
                ) + fadeIn(animationSpec = tween(600))
            ) {
                ThemeSection(
                    themePreferences = themePreferences,
                    onColorSourceChange = onColorSourceChange,
                    isDynamicColorAvailable = isDynamicColorAvailable,
                    isVisible = isVisible
                )
            }

            // Dark Mode Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffsetY = { it / 2 }
                ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                DisplaySection(
                    themePreferences = themePreferences,
                    onDarkModeChange = onDarkModeChange,
                    onWeekStartDayChange = onWeekStartDayChange
                )
            }

            // Notification Settings Section
            NotificationSettingsSection(
                userProfile = userProfile,
                onRequestPermission = onRequestNotificationPermission,
                isVisible = isVisible
            )

            // Developer Options Section (only show if enabled and repositories available)
            if (developerOptionsEnabled && userRepository != null && waterIntakeRepository != null) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it / 2 }
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
                ) {
                    DeveloperOptionsSection(
                        userRepository = userRepository,
                        waterIntakeRepository = waterIntakeRepository,
                        snackbarHostState = snackbarHostState,
                        onNavigateToOnboarding = onNavigateToOnboarding,
                        onDisableDeveloperOptions = {
                            developerOptionsEnabled = false
                            userRepository.saveDeveloperOptionsEnabled(false)
                        },
                        userProfile = userProfile,
                        isVisible = isVisible
                    )
                }
            }
            
            // Footer with app info
            FooterSection(
                onVersionTap = {
                    val currentTime = System.currentTimeMillis()
                    
                    // Reset counter if more than 3 seconds have passed
                    if (currentTime - lastTapTime > 3000) {
                        tapCount = 1
                    } else {
                        tapCount++
                    }
                    
                    lastTapTime = currentTime
                    
                    // Activate developer options after 10 taps
                    if (tapCount >= 10 && !developerOptionsEnabled) {
                        developerOptionsEnabled = true
                        userRepository?.saveDeveloperOptionsEnabled(true)
                        
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Developer options activated",
                                duration = SnackbarDuration.Short
                            )
                        }
                        
                        tapCount = 0
                    }
                },
                isVisible = isVisible
            )
        }
    }
}

@Composable
private fun ThemeSection(
    themePreferences: ThemePreferences,
    onColorSourceChange: (ColorSource) -> Unit,
    isDynamicColorAvailable: Boolean,
    isVisible: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Color Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // HydroTracker Theme Option
            ColorSourceOption(
                title = "HydroTracker Blue",
                description = "Beautiful water-themed blue palette",
                isSelected = themePreferences.colorSource == ColorSource.HYDRO_THEME,
                onClick = { onColorSourceChange(ColorSource.HYDRO_THEME) },
                isVisible = isVisible
            )

            // Dynamic Color Option
            ColorSourceOption(
                title = "Dynamic Colors",
                description = if (isDynamicColorAvailable) {
                    "Colors from your wallpaper"
                } else {
                    "Requires Android 12+"
                },
                isSelected = themePreferences.colorSource == ColorSource.DYNAMIC_COLOR,
                onClick = {
                    if (isDynamicColorAvailable) {
                        onColorSourceChange(ColorSource.DYNAMIC_COLOR)
                    }
                },
                enabled = isDynamicColorAvailable,
                isVisible = isVisible
            )
        }
    }
}

@Composable
private fun ColorSourceOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isVisible: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "option_scale"
    )

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun DisplaySection(
    themePreferences: ThemePreferences,
    onDarkModeChange: (DarkModePreference) -> Unit,
    onWeekStartDayChange: (WeekStartDay) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Display",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Dark Mode Options
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
            
            DarkModePreference.values().forEach { preference ->
                DarkModeOption(
                    preference = preference,
                    isSelected = themePreferences.darkMode == preference,
                    onClick = { onDarkModeChange(preference) }
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            // Week Start Day Options
            Text(
                text = "Week starts on",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
            
            WeekStartDay.values().forEach { weekStartDay ->
                WeekStartDayOption(
                    weekStartDay = weekStartDay,
                    isSelected = themePreferences.weekStartDay == weekStartDay,
                    onClick = { onWeekStartDayChange(weekStartDay) }
                )
            }
        }
    }
}

@Composable
private fun DarkModeOption(
    preference: DarkModePreference,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (preference) {
        DarkModePreference.SYSTEM -> Icons.Default.Settings
        DarkModePreference.LIGHT -> Icons.Default.LightMode
        DarkModePreference.DARK -> Icons.Default.DarkMode
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        text = preference.getDisplayName(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = preference.getDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun WeekStartDayOption(
    weekStartDay: WeekStartDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (weekStartDay) {
        WeekStartDay.SUNDAY -> Icons.Default.Weekend
        WeekStartDay.MONDAY -> Icons.Default.Today
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        text = weekStartDay.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = weekStartDay.getDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}


@Composable
private fun DebugActionButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    snackbarHostState: SnackbarHostState,
    onClick: () -> Unit,
    confirmationMessage: String
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "debug_button_press"
    )

    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Show confirmation snackbar
    LaunchedEffect(isPressed) {
        if (isPressed) {
            snackbarHostState.showSnackbar(
                message = confirmationMessage,
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AsyncDebugActionButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    snackbarHostState: SnackbarHostState,
    onClick: suspend () -> Unit,
    confirmationMessage: String
) {
    var isPressed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "debug_button_press"
    )

    Card(
        onClick = {
            if (!isLoading) {
                isPressed = true
                isLoading = true
                coroutineScope.launch {
                    try {
                        onClick()
                        snackbarHostState.showSnackbar(
                            message = confirmationMessage,
                            duration = SnackbarDuration.Long
                        )
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            message = "Error: ${e.message}",
                            duration = SnackbarDuration.Long
                        )
                    } finally {
                        isLoading = false
                        kotlinx.coroutines.delay(150)
                        isPressed = false
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FooterSection(
    onVersionTap: () -> Unit,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(600, delayMillis = 500))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "HydroTracker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onVersionTap() }
                )
                
                Text(
                    text = "Developed by Ali Cem Çakmak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeveloperOptionsSection(
    userRepository: UserRepository,
    waterIntakeRepository: WaterIntakeRepository,
    snackbarHostState: SnackbarHostState,
    onNavigateToOnboarding: () -> Unit,
    onDisableDeveloperOptions: () -> Unit,
    userProfile: UserProfile?,
    isVisible: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
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
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Developer Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Text(
                text = "These options are for development and testing purposes only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            
            // Disable Developer Options Toggle
            Card(
                onClick = onDisableDeveloperOptions,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Disable Developer Options",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Hide developer options from settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
            )

            // Reset Onboarding Button
            DebugActionButton(
                title = "Reset Onboarding",
                description = "Clear user data and restart onboarding",
                icon = Icons.Default.RestartAlt,
                snackbarHostState = snackbarHostState,
                onClick = {
                    userRepository.resetOnboarding()
                    onNavigateToOnboarding()
                },
                confirmationMessage = "Onboarding reset! Redirecting..."
            )

            // Clear All Data Button
            AsyncDebugActionButton(
                title = "Clear All Data",
                description = "Remove all stored user preferences and water data",
                icon = Icons.Default.DeleteForever,
                snackbarHostState = snackbarHostState,
                onClick = {
                    userRepository.clearUserProfile()
                    waterIntakeRepository.clearAllData()
                },
                confirmationMessage = "All data cleared!"
            )

            AsyncDebugActionButton(
                title = "Inject 30-Day Data",
                description = "Add realistic water intake data for past 30 days",
                icon = Icons.Default.DataObject,
                snackbarHostState = snackbarHostState,
                onClick = {
                    waterIntakeRepository.injectDebugData()
                },
                confirmationMessage = "30 days of realistic data injected! Check History screen."
            )

            // Show Current Status
            val isOnboardingCompleted by userRepository.isOnboardingCompleted.collectAsState()
            val currentUserProfile by userRepository.userProfile.collectAsState()

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Current Status",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Onboarding Completed: $isOnboardingCompleted",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "User Profile Exists: ${currentUserProfile != null}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (currentUserProfile != null) {
                        Text(
                            text = "Daily Goal: ${currentUserProfile!!.dailyWaterGoal.toInt()} ml",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Debug Notification Section
            DebugNotificationSection(
                userProfile = userProfile,
                waterIntakeRepository = waterIntakeRepository,
                snackbarHostState = snackbarHostState,
                isVisible = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    HydroTrackerTheme {
        SettingsScreen()
    }
}
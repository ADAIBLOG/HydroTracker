package com.cemcakmak.hydrotracker.presentation.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.cemcakmak.hydrotracker.data.models.ThemePreferences
import com.cemcakmak.hydrotracker.data.models.DarkModePreference
import com.cemcakmak.hydrotracker.data.models.ColorSource
import com.cemcakmak.hydrotracker.data.models.WeekStartDay
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.repository.UserRepository
import com.cemcakmak.hydrotracker.data.database.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.presentation.common.HydroSnackbarHost
import com.cemcakmak.hydrotracker.ui.theme.HydroTrackerTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Intent
import androidx.compose.ui.res.painterResource
import com.cemcakmak.hydrotracker.R
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import com.cemcakmak.hydrotracker.BuildConfig

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    themePreferences: ThemePreferences = ThemePreferences(),
    userProfile: UserProfile? = null,
    userRepository: UserRepository? = null,
    waterIntakeRepository: WaterIntakeRepository? = null,
    onDarkModeChange: (DarkModePreference) -> Unit = {},
    onColorSourceChange: (ColorSource) -> Unit = {},
    onPureBlackChange: (Boolean) -> Unit = {},
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
    var tapCount by remember { mutableIntStateOf(0) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

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
                    onDarkModeChange = onDarkModeChange,
                    onPureBlackChange = onPureBlackChange,
                    isDynamicColorAvailable = isDynamicColorAvailable,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Display Section
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
                    onWeekStartDayChange = onWeekStartDayChange
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Notification Settings Section
            NotificationSettingsSection(
                userProfile = userProfile,
                onRequestPermission = onRequestNotificationPermission,
                isVisible = isVisible
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                        userProfile = userProfile
                    )
                }
            }
            
            // Support Section
            SupportSection(
                isVisible = isVisible
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // About Section
            AboutSection(
                isVisible = isVisible
            )

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeSection(
    themePreferences: ThemePreferences,
    onColorSourceChange: (ColorSource) -> Unit,
    onDarkModeChange: (DarkModePreference) -> Unit,
    onPureBlackChange: (Boolean) -> Unit,
    isDynamicColorAvailable: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Theme Mode Section
            Column(
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
                        text = "Theme Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Connected Button Groups for Theme Mode
                val haptics = LocalHapticFeedback.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DarkModePreference.entries.forEach { preference ->
                        val isSelected = themePreferences.darkMode == preference
                        
                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = { 
                                onDarkModeChange(preference)
                                haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (preference) {
                                        DarkModePreference.SYSTEM -> if (isSelected) Icons.Filled.Settings else Icons.Outlined.Settings
                                        DarkModePreference.LIGHT -> if (isSelected) Icons.Filled.LightMode else Icons.Outlined.LightMode
                                        DarkModePreference.DARK -> if (isSelected) Icons.Filled.DarkMode else Icons.Outlined.DarkMode
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = when (preference) {
                                        DarkModePreference.SYSTEM -> "System"
                                        DarkModePreference.LIGHT -> "Light"
                                        DarkModePreference.DARK -> "Dark"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            // Color Theme Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dynamic Colors Toggle with Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Dynamic Colors",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDynamicColorAvailable) {
                                "Colors from your wallpaper"
                            } else {
                                "Requires Android 12+"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = themePreferences.colorSource == ColorSource.DYNAMIC_COLOR,
                        onCheckedChange = { enabled ->
                            if (isDynamicColorAvailable) {
                                onColorSourceChange(
                                    if (enabled) ColorSource.DYNAMIC_COLOR else ColorSource.HYDRO_THEME
                                )
                            }
                        },
                        enabled = isDynamicColorAvailable,
                        thumbContent = if (themePreferences.colorSource == ColorSource.DYNAMIC_COLOR) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                }

                // Pure Black Toggle
                val haptics = LocalHapticFeedback.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Pure Black",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "True black backgrounds in dark mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = themePreferences.usePureBlack,
                        onCheckedChange = { enabled ->
                            onPureBlackChange(enabled)
                            haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                        thumbContent = if (themePreferences.usePureBlack) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun DisplaySection(
    themePreferences: ThemePreferences,
    onWeekStartDayChange: (WeekStartDay) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Week Start",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Connected Button Groups for Week Start Day
            val haptics = LocalHapticFeedback.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeekStartDay.entries.forEach { weekStartDay ->
                    val isSelected = themePreferences.weekStartDay == weekStartDay
                    
                    ToggleButton(
                        checked = isSelected,
                        onCheckedChange = { 
                            onWeekStartDayChange(weekStartDay)
                            haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (weekStartDay) {
                                    WeekStartDay.SUNDAY -> if (isSelected) Icons.Filled.Weekend else Icons.Outlined.Weekend
                                    WeekStartDay.MONDAY -> if (isSelected) Icons.Filled.Today else Icons.Outlined.Today
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when (weekStartDay) {
                                    WeekStartDay.SUNDAY -> "Sunday"
                                    WeekStartDay.MONDAY -> "Monday"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
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
private fun SupportSection(
    isVisible: Boolean
) {
    val context = LocalContext.current
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(600, delayMillis = 450))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Support Development",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Text(
                    text = "If you like to support my work, you can donate me :)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // PayPal Button
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://www.paypal.com/donate/?hosted_button_id=CQUZLNRM79CAU".toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF003087)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.paypal),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.inverseOnSurface
                            )
                            Text(
                                text = "PayPal",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.inverseOnSurface
                            )
                        }
                    }
                    
                    // Buy Me a Coffee Button
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://buymeacoffee.com/thegadgetgeek".toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFFDD00)
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.coffee),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Buy Me Coffee",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
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
                containerColor = MaterialTheme.colorScheme.surface
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
                    text = "Version ${BuildConfig.VERSION_NAME}",
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
    userProfile: UserProfile?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
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
            ResetOnboardingButton(
                snackbarHostState = snackbarHostState,
                onClick = {
                    userRepository.resetOnboarding()
                    onNavigateToOnboarding()
                }
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

@Composable
private fun ResetOnboardingButton(
    snackbarHostState: SnackbarHostState,
    onClick: () -> Unit
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
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reset Onboarding",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Clear user data and restart onboarding",
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
                message = "Onboarding reset! Redirecting...",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
private fun AboutSection(
    isVisible: Boolean
) {
    val context = LocalContext.current
    var showLicenseBottomSheet by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(animationSpec = tween(600, delayMillis = 475))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Open Source License
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLicenseBottomSheet = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
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
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Open Source License",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "View the software license",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }

    // License Bottom Sheet
    if (showLicenseBottomSheet) {
        LicenseBottomSheet(
            onDismiss = { showLicenseBottomSheet = false },
            context = context
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenseBottomSheet(
    onDismiss: () -> Unit,
    context: android.content.Context
) {
    val sheetState = rememberModalBottomSheetState()
    var licenseText by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        licenseText = try {
            loadLicenseText(context)
        } catch (e: Exception) {
            "Error loading license: ${e.message}"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Source License",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // License Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ParsedMarkdownText(
                    text = licenseText,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ParsedMarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val lines = text.split("\n")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (line in lines) {
            when {
                line.startsWith("# ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = line.substring(2),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = line.substring(3),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                line.startsWith("### ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = line.substring(4),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.startsWith("- ") -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(line.substring(2)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                line.trim().startsWith("**") && line.trim().endsWith("**") -> {
                    Text(
                        text = line.trim().removeSurrounding("**"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                line.trim() == "---" -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                    )
                }
            }
        }
    }
}

@Composable
private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()

        val matches = boldRegex.findAll(text).toList()

        for (match in matches) {
            // Add text before the match
            append(text.substring(currentIndex, match.range.first))

            // Add bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(match.groupValues[1])
            }

            currentIndex = match.range.last + 1
        }

        // Add remaining text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

private fun loadLicenseText(context: android.content.Context): String {
    return context.assets.open("LICENSE.md").bufferedReader().use { it.readText() }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    HydroTrackerTheme {
        SettingsScreen()
    }
}
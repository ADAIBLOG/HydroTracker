package com.cemcakmak.hydrotracker.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.cemcakmak.hydrotracker.data.models.UserProfile
import com.cemcakmak.hydrotracker.data.models.ContainerPreset
import com.cemcakmak.hydrotracker.data.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.data.repository.WaterProgress
import com.cemcakmak.hydrotracker.data.repository.TodayStatistics
import com.cemcakmak.hydrotracker.data.database.entities.WaterIntakeEntry
import com.cemcakmak.hydrotracker.utils.WaterCalculator
import com.cemcakmak.hydrotracker.presentation.common.HydroSnackbarHost
import com.cemcakmak.hydrotracker.presentation.common.showSuccessSnackbar
import com.cemcakmak.hydrotracker.presentation.common.showErrorSnackbar

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userProfile: UserProfile,
    waterIntakeRepository: WaterIntakeRepository,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    // Collect real-time water intake data from database
    val todayProgress by waterIntakeRepository.getTodayProgress().collectAsState(
        initial = WaterProgress(
            currentIntake = 0.0,
            dailyGoal = userProfile.dailyWaterGoal,
            progress = 0f,
            isGoalAchieved = false,
            remainingAmount = userProfile.dailyWaterGoal
        )
    )

    val todayEntries by waterIntakeRepository.getTodayEntries().collectAsState(initial = emptyList())

    val todayStatistics by waterIntakeRepository.getTodayStatistics().collectAsState(
        initial = TodayStatistics(
            totalIntake = 0.0,
            goalProgress = 0f,
            entryCount = 0,
            averageIntake = 0.0,
            largestIntake = 0.0,
            firstIntakeTime = null,
            lastIntakeTime = null,
            isGoalAchieved = false,
            remainingAmount = userProfile.dailyWaterGoal
        )
    )

    // Coroutine scope for database operations
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Custom entry dialog state
    var showCustomDialog by remember { mutableStateOf(false) }

    // Function to add water intake to database
    fun addWaterIntake(amount: Double, containerName: String) {
        coroutineScope.launch {
            val containerPreset = ContainerPreset.getDefaultPresets()
                .find { it.name == containerName }
                ?: ContainerPreset(name = "Custom", volume = amount)

            val result = waterIntakeRepository.addWaterIntake(
                amount = amount,
                containerPreset = containerPreset
            )

            result.onSuccess {
                snackbarHostState.showSuccessSnackbar(
                    message = "Added ${WaterCalculator.formatWaterAmount(amount)}!"
                )
            }.onFailure { error ->
                snackbarHostState.showErrorSnackbar(
                    message = "Failed to add water: ${error.message}"
                )
            }
        }
    }

    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Animate the progress value
    val animatedProgress by animateFloatAsState(
        targetValue = todayProgress.progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progress_animation"
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val elevated by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0f }
    }
    val animatedElevation by animateDpAsState(
        targetValue = if (elevated) 6.dp else 0.dp,
        label = "AppBarElevation"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Surface(
                tonalElevation = animatedElevation,
                shadowElevation = animatedElevation
            ) {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            text = "HydroTracker",
                            style = MaterialTheme.typography.headlineLargeEmphasized,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    alwaysShowLabel = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "History") },
                    label = { Text("History") },
                    selected = false,
                    alwaysShowLabel = true,
                    onClick = onNavigateToHistory
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    alwaysShowLabel = true,
                    onClick = onNavigateToProfile
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCustomDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Add Custom Amount"
                )
                Text(
                    text = "Add Custom",
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        },
        snackbarHost = { HydroSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Daily Progress Section
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffsetY = { it / 3 }
                ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Daily Progress",
                            style = MaterialTheme.typography.headlineLargeEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Progress amount display
                        Text(
                            text = "${todayProgress.getFormattedCurrent()} / ${todayProgress.getFormattedGoal()}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Wavy Progress Indicator
                        LinearWavyProgressIndicator(
                            progress = { animatedProgress },
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
                            text = getMotivationalMessage(todayProgress.progress, userProfile, todayProgress.isGoalAchieved),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Additional stats row
                        if (todayStatistics.entryCount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatChip(
                                    label = "Entries",
                                    value = "${todayStatistics.entryCount}"
                                )
                                if (todayStatistics.firstIntakeTime != null) {
                                    StatChip(
                                        label = "First",
                                        value = todayStatistics.firstIntakeTime!!
                                    )
                                }
                                if (todayStatistics.lastIntakeTime != null && todayStatistics.entryCount > 1) {
                                    StatChip(
                                        label = "Latest",
                                        value = todayStatistics.lastIntakeTime!!
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = MaterialTheme.shapes.extraLargeIncreased
            ){
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
                    val presets = remember { ContainerPreset.getDefaultPresets() }
                    val carouselState = rememberCarouselState { presets.size }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 12.dp)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Quick Select",
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        HorizontalMultiBrowseCarousel(
                            state = carouselState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(135.dp),
                            preferredItemWidth = 130.dp,
                            itemSpacing = 8.dp,
                        ) { index ->
                            val preset = presets[index]
                            CarouselWaterCard(
                                preset = preset,
                                onClick = { addWaterIntake(preset.volume, preset.name) },
                                modifier = Modifier
                                    .height(130.dp)
                                    .maskClip(MaterialTheme.shapes.extraLarge)
                            )
                        }
                    }
                }

                // Recent Entries Section
                if (todayEntries.isNotEmpty()) {
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
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Recent Entries",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                todayEntries.forEach { entry ->
                                    RecentEntryItem(entry = entry)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Custom Water Entry Dialog
    if (showCustomDialog) {
        CustomWaterDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount ->
                addWaterIntake(amount, "Custom")
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun CarouselWaterCard(
    preset: ContainerPreset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                preset.iconRes != null -> {
                    Icon(
                        painter = painterResource(preset.iconRes),
                        contentDescription = preset.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                preset.icon != null -> {
                    Icon(
                        imageVector = preset.icon,
                        contentDescription = preset.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = preset.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Text(
                text = preset.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Text(
                text = preset.getFormattedVolume(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomWaterDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLargeIncreased,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Custom Amount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = false
                    },
                    label = { Text("Amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Please enter a valid amount (1-5000 ml)") }
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
                        shapes = ButtonDefaults.shapes(),
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount != null && amount > 0 && amount <= 5000) {
                                onConfirm(amount)
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RecentEntryItem(entry: WaterIntakeEntry) {
    // Find a matching preset to fetch its icon (res or vector)
    val preset = remember(entry.containerType) {
        ContainerPreset.getDefaultPresets()
            .firstOrNull { it.name == entry.containerType }
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        leadingContent = {
            // Tonal icon container
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        preset?.iconRes != null -> {
                            Icon(
                                painter = painterResource(preset.iconRes),
                                contentDescription = preset.name,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        preset?.icon != null -> {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = preset.name,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = entry.containerType,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        headlineContent = {
            Text(
                text = entry.containerType,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = entry.getFormattedTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                text = entry.getFormattedAmount(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}

private fun getMotivationalMessage(progress: Float, userProfile: UserProfile, isGoalAchieved: Boolean): String {
    return when {
        isGoalAchieved -> "🎉 Amazing! You've reached your daily goal!"
        progress >= 0.75f -> "💪 You're doing great! Almost there!"
        progress >= 0.5f -> "🌟 Halfway there! Keep up the good work!"
        progress >= 0.25f -> "👍 Good start! Stay consistent!"
        else -> userProfile.activityLevel.getHydrationTip()
    }
}
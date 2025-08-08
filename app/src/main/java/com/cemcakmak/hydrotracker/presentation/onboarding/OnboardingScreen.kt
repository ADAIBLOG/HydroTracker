package com.cemcakmak.hydrotracker.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cemcakmak.hydrotracker.ui.theme.HydroTrackerTheme

/**
 * Onboarding step enumeration
 */
enum class OnboardingStep {
    WELCOME,
    GENDER,
    AGE,
    ACTIVITY,
    SCHEDULE,
    GOAL,
    COMPLETE
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit = {},
    viewModel: OnboardingViewModel = viewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val canGoNext by viewModel.canGoNext.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()

    val progressValue by remember(currentStep) {
        derivedStateOf { viewModel.getProgress() }
    }

    // Animation states
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Handle completion - FIXED: This should properly trigger navigation
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            println("DEBUG: OnboardingViewModel reports completion, calling navigation callback")
            onNavigateToHome()
        }
    }

    // Animate the progress value
    val animatedProgress by animateFloatAsState(
        targetValue = progressValue.coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "DeterminateProgressAnimation"
    )

    Scaffold(
        topBar = {
            if (currentStep != OnboardingStep.WELCOME) {
                TopAppBar(
                    title = {
                        LinearWavyProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth(),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary,
                            stroke = WavyProgressIndicatorDefaults.linearIndicatorStroke,
                            trackStroke = WavyProgressIndicatorDefaults.linearTrackStroke,
                            gapSize = WavyProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
                            stopSize = WavyProgressIndicatorDefaults.LinearTrackStopIndicatorSize,
                            amplitude = WavyProgressIndicatorDefaults.indicatorAmplitude,
                            wavelength = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
                            waveSpeed = WavyProgressIndicatorDefaults.LinearDeterminateWavelength
                        )
                    },
                    navigationIcon = {
                        if (canGoBack) {
                            IconButton(
                                onClick = { viewModel.previousStep() },
                                enabled = !isAnimating,
                                shapes = IconButtonDefaults.shapes(),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val goingForwards = targetState.ordinal > initialState.ordinal

                    val enterTransition: EnterTransition
                    val exitTransition: ExitTransition

                    if (goingForwards) {
                        enterTransition = slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(animationSpec = tween(200, delayMillis = 100))

                        exitTransition = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeOut(animationSpec = tween(200))

                    } else { // Going backwards
                        enterTransition = slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) + fadeIn(animationSpec = tween(200, delayMillis = 100))

                        exitTransition = slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeOut(animationSpec = tween(200))
                    }

                    enterTransition togetherWith exitTransition
                },
                modifier = Modifier.fillMaxSize(),
                label = "onboarding_step_transition"
            ) { step ->
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep(
                        onNext = { viewModel.nextStep() },
                        onSkip = {
                            viewModel.skipOnboarding()
                            // Skip will trigger isCompleted, which will trigger navigation via LaunchedEffect
                        },
                        isVisible = isVisible
                    )
                    OnboardingStep.GENDER -> GenderStep(
                        selectedGender = userProfile.gender,
                        onGenderSelected = { viewModel.updateGender(it) },
                        title = viewModel.getStepTitle(),
                        description = viewModel.getStepDescription()
                    )
                    OnboardingStep.AGE -> AgeStep(
                        selectedAgeGroup = userProfile.ageGroup,
                        onAgeGroupSelected = { viewModel.updateAgeGroup(it) },
                        title = viewModel.getStepTitle(),
                        description = viewModel.getStepDescription()
                    )
                    OnboardingStep.ACTIVITY -> ActivityStep(
                        selectedActivityLevel = userProfile.activityLevel,
                        onActivityLevelSelected = { viewModel.updateActivityLevel(it) },
                        title = viewModel.getStepTitle(),
                        description = viewModel.getStepDescription()
                    )
                    OnboardingStep.SCHEDULE -> ScheduleStep(
                        wakeUpTime = userProfile.wakeUpTime,
                        sleepTime = userProfile.sleepTime,
                        onWakeUpTimeChanged = { viewModel.updateWakeUpTime(it) },
                        onSleepTimeChanged = { viewModel.updateSleepTime(it) },
                        title = viewModel.getStepTitle(),
                        description = viewModel.getStepDescription()
                    )
                    OnboardingStep.GOAL -> GoalStep(
                        userProfile = userProfile,
                        title = viewModel.getStepTitle(),
                        description = viewModel.getStepDescription()
                    )
                    OnboardingStep.COMPLETE -> CompleteStep(
                        userProfile = userProfile,
                        onComplete = {
                            println("DEBUG: Complete button clicked")
                            viewModel.completeOnboarding()
                            // Completion will trigger isCompleted, which will trigger navigation via LaunchedEffect
                        }
                    )
                }
            }

            // Navigation buttons
            if (currentStep != OnboardingStep.WELCOME && currentStep != OnboardingStep.COMPLETE) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Skip button
                    TextButton(
                        onClick = {
                            viewModel.skipOnboarding()
                        },
                        enabled = !isAnimating
                    ) {
                        Text("Skip")
                    }

                    // Next button
                    Button(
                        shapes = ButtonDefaults.shapes(),
                        onClick = { viewModel.nextStep() },
                        enabled = canGoNext && !isAnimating,
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text(
                            text = if (currentStep == OnboardingStep.GOAL) "Finish" else "Next",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    HydroTrackerTheme {
        OnboardingScreen(
            onNavigateToHome = {}
        )
    }
}
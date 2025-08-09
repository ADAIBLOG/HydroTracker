package com.cemcakmak.hydrotracker

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.compose.animation.*
import androidx.compose.ui.graphics.TransformOrigin
import com.cemcakmak.hydrotracker.data.repository.*
import com.cemcakmak.hydrotracker.data.database.DatabaseInitializer
import com.cemcakmak.hydrotracker.data.database.repository.WaterIntakeRepository
import com.cemcakmak.hydrotracker.presentation.common.*
import com.cemcakmak.hydrotracker.presentation.home.HomeScreen
import com.cemcakmak.hydrotracker.presentation.history.HistoryScreen
import com.cemcakmak.hydrotracker.presentation.profile.ProfileScreen
import com.cemcakmak.hydrotracker.presentation.settings.SettingsScreen
import com.cemcakmak.hydrotracker.presentation.onboarding.*
import com.cemcakmak.hydrotracker.notifications.*
import com.cemcakmak.hydrotracker.ui.theme.HydroTrackerTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
class MainActivity : ComponentActivity() {
    private lateinit var userRepository: UserRepository
    private lateinit var waterIntakeRepository: WaterIntakeRepository

    // Modern permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val userProfile = userRepository.userProfile.value
            userProfile?.takeIf { it.isOnboardingCompleted }?.let {
                HydroNotificationScheduler.startNotifications(this, it)
            }
        }
        // Handle denied case if needed - currently no-op as per original logic
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.isNavigationBarContrastEnforced = false

        // Init
        userRepository = UserRepository(applicationContext)
        waterIntakeRepository = DatabaseInitializer.getWaterIntakeRepository(
            applicationContext, userRepository
        )

        setContent {
            HydroTrackerApp(userRepository, waterIntakeRepository, notificationPermissionLauncher)
        }
    }

}

@Composable
fun HydroTrackerApp(
    userRepository: UserRepository,
    waterIntakeRepository: WaterIntakeRepository,
    notificationPermissionLauncher: ActivityResultLauncher<String>
) {
    val navController = rememberNavController()
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(userRepository))
    val themePreferences by themeViewModel.themePreferences.collectAsState()
    val userProfile by userRepository.userProfile.collectAsState()
    val isOnboardingCompleted by userRepository.isOnboardingCompleted.collectAsState()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(isOnboardingCompleted, userProfile) {
        isLoading = false
        
        // Check for new user day when app starts
        if (isOnboardingCompleted && userProfile != null) {
            waterIntakeRepository.checkAndHandleNewUserDay()
        }
    }

    val startDestination = if (isOnboardingCompleted && userProfile != null)
        NavigationRoutes.HOME else NavigationRoutes.ONBOARDING

    HydroTrackerTheme(themePreferences = themePreferences) {
        if (isLoading) {
            LoadingScreen()
        } else {
            val currentRoute by navController.currentBackStackEntryAsState()
            val route = currentRoute?.destination?.route ?: startDestination

            MainNavigationScaffold(navController = navController, currentRoute = route) { padding ->
                NavHost(
                    navController = navController, 
                    startDestination = startDestination,
                    // Predictive back gesture animations
                    popExitTransition = {
                        scaleOut(
                            targetScale = 0.7f,
                            transformOrigin = TransformOrigin(
                                pivotFractionX = 1f,
                                pivotFractionY = 0.5f
                            )
                        ) + fadeOut()
                    },
                    popEnterTransition = {
                        scaleIn(
                            initialScale = 1.1f,
                            transformOrigin = TransformOrigin(
                                pivotFractionX = 0.4f,
                                pivotFractionY = 0.5f
                            )
                        )
                    }
                ) {

                    composable(NavigationRoutes.ONBOARDING) {
                        val context = LocalContext.current
                        val onboardingVM: OnboardingViewModel = viewModel(
                            factory = OnboardingViewModelFactory(context.applicationContext as Application, userRepository)
                        )

                        OnboardingScreen(
                            onNavigateToHome = {
                                navController.navigate(NavigationRoutes.HOME) {
                                    popUpTo(NavigationRoutes.ONBOARDING) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            viewModel = onboardingVM
                        )
                    }

                    composable(NavigationRoutes.HOME) {
                        userProfile?.let {
                            HomeScreen(
                                userProfile = it,
                                waterIntakeRepository = waterIntakeRepository,
                                onNavigateToHistory = { navController.navigate(NavigationRoutes.HISTORY) },
                                onNavigateToSettings = { navController.navigate(NavigationRoutes.SETTINGS) },
                                onNavigateToProfile = { navController.navigate(NavigationRoutes.PROFILE) }
                            )
                        } ?: LoadingScreen()
                    }

                    composable(NavigationRoutes.HISTORY) {
                        HistoryScreen(waterIntakeRepository) {
                            navController.popBackStack()
                        }
                    }

                    composable(NavigationRoutes.PROFILE) {
                        userProfile?.let {
                            ProfileScreen(
                                userProfile = it,
                                userRepository = userRepository,
                                waterIntakeRepository = waterIntakeRepository,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        } ?: LoadingScreen()
                    }

                    composable(NavigationRoutes.SETTINGS) {
                        SettingsScreen(
                            themePreferences = themePreferences,
                            userProfile = userProfile,
                            userRepository = userRepository,
                            waterIntakeRepository = waterIntakeRepository,
                            onColorSourceChange = themeViewModel::setColorSource,
                            onDarkModeChange = themeViewModel::updateDarkModePreference,
                            onThemeToggle = themeViewModel::toggleDynamicColor,
                            isDynamicColorAvailable = themeViewModel.isDynamicColorAvailable(),
                            onRequestNotificationPermission = {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            },
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToOnboarding = {
                                navController.navigate(NavigationRoutes.ONBOARDING) {
                                    popUpTo(NavigationRoutes.HOME) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading HydroTracker...",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Checking your hydration data",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// MainNavigationScaffold.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/common/MainNavigationScaffold.kt

package com.cemcakmak.hydrotracker.presentation.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Main navigation scaffold with bottom navigation bar
 * Provides consistent navigation across Home, History, and Profile screens
 * Features Material 3 Expressive animations and design
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainNavigationScaffold(
    navController: NavController,
    currentRoute: String,
    content: @Composable (PaddingValues) -> Unit
) {
    // Animation states for navigation items
    val selectedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "selected_scale"
    )

    val shouldShowBottomBar = when (currentRoute) {
        NavigationRoutes.HOME, NavigationRoutes.HISTORY, NavigationRoutes.PROFILE -> true
        NavigationRoutes.SETTINGS, NavigationRoutes.ONBOARDING -> false
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                HydroNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        },
        content = content
    )
}

@Composable
private fun HydroNavigationBar(
    navController: NavController,
    currentRoute: String
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationItem.values().forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    AnimatedNavigationIcon(
                        icon = item.icon,
                        selectedIcon = item.selectedIcon,
                        isSelected = isSelected,
                        contentDescription = item.label
                    )
                },
                label = {
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            initialOffsetY = { it / 2 }
                        ) + fadeIn(animationSpec = tween(300)),
                        exit = slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            targetOffsetY = { it / 2 }
                        ) + fadeOut(animationSpec = tween(200))
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(NavigationRoutes.HOME) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AnimatedNavigationIcon(
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    contentDescription: String?
) {
    // Scale animation for selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    // Icon transition animation
    AnimatedContent(
        targetState = if (isSelected) selectedIcon else icon,
        transitionSpec = {
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                initialScale = 0.8f
            ) + fadeIn(
                animationSpec = tween(300)
            ) togetherWith scaleOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                targetScale = 0.8f
            ) + fadeOut(
                animationSpec = tween(200)
            )
        },
        label = "icon_transition"
    ) { targetIcon ->
        Icon(
            imageVector = targetIcon,
            contentDescription = contentDescription,
            modifier = Modifier.size((24 * scale).dp)
        )
    }
}

/**
 * Navigation items for the bottom navigation bar
 */
enum class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(
        route = NavigationRoutes.HOME,
        label = "Home",
        icon = Icons.Default.Home,
        selectedIcon = Icons.Filled.Home
    ),
    HISTORY(
        route = NavigationRoutes.HISTORY,
        label = "History",
        icon = Icons.Default.Analytics,
        selectedIcon = Icons.Filled.Analytics
    ),
    PROFILE(
        route = NavigationRoutes.PROFILE,
        label = "Profile",
        icon = Icons.Default.Person,
        selectedIcon = Icons.Filled.Person
    )
}
// MainNavigationScaffold.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/common/MainNavigationScaffold.kt

package com.cemcakmak.hydrotracker.presentation.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainNavigationScaffold(
    navController: NavController,
    currentRoute: String,
    content: @Composable (PaddingValues) -> Unit
) {
    val shouldShowBottomBar = when (currentRoute) {
        NavigationRoutes.HOME, NavigationRoutes.HISTORY, NavigationRoutes.PROFILE -> true
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        NavigationItem.entries.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMediumEmphasized
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(NavigationRoutes.HOME) { saveState = true }
                            launchSingleTop = true
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

enum class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(
        route = NavigationRoutes.HOME,
        label = "Home",
        icon = Icons.Filled.Home,
        selectedIcon = Icons.Filled.Home
    ),
    HISTORY(
        route = NavigationRoutes.HISTORY,
        label = "History",
        icon = Icons.Filled.Analytics,
        selectedIcon = Icons.Filled.Analytics
    ),
    PROFILE(
        route = NavigationRoutes.PROFILE,
        label = "Profile",
        icon = Icons.Filled.Person,
        selectedIcon = Icons.Filled.Person
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun MainNavigationScaffoldPreview() {
    val navController = rememberNavController()
    MainNavigationScaffold(
        navController = navController,
        currentRoute = NavigationRoutes.HOME,
        content = { paddingValues ->
            Text(
                text = "Sample Content",
                modifier = Modifier.size(paddingValues.calculateBottomPadding())
            )
        }
    )
}

@Preview
@Composable
fun HydroNavigationBarPreview() {
    val navController = rememberNavController()
    HydroNavigationBar(
        navController = navController,
        currentRoute = NavigationRoutes.HOME
    )
}

package com.ion.daily_tracking

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ion.daily_tracking.ui.EditItemScreen
import com.ion.daily_tracking.ui.ProfileScreen
import com.ion.daily_tracking.ui.ProfileViewModel
import com.ion.daily_tracking.ui.ScheduleScreen
import com.ion.daily_tracking.ui.ScheduleViewModel
import com.ion.daily_tracking.ui.theme.DailyTrackingTheme

class MainActivity : ComponentActivity() {

    private val requestNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeAskForNotifications()
        setContent {
            DailyTrackingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun maybeAskForNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Today("schedule", "Today", Icons.Filled.DateRange),
    Profile("profile", "Profile", Icons.Filled.Person),
}

@Composable
private fun AppNavigation() {
    val navController = rememberNavController()
    val scheduleViewModel: ScheduleViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route in Tab.entries.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.Today.route) {
                ScheduleScreen(
                    viewModel = scheduleViewModel,
                    onAddItem = { navController.navigate("edit") },
                    onEditItem = { id -> navController.navigate("edit?id=$id") },
                )
            }
            composable(Tab.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(viewModel = profileViewModel)
            }
            composable(
                route = "edit?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: -1L
                EditItemScreen(
                    viewModel = scheduleViewModel,
                    itemId = id,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}

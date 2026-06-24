package com.ion.daily_tracking

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ion.daily_tracking.ui.EditItemScreen
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

@Composable
private fun AppNavigation() {
    val navController = rememberNavController()
    // Activity-scoped VM so the schedule list and edit screen share the same selected day.
    val viewModel: ScheduleViewModel = viewModel()

    NavHost(navController = navController, startDestination = "schedule") {
        composable("schedule") {
            ScheduleScreen(
                viewModel = viewModel,
                onAddItem = { navController.navigate("edit") },
                onEditItem = { id -> navController.navigate("edit?id=$id") },
            )
        }
        composable(
            route = "edit?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            EditItemScreen(
                viewModel = viewModel,
                itemId = id,
                onDone = { navController.popBackStack() },
            )
        }
    }
}

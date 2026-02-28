package com.beatrunner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object WorkoutDetail : Screen("workout_detail/{workoutId}", "Workout Detail") {
        fun createRoute(workoutId: String) = "workout_detail/$workoutId"
    }
    object Bluetooth : Screen("bluetooth", "Bluetooth Devices")
    object Workout : Screen("workout", "Active Workout")
}

package com.beatrunner.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.beatrunner.ui.bluetooth.BluetoothScreen
import com.beatrunner.ui.history.HistoryScreen
import com.beatrunner.ui.navigation.Screen
import com.beatrunner.ui.settings.SettingsScreen


@Composable
fun MainScreen(
    isDeviceConnected: Boolean,
    onNavigateToWorkout: () -> Unit,
    onNavigateToBluetooth: () -> Unit = {}, // Added for compatibility if needed, but handled inside via checking isDeviceConnected? NO, let's keep it simple.
    // Actually, AppNavigation handles the callback. 
    // Let's refine: MainScreen should probably just take one callback for "Start Action" and decide? 
    // No, the requirement is "If connected -> open workout, else -> open bluetooth".
    // So MainScreen needs to know connectivity.
    // And it needs to be able to navigate to Bluetooth.
    // Current AppNavigation only passes onNavigateToWorkout. 
    // We should probably rely on navController inside MainScreen to navigate to Bluetooth? 
    // Yes, Screen.Bluetooth is available in MainScreen's NavHost? 
    // Wait, Screen.Bluetooth is IN MainScreen's NavHost.
    // So MainScreen CAN navigate to Bluetooth.
    
    // BUT, AppNavigation passes onNavigateToWorkout which calls viewModel.startWorkout().
    // We need to change the FAB behavior.
    
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEachIndexed { index, screen ->
                    // Add spacer for FAB in the middle
                    if (index == 1) {
                        NavigationBarItem(
                            selected = false,
                            onClick = { },
                            icon = { },
                            enabled = false,
                            modifier = Modifier.weight(0.5f) // Adjust as needed
                        )
                    }

                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isDeviceConnected) {
                        onNavigateToWorkout()
                    } else {
                        navController.navigate(Screen.Bluetooth.route)
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start Workout")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.History.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateToDetail = { workoutId ->
                        // Todo: Implement navigation to detail
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToBluetooth = {
                        navController.navigate(Screen.Bluetooth.route)
                    }
                )
            }
            composable(Screen.Bluetooth.route) {
                BluetoothScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

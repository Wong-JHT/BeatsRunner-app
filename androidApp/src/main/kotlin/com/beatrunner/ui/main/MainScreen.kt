package com.beatrunner.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.beatrunner.ui.bluetooth.BluetoothScreen
import com.beatrunner.ui.history.HistoryScreen
import com.beatrunner.ui.home.HomeScreen
import com.beatrunner.ui.navigation.Screen
import com.beatrunner.ui.settings.SettingsScreen

private val BgDark = Color(0xFF0A1A0F)
private val PrimaryGreen = Color(0xFF14D359)
private val NavBarBg = Color(0xFF0D1F12)
private val NavUnselected = Color(0xFF4A6B52)

@Composable
fun MainScreen(
    isDeviceConnected: Boolean,
    connectedDeviceName: String? = null,
    userName: String? = null,
    onNavigateToWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            NavigationBar(
                containerColor = NavBarBg,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon!!,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            unselectedIconColor = NavUnselected,
                            unselectedTextColor = NavUnselected,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    userName = userName ?: "Runner",
                    isDeviceConnected = isDeviceConnected,
                    connectedDeviceName = connectedDeviceName,
                    onStartWorkout = {
                        if (isDeviceConnected) {
                            onNavigateToWorkout()
                        } else {
                            navController.navigate(Screen.Bluetooth.route)
                        }
                    },
                    onConnectDevice = {
                        navController.navigate(Screen.Bluetooth.route)
                    }
                )
            }
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

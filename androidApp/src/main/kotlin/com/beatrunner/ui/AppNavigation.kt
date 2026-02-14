package com.beatrunner.ui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.beatrunner.ui.auth.LoginScreen
import com.beatrunner.ui.auth.RegisterScreen
import com.beatrunner.ui.main.MainScreen
import com.beatrunner.ui.workout.WorkoutScreen
import com.beatrunner.viewmodel.AuthViewModel
import com.beatrunner.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

/** Main app navigation logic Routes between authentication and main app based on login state */
@Composable
fun AppNavigation(
        authViewModel: AuthViewModel,
        workoutViewModel: WorkoutViewModel,
        modifier: Modifier = Modifier
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isWorkoutActive by workoutViewModel.isWorkoutActive.collectAsState()
    var showRegister by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        if (isWorkoutActive) {
            WorkoutScreen(viewModel = workoutViewModel, modifier = modifier)
        } else {
            // User is logged in - show main app
            val bluetoothState by workoutViewModel.bluetoothState.collectAsState()
            val connectedDevice by workoutViewModel.connectedDevice.collectAsState()
            val isDeviceConnected =
                    bluetoothState is
                            com.beatrunner.domain.bluetooth.BluetoothConnectionState.Connected
            val scope = rememberCoroutineScope()

            MainScreen(
                    isDeviceConnected = isDeviceConnected,
                    onNavigateToWorkout = {
                        // Only navigate if device is connected
                        scope.launch { workoutViewModel.startWorkout() }
                    },
                    onNavigateToBluetooth = {
                        // This callback might not be needed if MainScreen handles the navigation
                        // logic internally based on the state
                        // But for now, we'll keep the MainScreen signature clean or update it to
                        // handle navigation request
                    },
                    modifier = modifier
            )
        }
    } else {
        // User not logged in - show auth flow
        if (showRegister) {
            RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        showRegister = false
                        // isLoggedIn will auto-update and route to WorkoutScreen
                    },
                    onNavigateToLogin = { showRegister = false },
                    modifier = modifier
            )
        } else {
            LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // isLoggedIn will auto-update and route to WorkoutScreen
                    },
                    onNavigateToRegister = { showRegister = true },
                    modifier = modifier
            )
        }
    }
}

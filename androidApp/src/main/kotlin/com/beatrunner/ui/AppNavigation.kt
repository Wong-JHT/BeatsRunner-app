package com.beatrunner.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.beatrunner.domain.bluetooth.BluetoothConnectionState
import com.beatrunner.ui.auth.LoginScreen
import com.beatrunner.ui.auth.RegisterScreen
import com.beatrunner.ui.main.MainScreen
import com.beatrunner.ui.profile.ProfileSetupStep1Screen
import com.beatrunner.ui.profile.ProfileSetupStep2Screen
import com.beatrunner.ui.profile.ProfileSetupStep3Screen
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
    val isBasicInfoCompleted by authViewModel.isBasicInfoCompleted.collectAsState()
    val isFitnessLevelCompleted by authViewModel.isFitnessLevelCompleted.collectAsState()
    val isFitnessGoalCompleted by authViewModel.isFitnessGoalCompleted.collectAsState()
    val isWorkoutActive by workoutViewModel.isWorkoutActive.collectAsState()
    var showRegister by remember { mutableStateOf(false) }
    var forceStep1 by remember { mutableStateOf(false) }
    var forceStep2 by remember { mutableStateOf(false) }

    if (isLoggedIn) {
        if (isBasicInfoCompleted == null) {
            // Show a simple loading state or nothing while waiting for profile to load
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF14D359))
            }
        } else if (isBasicInfoCompleted == false || forceStep1) {
            ProfileSetupStep1Screen(
                viewModel = authViewModel,
                onBack = { 
                    if (forceStep1) {
                        forceStep1 = false 
                    } else {
                        authViewModel.logout() 
                    }
                },
                onContinue = {
                    forceStep1 = false
                    // isBasicInfoCompleted will auto-update and route to next step
                },
                modifier = modifier
            )
        } else if (isFitnessLevelCompleted == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF14D359))
            }
        } else if (isFitnessLevelCompleted == false || forceStep2) {
            ProfileSetupStep2Screen(
                viewModel = authViewModel,
                onBack = { 
                    if (forceStep2) {
                        forceStep2 = false
                    } else {
                        forceStep1 = true 
                    }
                },
                onSkip = { 
                    forceStep2 = false
                    authViewModel.skipFitnessLevelSetup()
                },
                onContinue = {
                    forceStep2 = false
                    // isFitnessLevelCompleted will auto-update and route to next step
                },
                modifier = modifier
            )
        } else if (isFitnessGoalCompleted == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF14D359))
            }
        } else if (isFitnessGoalCompleted == false) {
            ProfileSetupStep3Screen(
                viewModel = authViewModel,
                onBack = { forceStep2 = true },
                onSkip = { 
                    authViewModel.skipFitnessGoalSetup()
                },
                onContinue = {
                    // isFitnessGoalCompleted will auto-update and route to MainScreen
                },
                modifier = modifier
            )
        } else if (isWorkoutActive) {
            WorkoutScreen(viewModel = workoutViewModel, modifier = modifier)
        } else {
            // User is logged in and basic info is completed - show main app
            val bluetoothState by workoutViewModel.bluetoothState.collectAsState()
            val connectedDevice by workoutViewModel.connectedDevice.collectAsState()
            val isDeviceConnected =
                    bluetoothState is
                            BluetoothConnectionState.Connected
            val scope = rememberCoroutineScope()
            val currentUsername by authViewModel.currentUsername.collectAsState()

            MainScreen(
                    isDeviceConnected = isDeviceConnected,
                    connectedDeviceName = connectedDevice?.name,
                    userName = currentUsername,
                    onNavigateToWorkout = {
                        // Only navigate if device is connected
                        scope.launch { workoutViewModel.startWorkout() }
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

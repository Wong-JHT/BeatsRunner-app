package com.beatrunner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import com.beatrunner.ui.AppNavigation
import com.beatrunner.ui.theme.BeatRunnerTheme
import com.beatrunner.viewmodel.AuthViewModel
import com.beatrunner.viewmodel.WorkoutViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by inject()
    private val workoutViewModel: WorkoutViewModel by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeatRunnerTheme {
                AppNavigation(
                    authViewModel = authViewModel,
                    workoutViewModel = workoutViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

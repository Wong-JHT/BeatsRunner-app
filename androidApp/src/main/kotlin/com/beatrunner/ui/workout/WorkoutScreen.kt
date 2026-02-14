package com.beatrunner.ui.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.beatrunner.domain.bluetooth.BluetoothConnectionState
import com.beatrunner.domain.bluetooth.BluetoothModels
import com.beatrunner.domain.music.SongInfo
import com.beatrunner.ui.theme.AccentGreen
import com.beatrunner.ui.theme.BackgroundDark
import com.beatrunner.ui.theme.ErrorColor
import com.beatrunner.ui.theme.PrimaryBlue
import com.beatrunner.ui.theme.PrimaryPurple
import com.beatrunner.ui.theme.SuccessColor
import com.beatrunner.ui.theme.SurfaceDark
import com.beatrunner.ui.theme.SurfaceLight
import com.beatrunner.ui.theme.TextPrimary
import com.beatrunner.ui.theme.TextSecondary
import com.beatrunner.ui.theme.WarningColor
import com.beatrunner.viewmodel.CoachMessage
import com.beatrunner.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

/**
 * Main workout dashboard screen
 */
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val bluetoothState by viewModel.bluetoothState.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val audioVisualizationData by viewModel.audioVisualizationData.collectAsState()
    val workoutData by viewModel.workoutData.collectAsState()
    val coachMessages by viewModel.coachMessages.collectAsState()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    
    var showStopConfirmation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Check permissions when screen resumes (e.g. returning from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, SurfaceDark)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status bar
            ConnectionStatusBar(
                bluetoothState = bluetoothState,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main speed display
            SpeedDisplay(
                speed = workoutData?.speed ?: 0f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Incline display
            InclineDisplay(
                incline = workoutData?.incline ?: 0f,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Music info card
            currentSong?.let { song ->
                MusicInfoCard(
                    song = song,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Waveform visualizer
                WaveformVisualizer(
                    visualizationData = audioVisualizationData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Controls
            if (isWorkoutActive) {
                WorkoutControls(
                    isPaused = isPaused,
                    onPauseResume = {
                        scope.launch {
                            if (isPaused) {
                                viewModel.resumeWorkout()
                            } else {
                                viewModel.pauseWorkout()
                            }
                        }
                    },
                    onStop = { showStopConfirmation = true },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Coach messages
            CoachMessagesPanel(
                messages = coachMessages,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            )
        }
    }
    
    if (showStopConfirmation) {
        StopConfirmationDialog(
            onConfirm = {
                showStopConfirmation = false
                scope.launch {
                    viewModel.stopWorkout()
                }
            },
            onDismiss = { showStopConfirmation = false }
        )
    }
}

/**
 * Connection status indicator
 */
@Composable
fun ConnectionStatusBar(
    bluetoothState: BluetoothConnectionState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = when (bluetoothState) {
            is BluetoothConnectionState.Connected -> SuccessColor
            is BluetoothConnectionState.Connecting -> WarningColor
            is BluetoothConnectionState.Disconnected -> SurfaceDark
            else -> ErrorColor
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (bluetoothState) {
                    is BluetoothConnectionState.Connected -> "📶 已连接"
                    is BluetoothConnectionState.Connecting -> "⏳ 连接中..."
                    is BluetoothConnectionState.Disconnected -> "❌ 未连接"
                    else -> "⚠️ 错误"
                },
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
        }
    }
}

/**
 * Large speed display (main focus)
 */
@Composable
fun SpeedDisplay(
    speed: Float,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(targetValue = speed)
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%.1f".format(animatedSpeed),
            style = MaterialTheme.typography.displayLarge.copy(
                brush = Brush.linearGradient(
                    colors = listOf(PrimaryBlue, PrimaryPurple)
                )
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "km/h",
            style = MaterialTheme.typography.headlineMedium,
            color = TextSecondary
        )
    }
}

/**
 * Incline indicator
 */
@Composable
fun InclineDisplay(
    incline: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "坡度",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "%.1f%%".format(incline),
            style = MaterialTheme.typography.headlineSmall,
            color = AccentGreen
        )
    }
}

/**
 * Music information card
 */
@Composable
fun MusicInfoCard(
    song: SongInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryBlue, PrimaryPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎵",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
                )
                song.bpm?.let { bpm ->
                    Text(
                        text = "$bpm BPM",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentGreen
                    )
                }
            }
        }
    }
}

/**
 * Coach messages panel with bubbles
 */
@Composable
fun CoachMessagesPanel(
    messages: List<CoachMessage>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.reversed()) { message ->
                CoachMessageBubble(message)
            }
        }
    }
}

/**
 * Individual coach message bubble
 */
@Composable
fun CoachMessageBubble(
    message: CoachMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isAI) Arrangement.Start else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.isError -> ErrorColor.copy(alpha = 0.2f)
                    message.isAI -> PrimaryBlue.copy(alpha = 0.2f)
                    else -> SurfaceDark
                }
            )
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

/**
 * Workout control buttons (Pause/Resume, Stop)
 */
@Composable
fun WorkoutControls(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stop button
        FloatingActionButton(
            onClick = onStop,
            containerColor = ErrorColor,
            contentColor = Color.White,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop Workout",
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Pause/Resume button
        FloatingActionButton(
            onClick = onPauseResume,
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume Workout" else "Pause Workout",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

/**
 * Confirmation dialog for stopping workout
 */
@Composable
fun StopConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "结束训练？") },
        text = { Text(text = "确定要结束当前训练并保存数据吗？") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
            ) {
                Text("结束")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextPrimary)
            ) {
                Text("取消")
            }
        },
        containerColor = SurfaceLight,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

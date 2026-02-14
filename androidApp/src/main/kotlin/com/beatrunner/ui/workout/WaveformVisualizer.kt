package com.beatrunner.ui.workout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.beatrunner.domain.music.AudioVisualizationData
import com.beatrunner.ui.theme.PrimaryBlue
import com.beatrunner.ui.theme.PrimaryPurple
import com.beatrunner.ui.theme.SurfaceLight
import kotlin.math.abs

/**
 * Music waveform visualizer component
 * Displays real-time audio waveform bars based on FFT data
 */
@Composable
fun WaveformVisualizer(
    visualizationData: AudioVisualizationData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp)
        ) {
            if (visualizationData != null) {
                WaveformBars(visualizationData = visualizationData)
            } else {
                // Show placeholder when no data
                PlaceholderWaveform()
            }
        }
    }
}

/**
 * Renders animated waveform bars from FFT data
 */
@Composable
private fun WaveformBars(visualizationData: AudioVisualizationData) {
    // Use FFT data for frequency-based visualization
    val fftData = visualizationData.fft
    
    // Number of bars to display (sample the FFT data)
    val barCount = 32
    val sampledData = if (fftData.isNotEmpty()) {
        // Sample FFT data evenly across the spectrum
        val step = fftData.size / barCount
        (0 until barCount).map { i ->
            val index = (i * step).coerceIn(0, fftData.size - 1)
            // Convert byte to unsigned value (0-255) and normalize to 0-1
            (fftData[index].toInt() and 0xFF) / 255f
        }
    } else {
        List(barCount) { 0f }
    }
    
    // Animate each bar individually with spring animation
    val animatedHeights = sampledData.map { amplitude ->
        animateFloatAsState(
            targetValue = amplitude,
            animationSpec = tween(
                durationMillis = 100,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            ),
            label = "barHeight"
        ).value
    }
    
    Canvas(modifier = Modifier.fillMaxWidth().height(88.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / barCount * 0.7f
        val spacing = canvasWidth / barCount * 0.3f
        
        animatedHeights.forEachIndexed { index, amplitude ->
            // Calculate bar height (minimum 8dp for visibility)
            val barHeight = (amplitude * canvasHeight * 0.9f).coerceAtLeast(8f)
            
            // Calculate x position
            val x = index * (barWidth + spacing) + spacing / 2
            
            // Center the bar vertically
            val y = (canvasHeight - barHeight) / 2
            
            // Create gradient color based on position
            val colorProgress = index.toFloat() / barCount
            val barColor = androidx.compose.ui.graphics.lerp(
                PrimaryBlue,
                PrimaryPurple,
                colorProgress
            )
            
            // Draw rounded rectangle bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

/**
 * Placeholder waveform when no audio data is available
 */
@Composable
private fun PlaceholderWaveform() {
    val barCount = 32
    
    Canvas(modifier = Modifier.fillMaxWidth().height(88.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / barCount * 0.7f
        val spacing = canvasWidth / barCount * 0.3f
        
        repeat(barCount) { index ->
            // Static low bars for placeholder
            val barHeight = canvasHeight * 0.15f
            
            val x = index * (barWidth + spacing) + spacing / 2
            val y = (canvasHeight - barHeight) / 2
            
            val colorProgress = index.toFloat() / barCount
            val barColor = androidx.compose.ui.graphics.lerp(
                PrimaryBlue.copy(alpha = 0.3f),
                PrimaryPurple.copy(alpha = 0.3f),
                colorProgress
            )
            
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

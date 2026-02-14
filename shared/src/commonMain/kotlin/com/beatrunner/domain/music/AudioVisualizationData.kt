package com.beatrunner.domain.music

import com.beatrunner.util.currentTimeMillis
import kotlinx.serialization.Serializable

/**
 * Audio visualization data containing waveform and FFT information
 * Captured from the currently playing music using Android Visualizer API
 */
@Serializable
data class AudioVisualizationData(
    /**
     * Waveform data representing the audio signal amplitude over time
     * Values range from -128 to 127
     */
    val waveform: ByteArray,
    
    /**
     * FFT (Fast Fourier Transform) data representing frequency spectrum
     * Format: [magnitude1, magnitude2, ..., magnitudeN]
     * Values range from 0 to 255
     */
    val fft: ByteArray,
    
    /**
     * Capture rate in millihertz (mHz)
     * Indicates how frequently the data is updated
     */
    val captureRate: Int,
    
    /**
     * Timestamp when this data was captured
     */
    val timestamp: Long = currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AudioVisualizationData

        if (!waveform.contentEquals(other.waveform)) return false
        if (!fft.contentEquals(other.fft)) return false
        if (captureRate != other.captureRate) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = waveform.contentHashCode()
        result = 31 * result + fft.contentHashCode()
        result = 31 * result + captureRate
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

/**
 * Extension to get the dominant frequency from FFT data
 * @return Frequency bin index with the highest magnitude, or -1 if data is empty
 */
fun AudioVisualizationData.getDominantFrequency(): Int {
    if (fft.isEmpty()) return -1
    var maxIndex = 0
    var maxValue = fft[0]
    
    for (i in 1 until fft.size) {
        if (fft[i] > maxValue) {
            maxValue = fft[i]
            maxIndex = i
        }
    }
    
    return maxIndex
}

/**
 * Extension to calculate average amplitude from waveform data
 * @return Average amplitude value (0-127)
 */
fun AudioVisualizationData.getAverageAmplitude(): Int {
    if (waveform.isEmpty()) return 0
    return waveform.map { kotlin.math.abs(it.toInt()) }.average().toInt()
}

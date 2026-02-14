package com.beatrunner.platform

import android.content.Context
import android.media.audiofx.Visualizer
import android.util.Log
import com.beatrunner.domain.music.AudioVisualizationData

/**
 * Manager for Android Visualizer API to capture real audio data
 * 
 * Attempts to capture system audio output using Visualizer(0).
 * Note: This may fail on Android 9+ due to privacy restrictions.
 */
class AudioVisualizerManager(private val context: Context) {
    
    private var visualizer: Visualizer? = null
    private var isCapturing = false
    
    companion object {
        private const val TAG = "AudioVisualizerManager"
        private const val AUDIO_SESSION_ID_SYSTEM = 0 // System audio output
    }
    
    /**
     * Start capturing audio visualization data
     * 
     * @param onData Callback invoked with audio data at capture rate
     * @return true if successfully started, false if failed (e.g., permission denied or API restricted)
     */
    fun startCapture(onData: (AudioVisualizationData) -> Unit): Boolean {
        if (isCapturing) {
            Log.w(TAG, "Already capturing, stopping previous session")
            stopCapture()
        }
        
        return try {
            // Try to create Visualizer with system audio session
            val viz = Visualizer(AUDIO_SESSION_ID_SYSTEM)
            
            // Configure capture size (use maximum available)
            val captureSize = Visualizer.getCaptureSizeRange()[1]
            viz.captureSize = captureSize
            
            // Set up data capture listener
            viz.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer,
                        waveform: ByteArray,
                        samplingRate: Int
                    ) {
                        // Waveform data captured - will be called at capture rate
                    }
                    
                    override fun onFftDataCapture(
                        visualizer: Visualizer,
                        fft: ByteArray,
                        samplingRate: Int
                    ) {
                        // FFT data captured - invoke callback
                        // Note: We need both waveform and FFT, so we'll capture both
                        // For now, we'll use the FFT callback as the primary trigger
                        
                        // Get waveform data as well
                        val waveform = ByteArray(captureSize)
                        val waveformResult = visualizer.getWaveForm(waveform)
                        
                        if (waveformResult == Visualizer.SUCCESS) {
                            onData(
                                AudioVisualizationData(
                                    waveform = waveform,
                                    fft = fft,
                                    captureRate = samplingRate
                                )
                            )
                        }
                    }
                },
                Visualizer.getMaxCaptureRate() / 2, // ~30 FPS for smooth animation
                true, // Capture waveform
                true  // Capture FFT
            )
            
            // Enable the visualizer
            viz.enabled = true
            
            visualizer = viz
            isCapturing = true
            
            Log.i(TAG, "Successfully started audio capture with Visualizer(0)")
            Log.i(TAG, "Capture size: $captureSize, Max rate: ${Visualizer.getMaxCaptureRate()}")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Visualizer(0): ${e.message}", e)
            Log.i(TAG, "This is expected on Android 9+ due to privacy restrictions")
            visualizer?.release()
            visualizer = null
            isCapturing = false
            false
        }
    }
    
    /**
     * Stop capturing audio data and release resources
     */
    fun stopCapture() {
        try {
            visualizer?.apply {
                enabled = false
                setDataCaptureListener(null, 0, false, false)
                release()
            }
            Log.i(TAG, "Stopped audio capture")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping visualizer: ${e.message}", e)
        } finally {
            visualizer = null
            isCapturing = false
        }
    }
    
    /**
     * Check if currently capturing audio data
     */
    fun isCapturing(): Boolean = isCapturing
    
    /**
     * Release all resources (call in onDestroy)
     */
    fun release() {
        stopCapture()
    }
}

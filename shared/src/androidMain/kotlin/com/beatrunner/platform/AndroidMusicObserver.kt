package com.beatrunner.platform

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.beatrunner.domain.music.AudioVisualizationData
import com.beatrunner.domain.music.MusicObserver
import com.beatrunner.domain.music.SongInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Android implementation of MusicObserver using NotificationListenerService
 * This service monitors media session changes from music apps and provides audio visualization
 * 
 * Audio Visualization Strategy:
 * 1. First attempts to use Visualizer(0) to capture real system audio output
 * 2. If that fails (Android 9+ restrictions), automatically falls back to mock visualization
 * 
 * Mock visualization generates synthetic FFT data based on song BPM for visual feedback.
 * Real visualization captures actual audio waveform and FFT data from system output.
 */
class AndroidMusicObserver(private val context: Context) : MusicObserver {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _currentSong = MutableStateFlow<SongInfo?>(null)
    private val _audioVisualization = MutableStateFlow<AudioVisualizationData?>(null)
    
    private val mediaSessionManager: MediaSessionManager? by lazy {
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    }

    private var updatesJob: Job? = null
    private var visualizationJob: Job? = null
    private var isVisualizationEnabled = false
    private var currentBpm: Int = 120 // Default BPM
    private var visualizationSeed: Long = System.currentTimeMillis()
    
    // Real audio visualizer (tries to capture system audio)
    private val audioVisualizer: AudioVisualizerManager by lazy {
        AudioVisualizerManager(context)
    }
    private var isUsingRealVisualizer = false

    override fun observeCurrentSong(): Flow<SongInfo?> = _currentSong.asStateFlow()
    
    override fun observeAudioVisualization(): Flow<AudioVisualizationData?> = _audioVisualization.asStateFlow()

    override suspend fun startListening() {
        if (!hasPermission()) {
            return
        }

        // Cancel existing job to avoid duplicates
        updatesJob?.cancel()

        // Listen for events from the service
        updatesJob =
                scope.launch { MusicListenerService.musicEvents.collect { updateCurrentSong() } }

        // Initial update
        updateCurrentSong()
    }

    override fun stopListening() {
        updatesJob?.cancel()
        updatesJob = null
        _currentSong.value = null
        audioVisualizer.stopCapture()
        isUsingRealVisualizer = false
        stopMockVisualization()
    }
    
    override fun setVisualizationEnabled(enabled: Boolean) {
        isVisualizationEnabled = enabled
        
        if (enabled && _currentSong.value != null) {
            // Try real audio capture first
            val realVisualizerStarted = audioVisualizer.startCapture { data ->
                // Update visualization data from real audio
                _audioVisualization.value = data
            }
            
            if (realVisualizerStarted) {
                // Successfully started real audio capture
                isUsingRealVisualizer = true
                // Stop mock visualization if it was running
                stopMockVisualization()
                android.util.Log.i("AndroidMusicObserver", "Using real audio visualization")
            } else {
                // Failed to start real visualizer, fall back to mock
                isUsingRealVisualizer = false
                startMockVisualization()
                android.util.Log.i("AndroidMusicObserver", "Falling back to mock visualization")
            }
        } else {
            // Disabled - stop both
            audioVisualizer.stopCapture()
            isUsingRealVisualizer = false
            stopMockVisualization()
        }
    }

    override fun hasPermission(): Boolean {
        val enabledListeners =
                Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val componentName = ComponentName(context, MusicListenerService::class.java)
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }
    
    override fun hasAudioPermission(): Boolean {
        // Not needed for mock visualization, but kept for interface compatibility
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission() {
        val intent =
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
        context.startActivity(intent)
    }
    
    override suspend fun requestAudioPermission() {
        // Not needed for mock visualization
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /** Update current song from active media controllers */
    fun updateCurrentSong() {
        try {
            val controllers =
                    mediaSessionManager?.getActiveSessions(
                            ComponentName(context, MusicListenerService::class.java)
                    )
                            ?: emptyList()

            val activeController = controllers.firstOrNull()
            val metadata = activeController?.metadata

            if (metadata != null) {
                val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown"
                val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown"

                val songInfo =
                        SongInfo(
                                id = "${activeController.packageName}-$title-$artist",
                                title = title,
                                artist = artist,
                                album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM),
                                duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION),
                                albumArtUri =
                                        metadata.getString(
                                                MediaMetadata.METADATA_KEY_ALBUM_ART_URI
                                        ),
                                packageName = activeController.packageName
                        )
                
                // Check if song changed
                val songChanged = _currentSong.value?.id != songInfo.id
                _currentSong.value = songInfo
                
                // Update BPM if available
                currentBpm = songInfo.bpm ?: 120
                
                // If song changed, generate new visualization seed for variety
                if (songChanged) {
                    visualizationSeed = System.currentTimeMillis()
                }
                
                // Restart visualization if enabled (applies to both real and mock)
                if (isVisualizationEnabled) {
                    if (isUsingRealVisualizer) {
                        // Real visualizer continues automatically, no restart needed
                    } else {
                        // Restart mock visualization with new seed
                        startMockVisualization()
                    }
                }
            } else {
                _currentSong.value = null
                audioVisualizer.stopCapture()
                isUsingRealVisualizer = false
                stopMockVisualization()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _currentSong.value = null
            audioVisualizer.stopCapture()
            isUsingRealVisualizer = false
            stopMockVisualization()
        }
    }
    
    /**
     * Start generating mock audio visualization data
     * Generates synthetic FFT data based on BPM and random variation
     */
    private fun startMockVisualization() {
        // Stop existing job
        visualizationJob?.cancel()
        
        visualizationJob = scope.launch(Dispatchers.Default) {
            val fftSize = 1024 // Standard FFT size
            val random = Random(visualizationSeed)
            var frame = 0L
            
            // Target ~30 FPS for smooth animation
            val frameDelayMs = 33L
            
            // BPM affects the "pulse" speed of the visualization
            val beatsPerSecond = currentBpm / 60.0
            val framesPerBeat = (1000.0 / frameDelayMs) / beatsPerSecond
            
            while (true) {
                // Generate synthetic FFT data
                val fft = ByteArray(fftSize)
                val waveform = ByteArray(fftSize)
                
                // Calculate beat phase (0.0 to 1.0)
                val beatPhase = (frame % framesPerBeat) / framesPerBeat
                
                // Create a "pulse" effect synchronized with BPM
                val beatIntensity = (1.0 - abs(beatPhase - 0.5) * 2.0) * 0.5 + 0.5
                
                for (i in fft.indices) {
                    // Frequency-dependent amplitude (lower frequencies stronger)
                    val frequencyFactor = 1.0 - (i.toDouble() / fft.size) * 0.7
                    
                    // Add some randomness for natural variation
                    val randomFactor = random.nextDouble(0.7, 1.3)
                    
                    // Combine beat intensity, frequency response, and randomness
                    val amplitude = (beatIntensity * frequencyFactor * randomFactor * 255).toInt()
                    
                    // Clamp to byte range
                    fft[i] = amplitude.coerceIn(0, 255).toByte()
                    
                    // Generate waveform data (simpler sine wave pattern)
                    val wavePhase = (frame + i) * 0.1
                    waveform[i] = ((sin(wavePhase) * 127 * beatIntensity).toInt() + 128).toByte()
                }
                
                _audioVisualization.value = AudioVisualizationData(
                    waveform = waveform,
                    fft = fft,
                    captureRate = (1000 / frameDelayMs).toInt() // Approximate capture rate
                )
                
                frame++
                delay(frameDelayMs)
            }
        }
    }
    
    /**
     * Stop generating mock visualization data
     */
    private fun stopMockVisualization() {
        visualizationJob?.cancel()
        visualizationJob = null
        _audioVisualization.value = null
    }
}


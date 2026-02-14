package com.beatrunner.domain.music

import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform interface for monitoring music playback
 * Platform-specific implementations:
 * - Android: NotificationListenerService + Visualizer API
 * - iOS: MPNowPlayingInfoCenter
 */
interface MusicObserver {
    /**
     * Start listening to music playback changes
     * @return Flow emitting SongInfo whenever the playing track changes
     */
    fun observeCurrentSong(): Flow<SongInfo?>
    
    /**
     * Observe audio visualization data (waveform and FFT)
     * @return Flow emitting AudioVisualizationData when available
     * Note: Requires audio permission on Android
     */
    fun observeAudioVisualization(): Flow<AudioVisualizationData?>
    
    /**
     * Start the music monitoring service
     */
    suspend fun startListening()
    
    /**
     * Stop the music monitoring service
     */
    fun stopListening()
    
    /**
     * Enable or disable audio visualization
     * @param enabled true to enable visualization, false to disable
     */
    fun setVisualizationEnabled(enabled: Boolean)
    
    /**
     * Check if the observer has required permissions
     * - Android: Notification access permission
     * - iOS: Media library permission
     */
    fun hasPermission(): Boolean
    
    /**
     * Check if audio recording permission is granted (for visualization)
     * - Android: RECORD_AUDIO permission
     * - iOS: Microphone permission (not typically needed for visualization)
     */
    fun hasAudioPermission(): Boolean
    
    /**
     * Request necessary permissions (platform-specific)
     */
    suspend fun requestPermission()
    
    /**
     * Request audio recording permission for visualization
     */
    suspend fun requestAudioPermission()
}


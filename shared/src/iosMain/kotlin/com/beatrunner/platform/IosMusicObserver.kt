package com.beatrunner.platform

import com.beatrunner.domain.music.MusicObserver
import com.beatrunner.domain.music.SongInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyAlbumArtist
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMusicPlayerController
import platform.MediaPlayer.MPNowPlayingInfoCenter

/**
 * iOS implementation of MusicObserver using MPNowPlayingInfoCenter
 * Note: This monitors the system's now playing info, which is updated by music apps
 */
class IosMusicObserver : MusicObserver {
    
    private val _currentSong = MutableStateFlow<SongInfo?>(null)
    private val nowPlayingCenter = MPNowPlayingInfoCenter.defaultCenter()
    
    override fun observeCurrentSong(): Flow<SongInfo?> = _currentSong.asStateFlow()
    
    override suspend fun startListening() {
        // Subscribe to now playing info changes
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = "MPMusicPlayerControllerNowPlayingItemDidChangeNotification",
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            updateCurrentSong()
        }
        
        // Initial update
        updateCurrentSong()
    }
    
    override fun stopListening() {
        NSNotificationCenter.defaultCenter.removeObserver(this)
        _currentSong.value = null
    }
    
    override fun hasPermission(): Boolean {
        // iOS doesn't require explicit permission for MPNowPlayingInfoCenter
        // But we might need Media Library permission for some features
        return true
    }
    
    override suspend fun requestPermission() {
        // No explicit permission needed for basic now playing info
        // For full media library access, we would use MPMediaLibrary.requestAuthorization()
    }
    
    private fun updateCurrentSong() {
        val nowPlayingInfo = nowPlayingCenter.nowPlayingInfo
        
        if (nowPlayingInfo != null && nowPlayingInfo.isNotEmpty()) {
            val title = nowPlayingInfo[MPMediaItemPropertyTitle] as? String
            val artist = nowPlayingInfo[MPMediaItemPropertyArtist] as? String
            val album = nowPlayingInfo[MPMediaItemPropertyAlbumTitle] as? String
            val duration = (nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] as? Double)?.toLong()?.times(1000)
            
            if (title != null && artist != null) {
                val songInfo = SongInfo(
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    albumArtUrl = null, // Artwork is available but requires conversion
                    packageName = null, // Not available on iOS
                    bpm = null
                )
                _currentSong.value = songInfo
                return
            }
        }
        
        _currentSong.value = null
    }
}

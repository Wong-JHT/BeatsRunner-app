package com.beatrunner.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * NotificationListenerService implementation for monitoring music apps.
 * This service must be registered in AndroidManifest.xml within the application tag.
 */
class MusicListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "MusicListenerService"
        
        // Using a SharedFlow to broadcast events to observers (like AndroidMusicObserver)
        private val _musicEvents = MutableSharedFlow<Unit>(replay = 1)
        val musicEvents: SharedFlow<Unit> = _musicEvents.asSharedFlow()

        // Helper to trigger an update manually if needed
        fun requestUpdate() {
            _musicEvents.tryEmit(Unit)
        }
    }

    private val mediaSessionManager: MediaSessionManager by lazy {
        getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    }

    private val callback = object : MediaSessionManager.OnActiveSessionsChangedListener {
        override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
            // Notify observer of changes
            _musicEvents.tryEmit(Unit)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Ensure we try to register the listener when service starts
        try {
            val componentName = ComponentName(this, MusicListenerService::class.java)
            mediaSessionManager.addOnActiveSessionsChangedListener(callback, componentName)
            _musicEvents.tryEmit(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            val componentName = ComponentName(this, MusicListenerService::class.java)
            mediaSessionManager.addOnActiveSessionsChangedListener(callback, componentName)
            _musicEvents.tryEmit(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            mediaSessionManager.removeOnActiveSessionsChangedListener(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        _musicEvents.tryEmit(Unit)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        _musicEvents.tryEmit(Unit)
    }
}

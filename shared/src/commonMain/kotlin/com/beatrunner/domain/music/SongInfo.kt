package com.beatrunner.domain.music

import com.beatrunner.util.currentTimeMillis
import kotlinx.serialization.Serializable

@Serializable
data class SongInfo(
        val id: String = "",
        val title: String,
        val artist: String,
        val album: String? = null,
        val duration: Long, // milliseconds
        val albumArtUri: String? = null,
        val packageName: String? = null,
        val isPlaying: Boolean = false,
        val progress: Long = 0, // milliseconds
        val bpm: Int? = null,
        val lastUpdated: Long = currentTimeMillis()
)

/** Extension to check if song information is valid for AI analysis */
fun SongInfo.isValid(): Boolean = title.isNotEmpty() && artist.isNotEmpty()

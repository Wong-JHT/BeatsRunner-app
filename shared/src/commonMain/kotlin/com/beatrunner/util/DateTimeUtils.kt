package com.beatrunner.util

import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateTimeUtils {
    @OptIn(ExperimentalTime::class)
    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    @OptIn(ExperimentalTime::class)
    fun formatWorkoutTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val nowMs = currentTimeMillis()
        val durationMs = nowMs - timestamp

        return when {
            durationMs < 60000 -> "Just now"
            durationMs < 3600000 -> "${durationMs / 60000}m ago"
            durationMs < 86400000 -> "${durationMs / 3600000}h ago"
            else -> {
                val daysDiff = durationMs / 86400000
                if (daysDiff < 7) "${daysDiff}d ago" else "More than a week ago"
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun formatDate(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
    }

    @OptIn(ExperimentalTime::class)
    fun formatDateTime(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    }

    fun formatDuration(seconds: Int): String {
        return formatDuration(seconds * 1000L)
    }

    fun formatDistance(km: Double): String {
        return if (km != null) "${(km * 100).toInt() / 100.0} km" else "-- km"
    }

    fun formatSpeed(speed: Double): String {
        return if (speed != null) "${(speed * 10).toInt() / 10.0} km/h" else "-- km/h"
    }

    fun formatIncline(incline: Double): String {
        return if (incline != null) "${(incline * 10).toInt() / 10.0}%" else "-- %"
    }

    fun formatHeartRate(hr: Int?): String {
        return if (hr != null) "$hr bpm" else "-- bpm"
    }

    fun formatCalories(cal: Double): String {
        return if (cal != null) "$cal kcal" else "-- kcal"
    }
}

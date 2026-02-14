package com.beatrunner.data.network

import com.beatrunner.domain.bluetooth.BluetoothModels
import com.beatrunner.domain.music.SongInfo
import kotlinx.serialization.Serializable

/**
 * Request to analyze song and get BPM
 */
@Serializable
data class SongAnalysisRequest(
    val songInfo: SongInfo
)

/**
 * Response containing song analysis including BPM
 */
@Serializable
data class SongAnalysisResponse(
    val bpm: Int,
    val energy: Float? = null, // 0.0 - 1.0
    val mood: String? = null
)

/**
 * Request for AI coaching command
 */
@Serializable
data class AICommandRequest(
    val songInfo: SongInfo,
    val currentSpeed: Float,
    val currentIncline: Float,
    val heartRate: Int? = null,
    val userProfile: UserProfile
)

/**
 * User profile for personalized coaching
 */
@Serializable
data class UserProfile(
    val age: Int,
    val weight: Float, // kg
    val height: Float, // cm
    val fitnessLevel: FitnessLevel,
    val targetHeartRate: Int? = null
)

@Serializable
enum class FitnessLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * AI coaching response
 */
@Serializable
data class AICommandResponse(
    val command: BluetoothModels.TreadmillCommand,
    val coachMessage: String,
    val reasoning: String? = null
)

/**
 * API error response
 */
@Serializable
data class ApiError(
    val code: String,
    val message: String
)

/**
 * Paginated response for workout sessions
 */
@Serializable
data class WorkoutSessionsResponse(
    val data: List<WorkoutSessionDetail>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)

/**
 * Detailed workout session data
 */
@Serializable
data class WorkoutSessionDetail(
    val id: String,
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val distanceMeters: Double,
    val caloriesBurned: Double,
    val avgSpeed: Double,
    val maxSpeed: Double,
    val avgIncline: Double,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val musics: List<WorkoutMusicData> = emptyList(),
    val points: List<WorkoutDataPoint> = emptyList()
)

/**
 * Request to create a workout session.
 */
@Serializable
data class WorkoutSessionRequest(
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val distanceMeters: Double,
    val caloriesBurned: Double,
    val avgSpeed: Double,
    val maxSpeed: Double,
    val avgIncline: Double,
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val musics: List<WorkoutMusicData> = emptyList(),
    val points: List<WorkoutDataPoint> = emptyList()
)

/**
 * Per-second workout data point.
 */
@Serializable
data class WorkoutDataPoint(
    val offsetSeconds: Int,
    val speed: Double,
    val incline: Double,
    val heartRate: Int? = null
)

/**
 * Music data in workout request.
 */
@Serializable
data class WorkoutMusicData(
    val title: String,
    val artist: String,
    val bpm: Int,
    val genre: String,
    val playedAt: String,
    val durationSeconds: Int
)



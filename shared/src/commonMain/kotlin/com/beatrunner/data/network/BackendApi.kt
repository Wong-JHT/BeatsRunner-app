package com.beatrunner.data.network

import com.beatrunner.domain.music.SongInfo
import kotlinx.coroutines.flow.Flow

/** Backend API interface for BeatRunner service Implementation uses Ktor Client */
interface BackendApi {

        /**
         * Analyze music to get AI treadmill settings and coach message
         * @param songInfo The song to analyze
         * @param acceptLanguage Target language for AI coach
         * @return AI generated settings and message
         */
        suspend fun analyzeSong(
                songInfo: SongInfo,
                acceptLanguage: String = "zh-CN"
        ): Result<AIAnalyzeResponse>

        /**
         * Start a new workout session
         * @param request The start workout request
         * @return The created session detail
         */
        suspend fun startWorkoutSession(request: StartWorkoutRequest): Result<WorkoutSessionDetail>

        /** Connect to real-time coach via WebSocket */
        suspend fun connectCoachWebSocket(): Flow<AIAnalyzeResponse>

        /** Send current music state to coach via WebSocket */
        suspend fun sendMusicStateToCoach(state: WsMusicState): Result<Unit>

        /** Disconnect coach WebSocket */
        suspend fun disconnectCoachWebSocket()

        /**
         * Finish a workout session with full data
         * @param id The session UUID
         * @param request Workout data including points and music
         * @return The finished session detail
         */
        suspend fun finishWorkoutSession(
                id: String,
                request: WorkoutSessionRequest
        ): Result<WorkoutSessionDetail>

        /**
         * Register new user account
         * @param request Registration details
         * @return Authentication response with JWT token
         */
        suspend fun register(request: RegisterRequest): Result<AuthResponse>

        /**
         * Login user
         * @param request Login credentials
         * @return Authentication response with JWT token
         */
        suspend fun login(request: LoginRequest): Result<AuthResponse>

        /** Delete user account (requires authentication) Cascading delete of all user data */
        suspend fun deleteAccount(): Result<Unit>

        /**
         * Get current user profile
         * @return User profile data
         */
        suspend fun getUserProfile(): Result<UserProfileData>

        /**
         * Update user profile
         * @param request Profile update fields
         * @return Updated user profile
         */
        suspend fun updateUserProfile(request: UpdateProfileRequest): Result<UserProfileData>

        /**
         * Get workout sessions list with pagination
         * @param page Page number (1-indexed)
         * @param size Number of items per page
         * @return Paginated list of workout sessions
         */
        suspend fun getWorkoutSessions(
                page: Int = 1,
                size: Int = 10
        ): Result<WorkoutSessionsResponse>
        /** Get workout session detail by ID */
        suspend fun getWorkoutSessionDetail(id: String): Result<WorkoutSessionDetail>

        /** Close resources */
        // fun close() // Removed as it's not part of the core API logic usually, but kept in impl
}

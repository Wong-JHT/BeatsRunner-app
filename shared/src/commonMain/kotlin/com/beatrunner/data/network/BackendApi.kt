package com.beatrunner.data.network

import com.beatrunner.domain.bluetooth.BluetoothModels
import com.beatrunner.domain.music.SongInfo

/**
 * Backend API interface for BeatRunner service
 * Implementation uses Ktor Client
 */
interface BackendApi {
    
    /**
     * Analyze song to extract BPM and other metrics
     * @param songInfo The song to analyze
     * @return Analysis results including BPM
     */
    suspend fun analyzeSong(songInfo: SongInfo): Result<SongAnalysisResponse>
    
    /**
     * Get AI coaching command based on current context
     * @param songInfo Current playing song
     * @param currentSpeed Current treadmill speed
     * @param currentIncline Current treadmill incline
     * @param heartRate Optional heart rate
     * @param userProfile User's fitness profile
     * @return AI-generated treadmill command and coach message
     */
    suspend fun getAICommand(
        songInfo: SongInfo,
        currentSpeed: Float,
        currentIncline: Float,
        heartRate: Int?,
        userProfile: UserProfile
    ): Result<AICommandResponse>
    
    /**
     * Create a workout session with full data
     * @param request Workout data including points and music
     * @return Session ID
     */
    suspend fun createWorkoutSession(request: WorkoutSessionRequest): Result<String>

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

    /**
     * Delete user account (requires authentication)
     * Cascading delete of all user data
     */
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
     * @param page Page number (0-indexed)
     * @param pageSize Number of items per page
     * @return Paginated list of workout sessions
     */
    suspend fun getWorkoutSessions(page: Int = 0, pageSize: Int = 10): Result<WorkoutSessionsResponse>
    /**
     * Get workout session detail by ID
     */
    suspend fun getWorkoutSessionDetail(id: String): Result<WorkoutSessionDetail>
    
    /**
     * Close resources
     */
    // fun close() // Removed as it's not part of the core API logic usually, but kept in impl
}

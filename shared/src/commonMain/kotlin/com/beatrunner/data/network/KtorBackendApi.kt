package com.beatrunner.data.network

import com.beatrunner.domain.music.SongInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor-based implementation of BackendApi
 */
class KtorBackendApi(
    private val baseUrl: String = "https://api.beatrunner.com", // TODO: Replace with actual backend URL
    private val tokenManager: com.beatrunner.data.local.TokenManager
) : BackendApi {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
            })
        }
        
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("HTTP Client: $message")
                }
            }
            level = LogLevel.ALL
        }
        
        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            tokenManager.getToken()?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }
    
    override suspend fun analyzeSong(songInfo: SongInfo): Result<SongAnalysisResponse> {
        return try {
            val response = client.post("/ai/analyze") {
                setBody(SongAnalysisRequest(songInfo))
            }.body<SongAnalysisResponse>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAICommand(
        songInfo: SongInfo,
        currentSpeed: Float,
        currentIncline: Float,
        heartRate: Int?,
        userProfile: UserProfile
    ): Result<AICommandResponse> {
        return try {
            val request = AICommandRequest(
                songInfo = songInfo,
                currentSpeed = currentSpeed,
                currentIncline = currentIncline,
                heartRate = heartRate,
                userProfile = userProfile
            )
            
            val response = client.post("/api/v1/ai-command") {
                setBody(request)
            }.body<AICommandResponse>()
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createWorkoutSession(request: WorkoutSessionRequest): Result<String> {
        return try {
            val response =
                client
                    .post("/workout/session") { setBody(request) }
                    .body<Map<String, String>>()

            Result.success(response["sessionId"] ?: throw Exception("No session ID returned"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = client.post("/auth/register") {
                setBody(request)
            }.body<AuthResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = client.post("/auth/login") {
                setBody(request)
            }.body<AuthResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            client.delete("/auth/account")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserProfile(): Result<UserProfileData> {
        return try {
            val response = client.get("/user/profile").body<UserProfileData>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserProfile(request: UpdateProfileRequest): Result<UserProfileData> {
        return try {
            val response = client.put("/user/profile") {
                setBody(request)
            }.body<UserProfileData>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWorkoutSessions(page: Int, pageSize: Int): Result<WorkoutSessionsResponse> {
        return try {
            val response = client.get("/workout/sessions") {
                url {
                    parameters.append("page", page.toString())
                    parameters.append("pageSize", pageSize.toString())
                }
            }
            
            if (response.status.value in 200..299) {
                val sessionsResponse = response.body<WorkoutSessionsResponse>()
                Result.success(sessionsResponse)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Failed to get workout sessions: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkoutSessionDetail(id: String): Result<WorkoutSessionDetail> {
        return try {
            val response = client.get("/workout/session/$id").body<WorkoutSessionDetail>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    
    fun close() {
        client.close()
    }
}

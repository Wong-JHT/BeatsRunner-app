package com.beatrunner.data.network

import com.beatrunner.domain.music.SongInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Ktor-based implementation of BackendApi */
class KtorBackendApi(
        private val baseUrl: String =
                "https://api.beatrunner.com", // TODO: Replace with actual backend URL
        private val tokenManager: com.beatrunner.data.local.TokenManager
) : BackendApi {

    private var coachWsSession: DefaultClientWebSocketSession? = null

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                        explicitNulls = false
                    }
            )
        }

        install(Logging) {
            logger =
                    object : Logger {
                        override fun log(message: String) {
                            println("HTTP Client: $message")
                        }
                    }
            level = LogLevel.ALL
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            tokenManager.getToken()?.let { token -> header("Authorization", "Bearer $token") }
        }

        install(WebSockets) { pingIntervalMillis = 20_000 }
    }

    override suspend fun analyzeSong(
            songInfo: SongInfo,
            acceptLanguage: String
    ): Result<AIAnalyzeResponse> {
        return try {
            val response =
                    client
                            .post("/ai/analyze") {
                                header("Accept-Language", acceptLanguage)
                                setBody(AIAnalyzeRequest(songInfo))
                            }
                            .body<AIAnalyzeResponse>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun startWorkoutSession(
            request: StartWorkoutRequest
    ): Result<WorkoutSessionDetail> {
        return try {
            val response =
                    client.post("/workout/start") { setBody(request) }.body<WorkoutSessionDetail>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun finishWorkoutSession(
            id: String,
            request: WorkoutSessionRequest
    ): Result<WorkoutSessionDetail> {
        return try {
            val response =
                    client
                            .post("/workout/session/$id/finish") { setBody(request) }
                            .body<WorkoutSessionDetail>()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun connectCoachWebSocket(): Flow<AIAnalyzeResponse> = flow {
        val token = tokenManager.getToken() ?: throw Exception("No auth token")

        try {
            val wsUrl = baseUrl.replace("http", "ws") + "/ws/coach?token=$token"

            coachWsSession = client.webSocketSession { url(wsUrl) }

            coachWsSession
                    ?.incoming
                    ?.receiveAsFlow()
                    ?.mapNotNull<Frame, AIAnalyzeResponse> { frame ->
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            try {
                                val json = Json { ignoreUnknownKeys = true }
                                json.decodeFromString<AIAnalyzeResponse>(text)
                            } catch (e: Exception) {
                                null as AIAnalyzeResponse?
                            }
                        } else null as AIAnalyzeResponse?
                    }
                    ?.collect { emit(it) }
        } catch (e: Exception) {
            println("WebSocket connection failed: ${e.message}")
        } finally {
            coachWsSession?.close()
            coachWsSession = null
        }
    }

    override suspend fun sendMusicStateToCoach(state: WsMusicState): Result<Unit> {
        return try {
            val session = coachWsSession ?: throw Exception("WebSocket not connected")
            val jsonStr = Json.encodeToString(state)
            session.send(Frame.Text(jsonStr))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disconnectCoachWebSocket() {
        try {
            coachWsSession?.close()
        } catch (e: Exception) {
            // ignore
        } finally {
            coachWsSession = null
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = client.post("/auth/register") { setBody(request) }.body<AuthResponse>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = client.post("/auth/login") { setBody(request) }.body<AuthResponse>()
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
            val response = client.put("/user/profile") { setBody(request) }.body<UserProfileData>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkoutSessions(
            page: Int,
            size: Int
    ): Result<WorkoutSessionsResponse> {
        return try {
            val response =
                    client.get("/workout/sessions") {
                        url {
                            parameters.append("page", page.toString())
                            parameters.append("size", size.toString())
                        }
                    }

            if (response.status.value in 200..299) {
                val sessionsResponse = response.body<WorkoutSessionsResponse>()
                Result.success(sessionsResponse)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(
                        Exception("Failed to get workout sessions: ${response.status} - $errorBody")
                )
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

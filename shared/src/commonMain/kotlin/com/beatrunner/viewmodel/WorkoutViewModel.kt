package com.beatrunner.viewmodel

import com.beatrunner.data.network.BackendApi
import com.beatrunner.data.network.StartWorkoutRequest
import com.beatrunner.data.network.WorkoutDataPoint
import com.beatrunner.data.network.WorkoutMusicData
import com.beatrunner.data.network.WorkoutSessionRequest
import com.beatrunner.domain.bluetooth.BluetoothConnectionState
import com.beatrunner.domain.bluetooth.BluetoothModels
import com.beatrunner.domain.bluetooth.FtmsManager
import com.beatrunner.domain.music.AudioVisualizationData
import com.beatrunner.domain.music.MusicObserver
import com.beatrunner.domain.music.SongInfo
import com.beatrunner.domain.music.isValid
import com.beatrunner.util.currentTimeMillis
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/** ViewModel for workout screen Coordinates music monitoring, AI coaching, and treadmill control */
@OptIn(ExperimentalTime::class)
class WorkoutViewModel(
        private val musicObserver: MusicObserver,
        private val ftmsManager: FtmsManager,
        private val backendApi: BackendApi,
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    // Connection states
    private val _bluetoothState =
            MutableStateFlow<BluetoothConnectionState>(
                    BluetoothConnectionState.Disconnected("Not connected")
            )
    val bluetoothState: StateFlow<BluetoothConnectionState> = _bluetoothState.asStateFlow()

    val connectedDevice: StateFlow<BluetoothModels.BluetoothDevice?> = ftmsManager.connectedDevice

    private val _musicPermissionGranted = MutableStateFlow(false)
    val musicPermissionGranted: StateFlow<Boolean> = _musicPermissionGranted.asStateFlow()

    // Real-time data
    private val _currentSong = MutableStateFlow<SongInfo?>(null)
    val currentSong: StateFlow<SongInfo?> = _currentSong.asStateFlow()

    private val _audioVisualizationData = MutableStateFlow<AudioVisualizationData?>(null)
    val audioVisualizationData: StateFlow<AudioVisualizationData?> =
            _audioVisualizationData.asStateFlow()

    private val _workoutData = MutableStateFlow<BluetoothModels.WorkoutData?>(null)
    val workoutData: StateFlow<BluetoothModels.WorkoutData?> = _workoutData.asStateFlow()

    private val _coachMessages = MutableStateFlow<List<CoachMessage>>(emptyList())
    val coachMessages: StateFlow<List<CoachMessage>> = _coachMessages.asStateFlow()

    // Workout state
    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _sessionId = MutableStateFlow<String?>(null)

    // Workout statistics
    private var workoutStartTime: Long = 0
    private var totalPausedDuration: Long = 0
    private var pauseStartTime: Long = 0
    private var totalDistance: Float = 0f
    private var maxSpeed: Float = 0f
    private var totalSpeedSamples: Float = 0f
    private var speedSampleCount: Int = 0
    private var totalInclineSamples: Float = 0f
    private var inclineSampleCount: Int = 0
    private var maxHeartRate: Int? = null
    private var totalHeartRateSamples: Int = 0
    private var heartRateSampleCount: Int = 0
    private val songsPlayed = mutableListOf<SongInfo>()
    private val workoutDataPoints = mutableListOf<WorkoutDataPoint>()
    private val workoutMusics = mutableListOf<WorkoutMusicData>()
    private var lastUploadedSong: SongInfo? = null

    // User profile weight for calories calculation (TODO: load from preferences/database)
    private val userWeightKg = 70f

    private var aiUpdateJob: Job? = null
    private var wsJob: Job? = null

    init {
        observeConnections()
        observeMusicChanges()
        observeWorkoutStatistics()

        // Start listening immediately if permission is granted
        checkPermissions()
    }

    /** Check permissions and start listening if granted */
    fun checkPermissions() {
        scope.launch {
            _musicPermissionGranted.value = musicObserver.hasPermission()
            if (_musicPermissionGranted.value) {
                musicObserver.startListening()
            }
        }
    }

    /** Observe bluetooth and music states */
    private fun observeConnections() {
        scope.launch {
            ftmsManager.connectionState.collect { state -> _bluetoothState.value = state }
        }

        scope.launch { ftmsManager.workoutData.collect { data -> _workoutData.value = data } }

        _musicPermissionGranted.value = musicObserver.hasPermission()
    }

    /** Observe workout statistics from treadmill data */
    private fun observeWorkoutStatistics() {
        scope.launch {
            ftmsManager.workoutData.filterNotNull().collect { data ->
                if (_isWorkoutActive.value && !_isPaused.value) {
                    // Update stats
                    val speed = data.speed ?: 0f
                    speedSampleCount++
                    totalSpeedSamples += speed
                    if (speed > maxSpeed) {
                        maxSpeed = speed
                    }

                    val incline = data.incline ?: 0f
                    inclineSampleCount++
                    totalInclineSamples += incline

                    if (data.heartRate != null) {
                        heartRateSampleCount++
                        totalHeartRateSamples += data.heartRate!!
                        if (maxHeartRate == null || data.heartRate!! > maxHeartRate!!) {
                            maxHeartRate = data.heartRate
                        }
                    }

                    // Simple distance estimation if not provided (speed * time)
                    // Assuming updates every second (1s = 1/3600 h)
                    // distance += speed (km/h) / 3600
                    if (data.distance != null) {
                        // Use provided distance if available, or delta?
                        // `totalDistance` is an accumulator. `data.distance` might be total from
                        // device.
                        // If device provides total distance, use it. But we support various
                        // devices.
                        // Let's stick to our accumulator for consistency unless device guarantees
                        // it.
                        // For now, let's keep using our estimation or use delta if available.
                        // To keep it simple and consistent with previous logic:
                        totalDistance += speed / 3600f
                    } else {
                        totalDistance += speed / 3600f
                    }

                    // Record data point
                    val offsetSeconds = ((currentTimeMillis() - workoutStartTime) / 1000).toInt()
                    workoutDataPoints.add(
                            WorkoutDataPoint(
                                    offsetSeconds = offsetSeconds,
                                    speed = speed.toDouble(),
                                    incline = incline.toDouble(),
                                    heartRate = data.heartRate
                            )
                    )
                }
            }
        }
    }

    /** Observe music changes and trigger AI updates */
    private fun observeMusicChanges() {
        scope.launch {
            musicObserver.observeCurrentSong().filterNotNull().collect { song ->
                // Only process music changes during active workout
                if (_isWorkoutActive.value && song.isValid()) {
                    handleNewSong(song)
                }
            }
        }

        // Observe audio visualization data
        scope.launch {
            musicObserver.observeAudioVisualization().collect { data ->
                _audioVisualizationData.value = data
            }
        }
    }

    /** Handle new song detection */
    private suspend fun handleNewSong(song: SongInfo) {
        // Check if this is actually a new song (different from last uploaded)
        val isSongChanged =
                lastUploadedSong == null ||
                        lastUploadedSong?.title != song.title ||
                        lastUploadedSong?.artist != song.artist

        // Only process if song has changed
        if (!isSongChanged) {
            return
        }

        _currentSong.value = song

        // Track song if workout is active
        if (_isWorkoutActive.value && !songsPlayed.contains(song)) {
            songsPlayed.add(song)
        }

        // Analyze song if BPM is missing (only during active workout)
        if (_isWorkoutActive.value && !_isPaused.value) {
            addCoachMessage("🎵 检测到新歌曲: ${song.title} (${song.bpm ?: "?"} BPM)")

            // Upload music data
            uploadMusicData(song)

            // Notify AI coach via WebSocket
            scope.launch {
                backendApi.sendMusicStateToCoach(
                        com.beatrunner.data.network.WsMusicState(
                                title = song.title,
                                artist = song.artist,
                                bpm = song.bpm ?: 0 // Or null if your API allows
                        )
                )
            }
        }
    }

    /** Upload music data to backend (only called during active workout when song changes) */
    private fun uploadMusicData(song: SongInfo) {
        val durationSeconds = (song.duration / 1000).toInt()
        val playedAt =
                kotlinx.datetime.Instant.fromEpochMilliseconds(currentTimeMillis()).toString()
        workoutMusics.add(
                WorkoutMusicData(
                        title = song.title,
                        artist = song.artist,
                        bpm = song.bpm ?: 0,
                        genre = "Unknown", // Genre is not available in SongInfo yet
                        playedAt = playedAt,
                        durationSeconds = durationSeconds
                )
        )

        // Update last uploaded song
        lastUploadedSong = song
    }



    /** Start workout session */
    suspend fun startWorkout() {
        try {
            // Start music monitoring
            if (_musicPermissionGranted.value) {
                musicObserver.startListening()
                // Enable audio visualization
                musicObserver.setVisualizationEnabled(true)
            } else {
                musicObserver.requestPermission()
            }

            // Start treadmill
            delay(1000) // Wait for connection to stabilize
            ftmsManager.start()

            _isWorkoutActive.value = true
            workoutStartTime = currentTimeMillis()
            resetWorkoutStatistics()

            val startTimeIso =
                    kotlinx.datetime.Instant.fromEpochMilliseconds(workoutStartTime).toString()
            val result = backendApi.startWorkoutSession(StartWorkoutRequest(startTimeIso))
            result
                    .onSuccess { sessionDetail ->
                        _sessionId.value = sessionDetail.id
                        addCoachMessage("🏃 训练开始！跟随音乐节奏，享受运动吧！(Session: ${sessionDetail.id})")
                    }
                    .onFailure { e -> addCoachMessage("❌ 启动记录失败: ${e.message}", isError = true) }

            // Connect to real-time coach via WebSocket
            startWebSocketCoach()
        } catch (e: Exception) {
            addCoachMessage("❌ 启动失败: ${e.message}", isError = true)
        }
    }

    /** Pause workout session */
    suspend fun pauseWorkout() {
        if (!_isWorkoutActive.value || _isPaused.value) return

        _isPaused.value = true
        pauseStartTime = currentTimeMillis()

        // Pause treadmill
        ftmsManager.pause()

        addCoachMessage("⏸️ 训练已暂停")
    }

    /** Resume workout session */
    suspend fun resumeWorkout() {
        if (!_isWorkoutActive.value || !_isPaused.value) return

        // Track paused duration
        totalPausedDuration += currentTimeMillis() - pauseStartTime

        _isPaused.value = false

        // Resume treadmill
        ftmsManager.resume()

        addCoachMessage("▶️ 训练已恢复")
    }

    /** Stop workout session and upload data */
    suspend fun stopWorkout() {
        _isPaused.value = false
        aiUpdateJob?.cancel()

        // Calculate workout summary
        val endTime = currentTimeMillis()
        val actualDuration = ((endTime - workoutStartTime - totalPausedDuration) / 1000).toInt()
        val startTimeIso =
                kotlinx.datetime.Instant.fromEpochMilliseconds(workoutStartTime).toString()
        val endTimeIso = kotlinx.datetime.Instant.fromEpochMilliseconds(endTime).toString()

        val createWorkoutRequest =
                WorkoutSessionRequest(
                        startTime = startTimeIso,
                        endTime = endTimeIso,
                        durationSeconds = actualDuration,
                        distanceMeters = (totalDistance * 1000).toDouble(), // Convert km to meters
                        caloriesBurned =
                                calculateCalories(actualDuration, totalDistance).toDouble(),
                        avgSpeed =
                                if (speedSampleCount > 0)
                                        (totalSpeedSamples / speedSampleCount).toDouble()
                                else 0.0,
                        maxSpeed = maxSpeed.toDouble(),
                        avgIncline =
                                if (inclineSampleCount > 0)
                                        (totalInclineSamples / inclineSampleCount).toDouble()
                                else 0.0,
                        avgHeartRate =
                                if (heartRateSampleCount > 0)
                                        totalHeartRateSamples / heartRateSampleCount
                                else null,
                        maxHeartRate = maxHeartRate,
                        musics = workoutMusics,
                        points = workoutDataPoints
                )

        // Upload workout data
        val sessionId = _sessionId.value
        if (sessionId != null) {
            backendApi
                    .finishWorkoutSession(sessionId, createWorkoutRequest)
                    .onSuccess { sessionDetail ->
                        addCoachMessage("✅ 训练已上传！Session ID: ${sessionDetail.id}")
                        _sessionId.value = null
                    }
                    .onFailure { error ->
                        addCoachMessage("⚠️ 数据上传失败: ${error.message}", isError = true)
                    }
        } else {
            addCoachMessage("⚠️ 未找到 Session ID，数据未能上传", isError = true)
        }

        // Stop treadmill
        ftmsManager.stop()
        delay(500)

        // Disconnect
        ftmsManager.disconnect()
        musicObserver.setVisualizationEnabled(false)
        musicObserver.stopListening()

        // Disconnect coach
        wsJob?.cancel()
        scope.launch { backendApi.disconnectCoachWebSocket() }

        addCoachMessage("✅ 训练结束！干得漂亮！")

        // Update state last to avoid cancelling scope during network request
        _isWorkoutActive.value = false
    }

    /** Manually send command to treadmill */
    suspend fun sendManualCommand(command: BluetoothModels.TreadmillCommand) {
        ftmsManager.sendCommand(command)
        addCoachMessage("⚙️ 手动调整速度: ${command.targetSpeed} km/h")
    }

    /** Request music permission */
    suspend fun requestMusicPermission() {
        musicObserver.requestPermission()
        delay(1000)
        _musicPermissionGranted.value = musicObserver.hasPermission()
    }

    /** Start realtime AI coaching via WebSocket */
    private fun startWebSocketCoach() {
        wsJob?.cancel()
        wsJob =
                scope.launch {
                    try {
                        backendApi.connectCoachWebSocket().collect { aiResponse ->
                            // Make command
                            val command =
                                    BluetoothModels.TreadmillCommand(
                                            targetSpeed = aiResponse.speed,
                                            targetIncline = aiResponse.incline
                                    )
                            // Send command to treadmill
                            ftmsManager.sendCommand(command)

                            // Display coach message
                            addCoachMessage(aiResponse.coachMessage, isAI = true)
                        }
                    } catch (e: Exception) {
                        addCoachMessage("⚠️ AI 教练连线中断", isError = true)
                    }
                }
    }

    /** Add coach message to UI */
    private fun addCoachMessage(message: String, isAI: Boolean = false, isError: Boolean = false) {
        val messages = _coachMessages.value.toMutableList()
        messages.add(
                CoachMessage(
                        text = message,
                        isAI = isAI,
                        isError = isError,
                        timestamp = currentTimeMillis()
                )
        )
        // Keep only last 10 messages
        if (messages.size > 10) {
            messages.removeAt(0)
        }
        _coachMessages.value = messages
    }

    fun dispose() {
        aiUpdateJob?.cancel()
        scope.launch {
            if (_isWorkoutActive.value) {
                stopWorkout()
            }
        }
    }

    /** Set target speed */
    fun setSpeed(speed: Float) {
        scope.launch { ftmsManager.setSpeed(speed) }
    }

    /** Set target incline */
    fun setIncline(incline: Float) {
        scope.launch { ftmsManager.setIncline(incline) }
    }

    /** Reset workout statistics */
    private fun resetWorkoutStatistics() {
        workoutStartTime = currentTimeMillis()
        totalPausedDuration = 0
        pauseStartTime = 0
        totalDistance = 0f
        maxSpeed = 0f
        totalSpeedSamples = 0f
        speedSampleCount = 0
        totalInclineSamples = 0f
        inclineSampleCount = 0
        maxHeartRate = null
        totalHeartRateSamples = 0
        heartRateSampleCount = 0
        songsPlayed.clear()
        workoutDataPoints.clear()
        workoutMusics.clear()
        lastUploadedSong = null
    }

    /**
     * Calculate calories burned (metrics) Simple METs formula: Calories = METs * weight (kg) * time
     * (hours) Running 8km/h is approx 8 METs
     */
    private fun calculateCalories(durationSeconds: Int, distanceKm: Float): Int {
        // Very rough estimation
        val caloriesPerKm = userWeightKg * 1.03f
        return (distanceKm * caloriesPerKm).toInt()
    }
}

/** Represents a coach message in the UI */
data class CoachMessage(
        val text: String,
        val isAI: Boolean = false,
        val isError: Boolean = false,
        val timestamp: Long = currentTimeMillis()
)

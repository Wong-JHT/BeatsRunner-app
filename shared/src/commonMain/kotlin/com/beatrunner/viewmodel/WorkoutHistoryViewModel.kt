package com.beatrunner.viewmodel

import com.beatrunner.data.network.BackendApi
import com.beatrunner.data.network.WorkoutSessionDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for workout history screen
 * Manages workout sessions list with pagination
 */
class WorkoutHistoryViewModel(
    private val backendApi: BackendApi,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    
    // State flows
    private val _workoutSessions = MutableStateFlow<List<WorkoutSessionDetail>>(emptyList())
    val workoutSessions: StateFlow<List<WorkoutSessionDetail>> = _workoutSessions.asStateFlow()
    
    private val _selectedSession = MutableStateFlow<WorkoutSessionDetail?>(null)
    val selectedSession: StateFlow<WorkoutSessionDetail?> = _selectedSession.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentPage = 0
    private var hasMorePages = true
    private val pageSize = 10
    
    init {
        loadWorkoutSessions()
    }
    
    /**
     * Load initial workout sessions
     */
    fun loadWorkoutSessions() {
        if (_isLoading.value) return
        
        scope.launch {
            _isLoading.value = true
            _error.value = null
            currentPage = 1
            
            val result = backendApi.getWorkoutSessions(page = 1, size = pageSize)
            
            result.onSuccess { response ->
                _workoutSessions.value = response.data
                hasMorePages = currentPage < response.totalPages
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = exception.message ?: "加载失败"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load more workout sessions (pagination)
     */
    fun loadMoreSessions() {
        if (_isLoadingMore.value || !hasMorePages) return
        
        scope.launch {
            _isLoadingMore.value = true
            currentPage++
            
            val result = backendApi.getWorkoutSessions(page = currentPage, size = pageSize)
            
            result.onSuccess { response ->
                val currentSessions = _workoutSessions.value.toMutableList()
                currentSessions.addAll(response.data)
                _workoutSessions.value = currentSessions
                hasMorePages = currentPage < response.totalPages
                _isLoadingMore.value = false
            }.onFailure { exception ->
                _error.value = exception.message ?: "加载更多失败"
                currentPage-- // Rollback page increment
                _isLoadingMore.value = false
            }
        }
    }
    
    /**
     * Refresh workout sessions (pull-to-refresh)
     */
    fun refreshSessions() {
        loadWorkoutSessions()
    }
    
    /**
     * Get specific workout session by ID
     */
    fun getSessionById(id: String): WorkoutSessionDetail? {
        return _workoutSessions.value.find { it.id == id }
    }

    /**
     * Load full details for a session
     */
    fun loadSessionDetail(id: String) {
        scope.launch {
            _isLoading.value = true
            val result = backendApi.getWorkoutSessionDetail(id)
            
            result.onSuccess { detail ->
                _selectedSession.value = detail
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = exception.message ?: "加载详情失败"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Dispose resources
     */
    fun dispose() {
        // Cleanup if needed
    }
}

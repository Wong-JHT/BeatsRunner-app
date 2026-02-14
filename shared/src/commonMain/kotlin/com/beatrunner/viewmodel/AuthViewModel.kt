package com.beatrunner.viewmodel

import com.beatrunner.data.local.TokenManager
import com.beatrunner.data.network.BackendApi
import com.beatrunner.data.network.LoginRequest
import com.beatrunner.data.network.RegisterRequest
import com.beatrunner.data.network.UpdateProfileRequest
import com.beatrunner.data.network.UserProfileData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication and user profile management
 */
class AuthViewModel(
    private val backendApi: BackendApi,
    private val tokenManager: TokenManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    
    /**
     * Authentication states
     */
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val username: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(tokenManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfileData?>(null)
    val userProfile: StateFlow<UserProfileData?> = _userProfile.asStateFlow()
    
    private val _currentUsername = MutableStateFlow(tokenManager.getUsername())
    val currentUsername: StateFlow<String?> = _currentUsername.asStateFlow()
    
    init {
        // Load user profile if already logged in
        if (tokenManager.isLoggedIn()) {
            loadUserProfile()
        }
    }
    
    /**
     * Register new user account
     * @param identityType Type of identity: email, phone, apple, wechat
     * @param identifier The actual identifier (email address, phone number, Apple Sub, OpenID)
     * @param password Password (optional for third-party auth)
     * @param nickname Optional nickname
     * @param extraData Optional JSON string for third-party auth data
     */
    suspend fun register(
        identityType: String = "email",
        identifier: String,
        password: String? = null,
        nickname: String? = null,
        extraData: String? = null
    ) {
        _authState.value = AuthState.Loading
        
        val result = backendApi.register(
            RegisterRequest(
                identityType = identityType,
                identifier = identifier,
                password = password,
                nickname = nickname,
                extraData = extraData
            )
        )
        
        result.onSuccess { response ->
            tokenManager.saveAuthData(response.token, response.accountId)
            // Use nickname if provided, otherwise use identifier as display name
            val displayName = nickname ?: identifier
            tokenManager.saveUsername(displayName)
            _isLoggedIn.value = true
            _currentUsername.value = displayName
            _authState.value = AuthState.Success(displayName)
            loadUserProfile()
        }.onFailure { error ->
            _authState.value = AuthState.Error(error.message ?: "注册失败")
        }
    }
    
    /**
     * Login with identifier (username/email/phone) and password
     */
    suspend fun login(identifier: String, password: String) {
        _authState.value = AuthState.Loading
        
        val result = backendApi.login(
            LoginRequest(
                identifier = identifier,
                password = password
            )
        )
        
        result.onSuccess { response ->
            tokenManager.saveAuthData(response.token, response.accountId)
            // AuthResponse doesn't return username, so we don't update it here yet
            // It will be updated when profile loads
            _isLoggedIn.value = true
            
            // If identifier looks like a username, we could tentatively set it, 
            // but safer to wait for profile or leave as null/previous
             _authState.value = AuthState.Success(identifier) // Use identifier as temporary display name
            loadUserProfile()
        }.onFailure { error ->
            _authState.value = AuthState.Error(error.message ?: "登录失败")
        }
    }
    
    /**
     * Logout current user
     */
    fun logout() {
        tokenManager.clearAuthData()
        _isLoggedIn.value = false
        _userProfile.value = null
        _currentUsername.value = null
        _authState.value = AuthState.Idle
    }
    
    /**
     * Load user profile from backend
     */
    private fun loadUserProfile() {
        scope.launch {
            val result = backendApi.getUserProfile()
            result.onSuccess { profile ->
                _userProfile.value = profile
            }.onFailure { error ->
                // Profile load failed, but user is still logged in
                println("Failed to load user profile: ${error.message}")
            }
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(
        height: Int? = null,
        weight: Double? = null,
        age: Int? = null,
        nickname: String? = null,
        avatar: String? = null
    ): Result<UserProfileData> {
        val request = UpdateProfileRequest(
            height = height,
            weight = weight,
            age = age,
            nickname = nickname,
            avatar = avatar
        )
        
        val result = backendApi.updateUserProfile(request)
        result.onSuccess { updatedProfile ->
            _userProfile.value = updatedProfile
        }
        
        return result
    }
    
    /**
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit> {
        val result = backendApi.deleteAccount()
        result.onSuccess {
            logout()
        }
        return result
    }
    
    /**
     * Reset auth state to idle
     */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

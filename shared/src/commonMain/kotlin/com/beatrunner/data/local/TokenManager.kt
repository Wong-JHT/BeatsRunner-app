package com.beatrunner.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Manages JWT token and user authentication data storage
 * Uses platform-specific secure storage (SharedPreferences/UserDefaults)
 */
class TokenManager(private val settings: Settings) {
    
    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_USERNAME = "username"
    }
    
    /**
     * Save authentication data after successful login/register
     */
    fun saveAuthData(token: String, accountId: String) {
        settings[KEY_AUTH_TOKEN] = token
        settings[KEY_ACCOUNT_ID] = accountId
    }

    /**
     * Save username separately
     */
    fun saveUsername(username: String) {
        settings[KEY_USERNAME] = username
    }
    
    /**
     * Get stored JWT token
     */
    fun getToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)
    
    /**
     * Get stored account ID
     */
    fun getAccountId(): String? = settings.getStringOrNull(KEY_ACCOUNT_ID)
    
    /**
     * Get stored username
     */
    fun getUsername(): String? = settings.getStringOrNull(KEY_USERNAME)
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = getToken() != null
    
    /**
     * Clear all authentication data (logout)
     */
    fun clearAuthData() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_ACCOUNT_ID)
        settings.remove(KEY_USERNAME)
    }
}

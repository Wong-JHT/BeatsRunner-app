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
        private const val KEY_IS_BASIC_INFO_COMPLETED = "is_basic_info_completed"
        private const val KEY_IS_FITNESS_LEVEL_COMPLETED = "is_fitness_level_completed"
        private const val KEY_IS_FITNESS_GOAL_COMPLETED = "is_fitness_goal_completed"
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
     * Save whether basic info is completed
     */
    fun saveBasicInfoCompleted(isCompleted: Boolean) {
        settings[KEY_IS_BASIC_INFO_COMPLETED] = isCompleted
    }
    
    /**
     * Save whether fitness level is completed
     */
    fun saveFitnessLevelCompleted(isCompleted: Boolean) {
        settings[KEY_IS_FITNESS_LEVEL_COMPLETED] = isCompleted
    }
    
    /**
     * Save whether fitness goal is completed
     */
    fun saveFitnessGoalCompleted(isCompleted: Boolean) {
        settings[KEY_IS_FITNESS_GOAL_COMPLETED] = isCompleted
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
     * Get whether basic info is completed
     * Returns null if not set
     */
    fun getBasicInfoCompleted(): Boolean? {
        if (!settings.hasKey(KEY_IS_BASIC_INFO_COMPLETED)) return null
        return settings.getBoolean(KEY_IS_BASIC_INFO_COMPLETED, false)
    }
    
    /**
     * Get whether fitness level is completed
     * Returns null if not set
     */
    fun getFitnessLevelCompleted(): Boolean? {
        if (!settings.hasKey(KEY_IS_FITNESS_LEVEL_COMPLETED)) return null
        return settings.getBoolean(KEY_IS_FITNESS_LEVEL_COMPLETED, false)
    }
    
    /**
     * Get whether fitness goal is completed
     * Returns null if not set
     */
    fun getFitnessGoalCompleted(): Boolean? {
        if (!settings.hasKey(KEY_IS_FITNESS_GOAL_COMPLETED)) return null
        return settings.getBoolean(KEY_IS_FITNESS_GOAL_COMPLETED, false)
    }
    
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
        settings.remove(KEY_IS_BASIC_INFO_COMPLETED)
        settings.remove(KEY_IS_FITNESS_LEVEL_COMPLETED)
        settings.remove(KEY_IS_FITNESS_GOAL_COMPLETED)
    }
}

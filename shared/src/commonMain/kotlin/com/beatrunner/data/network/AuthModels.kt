package com.beatrunner.data.network

import kotlinx.serialization.Serializable

/**
 * User registration request
 * Supports multiple identity types: email, phone, apple, wechat
 */
@Serializable
data class RegisterRequest(
    val identityType: String, // email, phone, apple, wechat
    val identifier: String, // 邮箱、手机号、Apple Sub、OpenID
    val password: String? = null,
    val nickname: String? = null,
    val extraData: String? = null // JSON for third-party auth
)

/**
 * User login request
 */
@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String,
    val identityType: String = "email"
)

/**
 * Authentication response containing JWT token
 */
@Serializable
data class AuthResponse(
    val token: String,
    val accountId: String,
    val profileCompleted: Boolean
)

/**
 * User profile data matching backend schema
 */
@Serializable
data class UserProfileData(
    val height: Int? = null,      // cm
    val weight: Double? = null,   // kg  
    val age: Int? = null,
    val nickname: String? = null,
    val avatar: String? = null
)

/**
 * Update user profile request
 */
@Serializable
data class UpdateProfileRequest(
    val height: Int? = null,
    val weight: Double? = null,
    val age: Int? = null,
    val nickname: String? = null,
    val avatar: String? = null
)

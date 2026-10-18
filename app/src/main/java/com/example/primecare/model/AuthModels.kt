package com.example.primecare.model

/** POST body for /api/staff/auth/login.php */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response shape from login.php AND me.php — both endpoints return
 * the same staff-profile fields, just login.php additionally
 * includes the token itself.
 */
data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val role: String? = null,       // "DOCTOR" or "ADMIN" — matches StaffRole.name
    val staffId: Int? = null,
    val firstName: String? = null,
    val lastName: String? = null
)

data class StaffProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val role: String? = null,
    val staffId: Int? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)

/** Generic shape for endpoints that only ever return success/message. */
data class SimpleResponse(
    val success: Boolean,
    val message: String? = null
)

/** POST body for /api/staff/auth/register.php */
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String,              // "ADMIN" or "DOCTOR"
    val registrationCode: String
)

data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

data class UpdateProfileRequest(val firstName: String, val lastName: String)

/** POST body for /api/staff/auth/google.php (single sign-on). */
data class GoogleLoginRequest(val idToken: String)

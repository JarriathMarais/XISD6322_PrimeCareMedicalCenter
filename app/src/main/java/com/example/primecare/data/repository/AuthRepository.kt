package com.example.primecare.data.repository

import com.example.primecare.data.api.ApiClient
import com.example.primecare.data.api.ApiHelper
import com.example.primecare.data.local.SessionManager
import com.example.primecare.data.local.StaffRole
import com.example.primecare.model.ChangePasswordRequest
import com.example.primecare.model.GoogleLoginRequest
import com.example.primecare.model.LoginRequest
import com.example.primecare.model.LoginResponse
import com.example.primecare.model.RegisterRequest
import com.example.primecare.model.SimpleResponse
import com.example.primecare.model.StaffProfileResponse
import com.example.primecare.model.UpdateProfileRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed class AuthResult {
    data class Success(val role: StaffRole, val firstName: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val sessionManager: SessionManager) {

    private val api get() = ApiClient.apiService

    suspend fun login(email: String, password: String): AuthResult =
        finishLogin(
            ApiHelper.call(expireSessionOn401 = false) {
                api.login(LoginRequest(email, password))
            }
        )

    suspend fun googleLogin(idToken: String): AuthResult =
        finishLogin(
            ApiHelper.call(expireSessionOn401 = false) {
                api.googleLogin(GoogleLoginRequest(idToken))
            }
        )

    private fun finishLogin(result: DataResult<LoginResponse>): AuthResult {
        return when (result) {
            is DataResult.Error -> AuthResult.Error(result.message)
            is DataResult.Success -> {
                val body = result.data
                val role = parseRole(body.role)
                val token = body.token
                if (!body.success || role == null || token.isNullOrBlank()) {
                    AuthResult.Error(body.message ?: "Unexpected response from server.")
                } else {
                    sessionManager.saveSession(
                        token = token,
                        role = role,
                        staffId = body.staffId ?: -1,
                        name = body.firstName.orEmpty()
                    )
                    AuthResult.Success(role, body.firstName.orEmpty())
                }
            }
        }
    }

    suspend fun register(request: RegisterRequest): DataResult<SimpleResponse> =
        ApiHelper.call(expireSessionOn401 = false) { api.register(request) }

    suspend fun loadProfile(): DataResult<StaffProfileResponse> =
        ApiHelper.call { api.me() }

    suspend fun updateProfile(firstName: String, lastName: String): DataResult<SimpleResponse> {
        val result = ApiHelper.call { api.updateProfile(UpdateProfileRequest(firstName, lastName)) }
        if (result is DataResult.Success) {
            sessionManager.updateName(firstName)
        }
        return result
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): DataResult<SimpleResponse> =
        ApiHelper.call { api.changePassword(ChangePasswordRequest(currentPassword, newPassword)) }

    suspend fun ping(): DataResult<SimpleResponse> =
        ApiHelper.call(expireSessionOn401 = false) { api.ping() }

    suspend fun validateSession(): StaffRole? {
        if (!sessionManager.isLoggedIn()) return null

        return try {
            val response = api.me()
            when {
                response.isSuccessful && response.body()?.success == true ->
                    parseRole(response.body()?.role) ?: sessionManager.getRole()
                response.code() == 401 || response.code() == 403 -> {
                    sessionManager.clearSession()
                    null
                }
                else -> sessionManager.getRole()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            sessionManager.getRole()
        }
    }

    fun logout() {
        val token = sessionManager.getToken()
        sessionManager.clearSession()
        if (!token.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { api.logout("Bearer $token") }
            }
        }
    }

    private fun parseRole(role: String?): StaffRole? =
        role?.let { runCatching { StaffRole.valueOf(it) }.getOrNull() }
}
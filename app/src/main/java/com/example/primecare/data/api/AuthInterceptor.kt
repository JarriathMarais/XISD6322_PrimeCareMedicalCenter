package com.example.primecare.data.api

import com.example.primecare.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * PrimeCare — Auth Interceptor
 * ---------------------------------------------------------
 * Attaches "Authorization: Bearer <token>" to every outgoing
 * request automatically, IF a token is currently stored. This is
 * what lets every Repository just call the API normally without
 * manually threading the token through every single function.
 *
 * Login itself doesn't need a token yet (there isn't one), so this
 * safely no-ops when SessionManager has nothing stored — the
 * request just goes out without the header, which is exactly what
 * login.php expects (no auth-check.php on that endpoint).
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessionManager.getToken()

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}

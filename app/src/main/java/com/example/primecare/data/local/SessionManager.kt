package com.example.primecare.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * PrimeCare — Session Manager
 * ---------------------------------------------------------
 * Stores the staff auth token and role in EncryptedSharedPreferences
 * (AES-256, backed by the Android Keystore) rather than plain
 * SharedPreferences, since this token proves a real clinician's
 * identity against sensitive medical data.
 *
 * Populated from the real login (or Google sign-in) response.
 */
class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(token: String, role: StaffRole, staffId: Int, name: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role.name)
            .putInt(KEY_STAFF_ID, staffId)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun updateName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getRole(): StaffRole? =
        prefs.getString(KEY_ROLE, null)?.let { runCatching { StaffRole.valueOf(it) }.getOrNull() }

    fun getStaffId(): Int = prefs.getInt(KEY_STAFF_ID, -1)

    fun getName(): String? = prefs.getString(KEY_NAME, null)

    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "primecare_staff_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_ROLE = "role"
        private const val KEY_STAFF_ID = "staff_id"
        private const val KEY_NAME = "name"
    }
}

/**
 * Mirrors the two staff roles from the system design: Administrators
 * are NOT necessarily doctors (e.g. front-desk staff), so this is a
 * distinct role model from the `doctors` table's clinical identity.
 */
enum class StaffRole {
    ADMIN,
    DOCTOR
}

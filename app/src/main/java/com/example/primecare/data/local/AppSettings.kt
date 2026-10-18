package com.example.primecare.data.local

import android.content.Context

/**
 * Non-sensitive user preferences (the server address and queue
 * auto-refresh). The login token is NOT stored here — that lives in
 * the encrypted SessionManager.
 */
object AppSettings {

    const val DEFAULT_SERVER_URL = "http://10.0.2.2/primecarewebsite/"
    private const val PREFS = "primecare_settings"
    private const val KEY_SERVER = "server_url"
    private const val KEY_AUTO_REFRESH = "queue_auto_refresh"

    private val URL_PATTERN = Regex("^https?://[^\\s/?#]+(/[^\\s?#]*)?$", RegexOption.IGNORE_CASE)

    /** Returns the cleaned address (always ending in "/"), or null if it isn't a valid http(s) URL. */
    fun normalizeServerUrl(input: String): String? {
        val trimmed = input.trim()
        if (!URL_PATTERN.matches(trimmed)) return null
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    fun getServerUrl(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SERVER, null) ?: DEFAULT_SERVER_URL

    fun setServerUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_SERVER, url).apply()
    }

    fun isAutoRefreshEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_AUTO_REFRESH, true)

    fun setAutoRefreshEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_REFRESH, enabled).apply()
    }
}

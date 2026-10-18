package com.example.primecare.data.api

import android.content.Context
import com.example.primecare.data.local.AppSettings
import com.example.primecare.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * PrimeCare — API Client
 * ---------------------------------------------------------
 * Builds the Retrofit instance every Repository uses to talk to the
 * PrimeCare PHP backend's api/staff/ namespace.
 *
 * The server address is a USER SETTING (Settings > Server address, also
 * reachable from the login screen), so the same APK works on the
 * emulator (10.0.2.2), a phone on the same Wi-Fi (the PC's LAN IP) and
 * a hosted server (https://...). Changing it calls resetBaseUrl() and
 * the next request rebuilds Retrofit with the new address.
 *
 * Must be initialised once via ApiClient.init(context) from
 * Application.onCreate().
 */
object ApiClient {

    private lateinit var appContext: Context
    private lateinit var sessionManager: SessionManager

    @Volatile
    private var cached: ApiService? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        sessionManager = SessionManager(appContext)
    }

    fun clearSession() {
        if (::sessionManager.isInitialized) sessionManager.clearSession()
    }

    /** Call after the server address setting changes. */
    fun resetBaseUrl() {
        cached = null
    }

    // BASIC logs method/URL/status only. BODY would write passwords and
    // tokens into logcat.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val apiService: ApiService
        get() = cached ?: synchronized(this) {
            cached ?: Retrofit.Builder()
                .baseUrl(AppSettings.getServerUrl(appContext))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
                .also { cached = it }
        }
}

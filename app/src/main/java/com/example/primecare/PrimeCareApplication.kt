package com.example.primecare

import android.app.Application
import com.example.primecare.data.api.ApiClient

/**
 * PrimeCare — Application class
 * ---------------------------------------------------------
 * One-time app-wide setup, run before any Activity/Fragment.
 * Currently just initializes ApiClient so AuthInterceptor can read
 * the stored session token — grows here if other app-wide
 * singletons are needed later (e.g. crash reporting).
 */
class PrimeCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}

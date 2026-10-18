package com.example.primecare.data.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Raised when the server answers 401 to a logged-in request (token
 * expired or revoked). MainActivity observes this, wipes navigation and
 * returns to the login screen, so no screen has to handle it itself.
 */
object SessionEvents {
    private val _expired = MutableLiveData<Boolean>()
    val expired: LiveData<Boolean> = _expired

    fun expire() {
        ApiClient.clearSession()
        _expired.postValue(true)
    }

    fun consume() {
        _expired.value = false
    }
}

package com.example.primecare.data.api

import com.example.primecare.data.repository.DataResult
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Wraps every Retrofit call so repositories/UI only ever see a
 * DataResult with a human-readable message — never an exception or a
 * raw HTTP code.
 */
object ApiHelper {

    suspend fun <T> call(
        expireSessionOn401: Boolean = true,
        block: suspend () -> Response<T>
    ): DataResult<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) DataResult.Success(body)
                else DataResult.Error("The server sent an empty response.")
            } else {
                if (response.code() == 401 && expireSessionOn401) SessionEvents.expire()
                DataResult.Error(messageFrom(response.errorBody()?.string(), response.code()))
            }
        } catch (e: CancellationException) {
            throw e   // never swallow coroutine cancellation (fragment may be gone)
        } catch (e: IOException) {
            DataResult.Error("Could not reach the server. Check your connection and try again.")
        } catch (e: Exception) {
            DataResult.Error("Something went wrong. Please try again.")
        }
    }

    private fun messageFrom(errorBody: String?, code: Int): String {
        val fromServer = runCatching {
            JsonParser.parseString(errorBody).asJsonObject.get("message")?.asString
        }.getOrNull()
        if (!fromServer.isNullOrBlank()) return fromServer
        return when (code) {
            401 -> "Your session has expired. Please log in again."
            403 -> "You do not have permission to do that."
            404 -> "That could not be found on the server."
            429 -> "Too many attempts. Please wait a few minutes and try again."
            in 500..599 -> "The server had a problem. Please try again."
            else -> "Something went wrong (error $code)."
        }
    }
}

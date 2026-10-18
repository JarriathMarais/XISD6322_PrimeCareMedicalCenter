package com.example.primecare

import com.example.primecare.data.api.ApiHelper
import com.example.primecare.data.repository.DataResult
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/** Checks that every network outcome becomes a friendly DataResult (never a crash). */
class ApiHelperTest {

    private fun errorBody(json: String) = json.toResponseBody("application/json".toMediaType())

    @Test fun successReturnsBody() = runBlocking {
        val result = ApiHelper.call { Response.success("hello") }
        assertEquals(DataResult.Success("hello"), result)
    }

    @Test fun serverMessageIsShown() = runBlocking {
        val result = ApiHelper.call<String> {
            Response.error(422, errorBody("""{"success":false,"message":"Please enter a valid email."}"""))
        }
        assertEquals(DataResult.Error("Please enter a valid email."), result)
    }

    @Test fun serverErrorWithoutJsonGetsGenericMessage() = runBlocking {
        val result = ApiHelper.call<String> { Response.error(500, errorBody("<html>boom</html>")) }
        assertTrue(result is DataResult.Error)
        assertTrue((result as DataResult.Error).message.contains("server", ignoreCase = true))
    }

    @Test fun wrongPasswordDoesNotExpireSession() = runBlocking {
        val result = ApiHelper.call<String>(expireSessionOn401 = false) {
            Response.error(401, errorBody("""{"message":"Invalid email or password."}"""))
        }
        assertEquals(DataResult.Error("Invalid email or password."), result)
    }

    @Test fun networkFailureIsFriendly() = runBlocking {
        val result = ApiHelper.call<String> { throw IOException("timeout") }
        assertTrue((result as DataResult.Error).message.startsWith("Could not reach the server"))
    }
}

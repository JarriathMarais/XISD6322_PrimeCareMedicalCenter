package com.example.primecare.data.repository

import com.example.primecare.data.api.ApiClient
import com.example.primecare.data.api.ApiHelper
import com.example.primecare.model.AppointmentDto

class AppointmentRepository {

    private val api get() = ApiClient.apiService

    suspend fun getTodayAppointments(
        query: String? = null,
        status: String? = null,
        unassignedOnly: Boolean = false
    ): DataResult<List<AppointmentDto>> =
        ApiHelper.call {
            api.getTodayAppointments(
                query = query?.takeIf { it.isNotBlank() },
                status = status,
                unassigned = if (unassignedOnly) 1 else null
            )
        }.map { it.appointments }
}

package com.example.primecare.data.repository

import com.example.primecare.data.api.ApiClient
import com.example.primecare.data.api.ApiHelper
import com.example.primecare.model.DoctorDto

class DoctorRepository {

    private val api get() = ApiClient.apiService

    suspend fun getDoctors(): DataResult<List<DoctorDto>> =
        ApiHelper.call { api.getDoctors() }.map { it.doctors }
}

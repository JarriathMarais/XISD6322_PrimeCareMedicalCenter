package com.example.primecare.data.repository

import com.example.primecare.data.api.ApiClient
import com.example.primecare.data.api.ApiHelper
import com.example.primecare.model.*

/**
 * Everything the reception and doctor workflows need beyond login and
 * the plain appointment/doctor lists. One thin function per endpoint.
 */
class ClinicRepository {

    private val api get() = ApiClient.apiService

    // ---- dashboards ----
    suspend fun adminStats(): DataResult<AdminDashboardStats> =
        ApiHelper.call { api.getAdminStats() }.map { it.toStats() }

    suspend fun doctorStats(): DataResult<DoctorDashboardStats> =
        ApiHelper.call { api.getDoctorStats() }.map { it.toStats() }

    // ---- reception ----
    suspend fun checkIn(appointmentId: Int): DataResult<CheckInResponse> =
        ApiHelper.call { api.checkIn(AppointmentIdRequest(appointmentId)) }

    suspend fun assignDoctor(appointmentId: Int, doctorId: Int): DataResult<SimpleResponse> =
        ApiHelper.call { api.assignDoctor(AssignDoctorRequest(appointmentId, doctorId)) }

    suspend fun registerWalkIn(fullName: String, phone: String, reason: String): DataResult<WalkInResponse> =
        ApiHelper.call { api.registerWalkIn(WalkInRequest(fullName, phone, reason)) }

    suspend fun queue(): DataResult<QueueResponse> = ApiHelper.call { api.getQueue() }

    // ---- consultation ----
    suspend fun startConsultation(appointmentId: Int): DataResult<ConsultationResponse> =
        ApiHelper.call { api.startConsultation(AppointmentIdRequest(appointmentId)) }

    suspend fun saveConsultation(request: SaveConsultationRequest): DataResult<SaveConsultationResponse> =
        ApiHelper.call { api.saveConsultation(request) }

    suspend fun createPrescription(request: PrescriptionRequest): DataResult<SimpleResponse> =
        ApiHelper.call { api.createPrescription(request) }

    suspend fun prescriptions(appointmentId: Int): DataResult<List<PrescriptionDto>> =
        ApiHelper.call { api.getPrescriptions(appointmentId) }.map { it.prescriptions }

    suspend fun createFollowUp(appointmentId: Int, date: String, reason: String): DataResult<SimpleResponse> =
        ApiHelper.call { api.createFollowUp(FollowUpRequest(appointmentId, date, reason)) }

    // ---- repeat requests ----
    suspend fun repeatRequests(): DataResult<List<RepeatRequestDto>> =
        ApiHelper.call { api.getRepeatRequests() }.map { it.requests }

    suspend fun decideRepeatRequest(requestId: Int, approve: Boolean): DataResult<SimpleResponse> =
        ApiHelper.call { api.decideRepeatRequest(DecideRepeatRequest(requestId, approve)) }
}

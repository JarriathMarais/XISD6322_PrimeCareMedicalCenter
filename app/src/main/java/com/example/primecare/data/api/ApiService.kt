package com.example.primecare.data.api

import com.example.primecare.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * PrimeCare — Staff API Service
 * Maps one-to-one onto api/staff/... on the PHP backend.
 */
interface ApiService {

    // ---- auth ----
    @POST("api/staff/auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /** Header passed explicitly because the local token is wiped before this fires. */
    @POST("api/staff/auth/logout.php")
    suspend fun logout(@Header("Authorization") authorization: String): Response<SimpleResponse>

    @POST("api/staff/auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<SimpleResponse>

    @POST("api/staff/auth/google.php")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @POST("api/staff/auth/change-password.php")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<SimpleResponse>

    @POST("api/staff/auth/update-profile.php")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<SimpleResponse>

    /** Used by Settings to test a server address. */
    @GET("api/staff/ping.php")
    suspend fun ping(): Response<SimpleResponse>

    @GET("api/staff/auth/me.php")
    suspend fun me(): Response<StaffProfileResponse>

    // ---- dashboards ----
    @GET("api/staff/dashboard/admin.php")
    suspend fun getAdminStats(): Response<AdminStatsResponse>

    @GET("api/staff/dashboard/doctor.php")
    suspend fun getDoctorStats(): Response<DoctorStatsResponse>

    // ---- appointments / reception ----
    @GET("api/staff/appointments/today.php")
    suspend fun getTodayAppointments(
        @Query("q") query: String? = null,
        @Query("status") status: String? = null,
        @Query("unassigned") unassigned: Int? = null
    ): Response<TodayAppointmentsResponse>

    @POST("api/staff/appointments/checkin.php")
    suspend fun checkIn(@Body request: AppointmentIdRequest): Response<CheckInResponse>

    @POST("api/staff/appointments/assign.php")
    suspend fun assignDoctor(@Body request: AssignDoctorRequest): Response<SimpleResponse>

    @POST("api/staff/appointments/walkin.php")
    suspend fun registerWalkIn(@Body request: WalkInRequest): Response<WalkInResponse>

    @GET("api/staff/queue/list.php")
    suspend fun getQueue(): Response<QueueResponse>

    @GET("api/staff/doctors/list.php")
    suspend fun getDoctors(): Response<DoctorsListResponse>

    // ---- consultations ----
    @POST("api/staff/consultations/start.php")
    suspend fun startConsultation(@Body request: AppointmentIdRequest): Response<ConsultationResponse>

    @POST("api/staff/consultations/save.php")
    suspend fun saveConsultation(@Body request: SaveConsultationRequest): Response<SaveConsultationResponse>

    @POST("api/staff/prescriptions/create.php")
    suspend fun createPrescription(@Body request: PrescriptionRequest): Response<SimpleResponse>

    @GET("api/staff/prescriptions/list.php")
    suspend fun getPrescriptions(@Query("appointmentId") appointmentId: Int): Response<PrescriptionsResponse>

    @POST("api/staff/followups/create.php")
    suspend fun createFollowUp(@Body request: FollowUpRequest): Response<SimpleResponse>

    // ---- repeat prescription requests ----
    @GET("api/staff/repeat-requests/list.php")
    suspend fun getRepeatRequests(): Response<RepeatRequestsResponse>

    @POST("api/staff/repeat-requests/decide.php")
    suspend fun decideRepeatRequest(@Body request: DecideRepeatRequest): Response<SimpleResponse>
}

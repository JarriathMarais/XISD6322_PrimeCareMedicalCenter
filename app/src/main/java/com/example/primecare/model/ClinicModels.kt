package com.example.primecare.model

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------- dashboards

data class AdminDashboardStats(
    val todaysAppointments: Int = 0,
    val patientsWaiting: Int = 0,
    val walkinsToday: Int = 0,
    val availableDoctors: Int = 0
)

data class DoctorDashboardStats(
    val assignedPatients: Int = 0,
    val waitingPatients: Int = 0,
    val completedToday: Int = 0,
    val followups: Int = 0
)

data class AdminStatsResponse(
    val success: Boolean,
    val message: String? = null,
    val todaysAppointments: Int = 0,
    val patientsWaiting: Int = 0,
    val walkinsToday: Int = 0,
    val availableDoctors: Int = 0
) {
    fun toStats() = AdminDashboardStats(todaysAppointments, patientsWaiting, walkinsToday, availableDoctors)
}

data class DoctorStatsResponse(
    val success: Boolean,
    val message: String? = null,
    val assignedPatients: Int = 0,
    val waitingPatients: Int = 0,
    val completedToday: Int = 0,
    val followups: Int = 0
) {
    fun toStats() = DoctorDashboardStats(assignedPatients, waitingPatients, completedToday, followups)
}

// -------------------------------------------------------------------- queue

data class QueueResponse(
    val success: Boolean,
    val message: String? = null,
    val nowServing: List<AppointmentDto> = emptyList(),
    val waiting: List<AppointmentDto> = emptyList()
)

// ------------------------------------------------------- check-in / assign

data class AppointmentIdRequest(val appointmentId: Int)

data class CheckInResponse(
    val success: Boolean,
    val message: String? = null,
    val queueNumber: Int? = null
)

data class AssignDoctorRequest(val appointmentId: Int, val doctorId: Int)

data class WalkInRequest(val fullName: String, val phone: String, val reason: String)

data class WalkInResponse(
    val success: Boolean,
    val message: String? = null,
    val queueNumber: Int? = null,
    val appointmentId: Int? = null
)

// ------------------------------------------------------------ consultation

/** Dates are yyyy-MM-dd; an empty string means "not set". */
data class ConsultationDto(
    val symptoms: String? = "",
    val bloodPressure: String? = "",
    val temperature: String? = "",
    val pulse: String? = "",
    val spo2: String? = "",
    val medicalHistory: String? = "",
    val allergies: String? = "",
    val diagnosis: String? = "",
    val treatment: String? = "",
    val investigations: String? = "",
    val notes: String? = "",
    val sickLeaveDays: String? = "",
    val followUpDate: String? = "",
    val status: String? = "Draft"
)

data class PrescriptionDto(
    val prescription_id: Int,
    val medication_name: String,
    val dosage: String?,
    val instructions: String?
)

data class ConsultationResponse(
    val success: Boolean,
    val message: String? = null,
    val appointment: AppointmentDto? = null,
    val consultation: ConsultationDto? = null,
    val prescriptions: List<PrescriptionDto> = emptyList()
)

data class SaveConsultationRequest(
    val appointmentId: Int,
    val symptoms: String,
    val bloodPressure: String,
    val temperature: String,
    val pulse: String,
    val spo2: String,
    val medicalHistory: String,
    val allergies: String,
    val diagnosis: String,
    val treatment: String,
    val investigations: String,
    val notes: String,
    val sickLeaveDays: String,
    val followUpDate: String,
    @SerializedName("finalize") val finalizeNow: Boolean
)

data class SaveConsultationResponse(
    val success: Boolean,
    val message: String? = null,
    val followUpMessage: String? = null
)

data class PrescriptionRequest(
    val appointmentId: Int,
    val medication: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String
)

data class PrescriptionsResponse(
    val success: Boolean,
    val message: String? = null,
    val prescriptions: List<PrescriptionDto> = emptyList()
)

data class FollowUpRequest(val appointmentId: Int, val date: String, val reason: String)

// ---------------------------------------------------------- repeat requests

data class RepeatRequestDto(
    val request_id: Int,
    val medication_name: String,
    val dosage: String?,
    val request_date: String?,
    val patient_first_name: String,
    val patient_last_name: String
) {
    val patientFullName: String get() = "$patient_first_name $patient_last_name".trim()
}

data class RepeatRequestsResponse(
    val success: Boolean,
    val message: String? = null,
    val count: Int = 0,
    val requests: List<RepeatRequestDto> = emptyList()
)

data class DecideRepeatRequest(val requestId: Int, val approve: Boolean)

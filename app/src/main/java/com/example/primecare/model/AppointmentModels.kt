package com.example.primecare.model

/**
 * One appointment as returned by every api/staff endpoint that lists
 * appointments. Field names match the JSON keys exactly.
 */
data class AppointmentDto(
    val appointment_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val reason: String,
    val status: String,
    val patient_first_name: String,
    val patient_last_name: String,
    val doctor_first_name: String?,
    val doctor_last_name: String?,
    val department_name: String?,
    val patient_id: Int = 0,
    val doctor_id: Int? = null,
    val queue_number: Int? = null,
    val is_walkin: Boolean = false,
    val is_followup: Boolean = false,
    val in_consultation: Boolean = false
) {
    val patientFullName: String get() = "$patient_first_name $patient_last_name".trim()
    val queueLabel: String? get() = queue_number?.let { "Q-%02d".format(it) }
    val doctorFullName: String? get() = doctor_first_name?.let { "Dr. $it $doctor_last_name" }
}

data class TodayAppointmentsResponse(
    val success: Boolean,
    val message: String? = null,
    val count: Int = 0,
    val appointments: List<AppointmentDto> = emptyList()
)

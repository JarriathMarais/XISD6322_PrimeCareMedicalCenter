package com.example.primecare.model

data class DoctorDto(
    val doctor_id: Int,
    val first_name: String,
    val last_name: String,
    val specialty: String?,
    val status: String,
    val department_name: String?
) {
    val fullName: String get() = "Dr. $first_name $last_name"
}

data class DoctorsListResponse(
    val success: Boolean,
    val message: String? = null,
    val count: Int = 0,
    val activeCount: Int = 0,
    val doctors: List<DoctorDto> = emptyList()
)

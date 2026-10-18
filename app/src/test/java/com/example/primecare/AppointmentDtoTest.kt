package com.example.primecare

import com.example.primecare.model.AppointmentDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppointmentDtoTest {

    private fun dto(queue: Int? = null, doctorFirst: String? = null, last: String = "Dube") = AppointmentDto(
        appointment_id = 1, appointment_date = "2026-03-05", appointment_time = "09:00:00",
        reason = "Check-up", status = "Booked", patient_first_name = "Naledi", patient_last_name = last,
        doctor_first_name = doctorFirst, doctor_last_name = doctorFirst?.let { "Moyo" },
        department_name = null, queue_number = queue
    )

    @Test fun queueLabelIsZeroPadded() {
        assertEquals("Q-04", dto(queue = 4).queueLabel)
        assertEquals("Q-12", dto(queue = 12).queueLabel)
    }

    @Test fun noQueueNumberMeansNoLabel() = assertNull(dto().queueLabel)

    @Test fun patientFullNameIsTrimmedWhenLastNameBlank() =
        assertEquals("Naledi", dto(last = "").patientFullName)

    @Test fun doctorNameHasTitleOrIsNull() {
        assertEquals("Dr. Tendai Moyo", dto(doctorFirst = "Tendai").doctorFullName)
        assertNull(dto().doctorFullName)
    }
}

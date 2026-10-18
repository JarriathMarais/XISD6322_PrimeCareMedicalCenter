package com.example.primecare

import com.example.primecare.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatUtilsTest {

    @Test fun displayDateToIso() = assertEquals("2026-03-05", FormatUtils.displayToIso("05/03/2026"))

    @Test fun invalidDisplayDateIsNull() {
        assertNull(FormatUtils.displayToIso("31/02/2026"))
        assertNull(FormatUtils.displayToIso("garbage"))
        assertNull(FormatUtils.displayToIso(""))
    }

    @Test fun isoToDisplay() {
        assertEquals("05/03/2026", FormatUtils.isoToDisplay("2026-03-05"))
        assertEquals("not a date", FormatUtils.isoToDisplay("not a date"))
    }

    @Test fun datePickerMonthIsZeroBased() =
        assertEquals("05/03/2026", FormatUtils.formatDisplayDate(2026, 2, 5))

    @Test fun formatsMySqlTimeWithAndWithoutSeconds() {
        assertTrue(FormatUtils.formatTime("09:30:00").startsWith("9:30"))
        assertTrue(FormatUtils.formatTime("14:05").startsWith("2:05"))
    }

    @Test fun unparseableTimeIsReturnedAsIs() = assertEquals("soon", FormatUtils.formatTime("soon"))

    @Test fun statusLabels() {
        assertEquals("In consultation", FormatUtils.statusLabel("CheckedIn", true))
        assertEquals("Checked in", FormatUtils.statusLabel("CheckedIn", false))
        assertEquals("Booked", FormatUtils.statusLabel("Booked", false))
    }
}

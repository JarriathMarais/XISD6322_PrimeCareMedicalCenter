package com.example.primecare

import com.example.primecare.data.local.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppSettingsTest {

    @Test fun addsTrailingSlash() =
        assertEquals("http://192.168.1.50/primecarewebsite/",
            AppSettings.normalizeServerUrl("http://192.168.1.50/primecarewebsite"))

    @Test fun keepsExistingSlashAndTrims() =
        assertEquals("https://clinic.example.com/", AppSettings.normalizeServerUrl("  https://clinic.example.com/ "))

    @Test fun acceptsHostWithPort() =
        assertEquals("http://10.0.2.2:8080/", AppSettings.normalizeServerUrl("http://10.0.2.2:8080"))

    @Test fun rejectsMissingScheme() = assertNull(AppSettings.normalizeServerUrl("192.168.1.50/primecare"))

    @Test fun rejectsOtherSchemes() = assertNull(AppSettings.normalizeServerUrl("ftp://example.com/"))

    @Test fun rejectsSpacesAndBlank() {
        assertNull(AppSettings.normalizeServerUrl("http://exa mple.com/"))
        assertNull(AppSettings.normalizeServerUrl("   "))
    }
}

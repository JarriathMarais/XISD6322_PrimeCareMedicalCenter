package com.example.primecare

import com.example.primecare.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    @Test fun validEmailPasses() = assertNull(Validators.emailError("dr.moyo@primecare.com"))

    @Test fun blankEmailRejected() = assertNotNull(Validators.emailError("   "))

    @Test fun malformedEmailRejected() {
        assertNotNull(Validators.emailError("not-an-email"))
        assertNotNull(Validators.emailError("a@b"))
        assertNotNull(Validators.emailError("a b@c.com"))
    }

    @Test fun shortPasswordRejected() = assertNotNull(Validators.passwordError("abc123"))

    @Test fun passwordNeedsLetterAndNumber() {
        assertNotNull(Validators.passwordError("onlyletters"))
        assertNotNull(Validators.passwordError("12345678"))
        assertNull(Validators.passwordError("goodpass1"))
    }

    @Test fun confirmMustMatch() {
        assertNull(Validators.confirmError("abc12345", "abc12345"))
        assertEquals("Passwords do not match", Validators.confirmError("abc12345", "abc12346"))
    }

    @Test fun nameRules() {
        assertNull(Validators.nameError("Tendai", "first name"))
        assertNotNull(Validators.nameError("  ", "first name"))
        assertNotNull(Validators.nameError("x".repeat(51), "first name"))
    }
}

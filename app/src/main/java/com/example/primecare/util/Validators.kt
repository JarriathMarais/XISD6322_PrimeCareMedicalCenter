package com.example.primecare.util

/**
 * Input validation shared by Register, Settings and Login. Every rule
 * returns a user-readable message, or null when the input is fine.
 * The password rule is identical to the server's (password_error()).
 */
object Validators {

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nameError(value: String, label: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "Enter your $label"
            v.length > 50 -> "$label is too long (max 50 characters)"
            else -> null
        }
    }

    fun emailError(value: String): String? {
        val v = value.trim()
        return when {
            v.isEmpty() -> "Enter your email address"
            !EMAIL.matches(v) -> "Enter a valid email address"
            else -> null
        }
    }

    fun passwordError(value: String): String? = when {
        value.length < 8 -> "Password must be at least 8 characters"
        !value.any { it.isLetter() } || !value.any { it.isDigit() } ->
            "Password needs at least one letter and one number"
        else -> null
    }

    fun confirmError(password: String, confirm: String): String? =
        if (password != confirm) "Passwords do not match" else null
}

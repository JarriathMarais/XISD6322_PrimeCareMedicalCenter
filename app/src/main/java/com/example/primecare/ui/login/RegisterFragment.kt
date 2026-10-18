package com.example.primecare.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.primecare.R
import com.example.primecare.data.local.SessionManager
import com.example.primecare.data.repository.AuthRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentRegisterBinding
import com.example.primecare.model.RegisterRequest
import com.example.primecare.ui.common.BaseFragment
import com.example.primecare.util.Validators
import kotlinx.coroutines.launch

/**
 * Staff registration. Every field is validated on the device first (same
 * rules as the server), so bad input never leaves the phone; the server
 * stores only a bcrypt hash of the password and checks the clinic code.
 */
class RegisterFragment : BaseFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToLoginButton.setOnClickListener { findNavController().popBackStack() }
        binding.registerButton.setOnClickListener { submit() }
    }

    private fun text(input: com.google.android.material.textfield.TextInputEditText) =
        input.text?.toString().orEmpty()

    private fun submit() {
        val first = text(binding.firstNameInput).trim()
        val last = text(binding.lastNameInput).trim()
        val email = text(binding.emailInput).trim()
        val password = text(binding.passwordInput)
        val confirm = text(binding.confirmInput)
        val code = text(binding.codeInput).trim()

        binding.firstNameInputLayout.error = Validators.nameError(first, "first name")
        binding.lastNameInputLayout.error = Validators.nameError(last, "last name")
        binding.emailInputLayout.error = Validators.emailError(email)
        binding.passwordInputLayout.error = Validators.passwordError(password)
        binding.confirmInputLayout.error = Validators.confirmError(password, confirm)
        binding.codeInputLayout.error = if (code.isEmpty()) "Enter the clinic registration code" else null
        binding.registerError.visibility = View.GONE

        val hasError = listOf(
            binding.firstNameInputLayout, binding.lastNameInputLayout, binding.emailInputLayout,
            binding.passwordInputLayout, binding.confirmInputLayout, binding.codeInputLayout
        ).any { it.error != null }
        if (hasError) return

        val role = if (binding.roleGroup.checkedRadioButtonId == R.id.roleAdmin) "ADMIN" else "DOCTOR"
        val repository = AuthRepository(SessionManager(requireContext()))

        binding.registerButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.register(RegisterRequest(first, last, email, password, role, code))) {
                is DataResult.Success -> {
                    Toast.makeText(requireContext(), result.data.message ?: "Account created.", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
                is DataResult.Error -> {
                    binding.registerButton.isEnabled = true
                    binding.registerError.text = result.message
                    binding.registerError.visibility = View.VISIBLE
                }
            }
        }
    }
}

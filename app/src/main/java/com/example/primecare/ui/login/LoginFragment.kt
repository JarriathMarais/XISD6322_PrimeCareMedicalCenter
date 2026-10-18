package com.example.primecare.ui.login

import android.os.Bundle
import android.view.View
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.primecare.AppConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import com.example.primecare.R
import com.example.primecare.data.local.SessionManager
import com.example.primecare.data.local.StaffRole
import com.example.primecare.data.repository.AuthRepository
import com.example.primecare.databinding.FragmentLoginBinding
import com.example.primecare.ui.common.BaseFragment


class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(AuthRepository(SessionManager(requireContext())))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString()?.trim().orEmpty()
            val password = binding.passwordInput.text?.toString().orEmpty()
            viewModel.login(email, password)
        }

        binding.googleButton.setOnClickListener { signInWithGoogle() }
        binding.registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        binding.serverSettingsLink.setOnClickListener {
            findNavController().navigate(R.id.action_global_settings)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginUiState.Idle -> setLoading(false)

                is LoginUiState.Loading -> {
                    setLoading(true)
                    binding.errorText.visibility = View.GONE
                }

                is LoginUiState.Success -> {
                    setLoading(false)
                    navigateToDashboard(state.role)
                }

                is LoginUiState.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loginProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !isLoading
        binding.googleButton.isEnabled = !isLoading
    }

    /**
     * Single sign-on. Google's Credential Manager shows the account picker
     * and returns an ID token, which the server verifies with Google before
     * issuing a PrimeCare session. Only existing staff emails are accepted.
     */
    private fun signInWithGoogle() {
        if (AppConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            showError("Google sign-in is not configured in this build yet (AppConfig.GOOGLE_WEB_CLIENT_ID).")
            return
        }
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(AppConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = CredentialManager.create(requireContext())
                    .getCredential(requireActivity(), request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    viewModel.googleLogin(token)
                } else {
                    showError("Google sign-in returned an unexpected result.")
                }
            } catch (e: GetCredentialCancellationException) {
                // user closed the picker — nothing to report
            } catch (e: GetCredentialException) {
                showError("Google sign-in failed: ${e.message ?: "please try again."}")
            }
        }
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun navigateToDashboard(role: StaffRole) {
        val action = when (role) {
            StaffRole.ADMIN -> R.id.action_login_to_adminDashboard
            StaffRole.DOCTOR -> R.id.action_login_to_doctorDashboard
        }
        findNavController().navigate(action)
    }
}

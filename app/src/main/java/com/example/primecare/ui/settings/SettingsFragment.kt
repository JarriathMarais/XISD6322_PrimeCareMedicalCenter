package com.example.primecare.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.primecare.R
import com.example.primecare.data.api.ApiClient
import com.example.primecare.data.local.AppSettings
import com.example.primecare.data.local.SessionManager
import com.example.primecare.data.repository.AuthRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentSettingsBinding
import com.example.primecare.ui.common.BaseFragment
import com.example.primecare.util.Validators
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Settings: profile, password, server address and preferences. Also
 * reachable from the login screen (before sign-in), where only the
 * server address and help are shown.
 */
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private lateinit var session: SessionManager
    private lateinit var auth: AuthRepository

    private fun text(input: com.google.android.material.textfield.TextInputEditText) =
        input.text?.toString().orEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to fix unresolved reference R
        binding.topBar.tvTitle.text = "Settings"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        session = SessionManager(requireContext())
        auth = AuthRepository(session)
        val loggedIn = session.isLoggedIn()

        binding.profileCard.visibility = if (loggedIn) View.VISIBLE else View.GONE
        binding.passwordCard.visibility = if (loggedIn) View.VISIBLE else View.GONE
        binding.signOutButton.visibility = if (loggedIn) View.VISIBLE else View.GONE
        binding.versionText.text = "PrimeCare Staff v1.0"

        binding.serverUrlInput.setText(AppSettings.getServerUrl(requireContext()))
        binding.autoRefreshSwitch.isChecked = AppSettings.isAutoRefreshEnabled(requireContext())
        binding.autoRefreshSwitch.setOnCheckedChangeListener { _, checked ->
            AppSettings.setAutoRefreshEnabled(requireContext(), checked)
        }

        binding.saveServerButton.setOnClickListener { saveServer(showMessage = true) }
        binding.testServerButton.setOnClickListener { testServer() }
        binding.openHelpButton.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_help)
        }
        binding.signOutButton.setOnClickListener {
            auth.logout()
            findNavController().navigate(R.id.action_settings_to_login)
        }

        if (loggedIn) {
            binding.saveProfileButton.setOnClickListener { saveProfile() }
            binding.changePasswordButton.setOnClickListener { changePassword() }
            loadProfile()
        }
    }

    // ---- server address ----

    /** Validates and stores the address. Returns true if it was saved. */
    private fun saveServer(showMessage: Boolean): Boolean {
        val normalized = AppSettings.normalizeServerUrl(text(binding.serverUrlInput))
        if (normalized == null) {
            binding.serverUrlInputLayout.error = "Enter a full address, e.g. http://192.168.1.50/primecarewebsite/"
            return false
        }
        binding.serverUrlInputLayout.error = null
        binding.serverUrlInput.setText(normalized)
        AppSettings.setServerUrl(requireContext(), normalized)
        ApiClient.resetBaseUrl()
        if (showMessage) Snackbar.make(binding.root, "Server address saved", Snackbar.LENGTH_SHORT).show()
        return true
    }

    private fun testServer() {
        if (!saveServer(showMessage = false)) return
        binding.serverStatusText.text = "Testing…"
        binding.testServerButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = auth.ping()) {
                is DataResult.Success -> binding.serverStatusText.text = "Connected ✓ — server is reachable"
                is DataResult.Error -> binding.serverStatusText.text = "Not connected: ${result.message}"
            }
            binding.testServerButton.isEnabled = true
        }
    }

    // ---- profile ----

    private fun loadProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = auth.loadProfile()) {
                is DataResult.Success -> {
                    binding.settingsFirstName.setText(result.data.firstName.orEmpty())
                    binding.settingsLastName.setText(result.data.lastName.orEmpty())
                    binding.profileEmailText.text =
                        "${result.data.email.orEmpty()} · ${result.data.role.orEmpty().lowercase()}"
                }
                is DataResult.Error ->
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun saveProfile() {
        val first = text(binding.settingsFirstName).trim()
        val last = text(binding.settingsLastName).trim()
        binding.settingsFirstNameLayout.error = Validators.nameError(first, "first name")
        binding.settingsLastNameLayout.error = Validators.nameError(last, "last name")
        if (binding.settingsFirstNameLayout.error != null || binding.settingsLastNameLayout.error != null) return

        binding.saveProfileButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            val message = when (val result = auth.updateProfile(first, last)) {
                is DataResult.Success -> result.data.message ?: "Profile updated"
                is DataResult.Error -> result.message
            }
            binding.saveProfileButton.isEnabled = true
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    // ---- password ----

    private fun changePassword() {
        val current = text(binding.currentPassword)
        val new = text(binding.newPassword)
        val confirm = text(binding.confirmNewPassword)

        binding.currentPasswordLayout.error = if (current.isEmpty()) "Enter your current password" else null
        binding.newPasswordLayout.error = Validators.passwordError(new)
        binding.confirmNewPasswordLayout.error = Validators.confirmError(new, confirm)
        if (listOf(binding.currentPasswordLayout, binding.newPasswordLayout, binding.confirmNewPasswordLayout)
                .any { it.error != null }) return

        binding.changePasswordButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = auth.changePassword(current, new)) {
                is DataResult.Success -> {
                    binding.currentPassword.setText("")
                    binding.newPassword.setText("")
                    binding.confirmNewPassword.setText("")
                    Toast.makeText(requireContext(), result.data.message ?: "Password updated", Toast.LENGTH_LONG).show()
                }
                is DataResult.Error ->
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
            }
            binding.changePasswordButton.isEnabled = true
        }
    }
}

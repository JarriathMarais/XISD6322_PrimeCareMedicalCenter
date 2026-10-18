package com.example.primecare.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.primecare.R
import com.example.primecare.data.local.SessionManager
import com.example.primecare.data.repository.AuthRepository
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.databinding.FragmentAdminDashboardBinding
import com.example.primecare.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar

/**
 * PrimeCare — Admin Dashboard
 * ---------------------------------------------------------
 * Displays key metrics and quick actions for clinic administrators.
 */
class AdminDashboardFragment :
    BaseFragment<FragmentAdminDashboardBinding>(FragmentAdminDashboardBinding::inflate) {

    private val viewModel: AdminDashboardViewModel by viewModels {
        AdminDashboardViewModel.Factory(ClinicRepository())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use binding to access top bar and other views
        binding.topBar.tvTitle.text = "Admin Dashboard"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_login)
        }

        binding.statTodaysAppointments.statLabel.text = getString(R.string.stat_todays_appointments)
        binding.statPatientsWaiting.statLabel.text = getString(R.string.stat_patients_waiting)
        binding.statWalkinsToday.statLabel.text = getString(R.string.stat_walkins_today)
        binding.statAvailableDoctors.statLabel.text = getString(R.string.stat_available_doctors)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AdminDashboardUiState.Loading -> {}
                is AdminDashboardUiState.Loaded -> {
                    binding.statTodaysAppointments.statValue.text = state.stats.todaysAppointments.toString()
                    binding.statPatientsWaiting.statValue.text = state.stats.patientsWaiting.toString()
                    binding.statWalkinsToday.statValue.text = state.stats.walkinsToday.toString()
                    binding.statAvailableDoctors.statValue.text = state.stats.availableDoctors.toString()
                }
                is AdminDashboardUiState.Error -> {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.btnViewAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_appointmentsList)
        }
        binding.btnCheckin.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_checkin)
        }
        binding.btnQueue.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_queue)
        }
        binding.btnAssignDoctor.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_assignDoctor)
        }
        binding.btnRegisterWalkin.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboard_to_registerWalkin)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_global_settings)
        }
        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.action_global_help)
        }
        binding.btnLogout.setOnClickListener {
            AuthRepository(SessionManager(requireContext())).logout()
            findNavController().navigate(R.id.action_adminDashboard_to_login)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStats()
    }
}

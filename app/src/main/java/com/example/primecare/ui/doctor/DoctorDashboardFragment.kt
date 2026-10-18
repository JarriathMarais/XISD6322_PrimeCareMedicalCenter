package com.example.primecare.ui.doctor

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primecare.R
import com.example.primecare.data.local.SessionManager
import com.example.primecare.data.repository.AuthRepository
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentDoctorDashboardBinding
import com.example.primecare.model.AppointmentDto
import com.example.primecare.ui.common.BaseFragment
import com.example.primecare.util.NavArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * PrimeCare — Doctor Dashboard
 * Live counters + the doctor's own queue. Everything is scoped to the
 * logged-in doctor by the server (it reads the doctor from the token).
 */
class DoctorDashboardFragment :
    BaseFragment<FragmentDoctorDashboardBinding>(FragmentDoctorDashboardBinding::inflate) {

    private val clinic = ClinicRepository()
    private lateinit var queueAdapter: DoctorQueueAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to avoid unresolved reference R
        binding.topBar.tvTitle.text = "Doctor Dashboard"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.statAssignedPatients.statLabel.text = getString(R.string.stat_assigned_patients)
        binding.statWaitingPatients.statLabel.text = getString(R.string.stat_waiting_patients)
        binding.statCompletedToday.statLabel.text = getString(R.string.stat_completed_today)
        binding.statFollowups.statLabel.text = getString(R.string.stat_followups)

        queueAdapter = DoctorQueueAdapter { openConsultation(it) }
        binding.queueRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.queueRecycler.adapter = queueAdapter

        binding.btnStartConsultation.setOnClickListener { callNextPatient() }
        binding.btnRepeatRequests.setOnClickListener {
            findNavController().navigate(R.id.action_doctorDashboard_to_repeatRequests)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_global_settings)
        }
        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.action_global_help)
        }
        binding.btnLogout.setOnClickListener {
            AuthRepository(SessionManager(requireContext())).logout()
            findNavController().navigate(R.id.action_doctorDashboard_to_login)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val stats = clinic.doctorStats()) {
                is DataResult.Success -> {
                    binding.statAssignedPatients.statValue.text = stats.data.assignedPatients.toString()
                    binding.statWaitingPatients.statValue.text = stats.data.waitingPatients.toString()
                    binding.statCompletedToday.statValue.text = stats.data.completedToday.toString()
                    binding.statFollowups.statValue.text = stats.data.followups.toString()
                }
                is DataResult.Error ->
                    Snackbar.make(binding.root, stats.message, Snackbar.LENGTH_LONG).show()
            }

            when (val queue = clinic.queue()) {
                is DataResult.Success -> {
                    val all = queue.data.nowServing + queue.data.waiting
                    queueAdapter.submitList(all)
                    binding.queueEmpty.visibility = if (all.isEmpty()) View.VISIBLE else View.GONE
                }
                is DataResult.Error -> { /* the stats call above already surfaced the error */ }
            }
        }
    }

    /** Resumes the patient already in consultation, otherwise the next one waiting. */
    private fun callNextPatient() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val queue = clinic.queue()) {
                is DataResult.Success -> {
                    val next = queue.data.nowServing.firstOrNull() ?: queue.data.waiting.firstOrNull()
                    if (next == null) {
                        Snackbar.make(binding.root, "No patients are waiting for you right now.", Snackbar.LENGTH_LONG).show()
                    } else {
                        openConsultation(next)
                    }
                }
                is DataResult.Error ->
                    Snackbar.make(binding.root, queue.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun openConsultation(appointment: AppointmentDto) {
        findNavController().navigate(
            R.id.action_doctorDashboard_to_consultation,
            bundleOf(
                NavArgs.APPOINTMENT_ID to appointment.appointment_id,
                NavArgs.PATIENT_NAME to appointment.patientFullName
            )
        )
    }
}

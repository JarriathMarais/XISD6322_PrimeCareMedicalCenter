package com.example.primecare.ui.admin

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primecare.data.repository.AppointmentRepository
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.data.repository.DoctorRepository
import com.example.primecare.databinding.FragmentAssignDoctorBinding
import com.example.primecare.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

/**
 * Lists today's open appointments that have no doctor yet (website
 * bookings arrive unassigned) and lets reception assign an active doctor.
 */
class AssignDoctorFragment : BaseFragment<FragmentAssignDoctorBinding>(FragmentAssignDoctorBinding::inflate) {

    private val appointments = AppointmentRepository()
    private val doctors = DoctorRepository()
    private val clinic = ClinicRepository()
    private lateinit var adapter: AssignDoctorAdapter
    private var busy = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding to fix "Unresolved reference R" for top bar
        binding.topBar.tvTitle.text = "Assign Doctor"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = AssignDoctorAdapter { appointment, doctor ->
            if (busy) return@AssignDoctorAdapter
            busy = true
            viewLifecycleOwner.lifecycleScope.launch {
                val message = when (val r = clinic.assignDoctor(appointment.appointment_id, doctor.doctor_id)) {
                    is DataResult.Success -> "${appointment.patientFullName} assigned to ${doctor.fullName}"
                    is DataResult.Error -> r.message
                }
                busy = false
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                load()
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        load()
    }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            val doctorResult = doctors.getDoctors()
            val apptResult = appointments.getTodayAppointments(
                status = "Booked,CheckedIn", unassignedOnly = true
            )

            if (doctorResult is DataResult.Error) {
                Snackbar.make(binding.root, doctorResult.message, Snackbar.LENGTH_LONG).show()
                return@launch
            }
            if (apptResult is DataResult.Error) {
                Snackbar.make(binding.root, apptResult.message, Snackbar.LENGTH_LONG).show()
                return@launch
            }

            val active = (doctorResult as DataResult.Success).data.filter { it.status == "Active" }
            val items = (apptResult as DataResult.Success).data

            adapter.doctors = active
            adapter.submitList(items) { adapter.notifyDataSetChanged() }

            val empty = items.isEmpty()
            binding.emptyView.visibility = if (empty) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
            if (!empty && active.isEmpty()) {
                Snackbar.make(binding.root, "No doctors are currently active.", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}

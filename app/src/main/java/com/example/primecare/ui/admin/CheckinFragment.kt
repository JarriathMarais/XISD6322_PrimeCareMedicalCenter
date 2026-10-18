package com.example.primecare.ui.admin

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primecare.data.repository.AppointmentRepository
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentCheckinBinding
import com.example.primecare.model.AppointmentDto
import com.example.primecare.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

/**
 * Reception check-in: search today's Booked appointments by patient
 * name and check them in (the server assigns the queue number).
 */
class CheckinFragment : BaseFragment<FragmentCheckinBinding>(FragmentCheckinBinding::inflate) {

    private val appointments = AppointmentRepository()
    private val clinic = ClinicRepository()
    private lateinit var adapter: CheckinAdapter
    private var loadJob: Job? = null
    private var busy = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to fix unresolved R reference
        binding.topBar.tvTitle.text = "Patient Check-in"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = CheckinAdapter { checkIn(it) }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchInput.doAfterTextChanged { load(debounce = true) }
        load(debounce = false)
    }

    private fun currentQuery() = binding.searchInput.text?.toString().orEmpty().trim()

    private fun load(debounce: Boolean) {
        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            if (debounce) delay(300)
            when (val result = appointments.getTodayAppointments(
                query = currentQuery(), status = "Booked,CheckedIn"
            )) {
                is DataResult.Success -> show(result.data)
                is DataResult.Error -> Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun show(items: List<AppointmentDto>) {
        adapter.submitList(items)
        val empty = items.isEmpty()
        binding.emptyView.visibility = if (empty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun checkIn(appointment: AppointmentDto) {
        if (busy) return
        busy = true
        viewLifecycleOwner.lifecycleScope.launch {
            val message = when (val result = clinic.checkIn(appointment.appointment_id)) {
                is DataResult.Success -> {
                    val queue = result.data.queueNumber?.let { " · Q-%02d".format(it) }.orEmpty()
                    "${appointment.patientFullName} checked in$queue"
                }
                is DataResult.Error -> result.message
            }
            busy = false
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            load(debounce = false)
        }
    }
}

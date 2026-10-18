package com.example.primecare.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primecare.data.repository.AppointmentRepository
import com.example.primecare.databinding.FragmentAppointmentsListBinding
import com.example.primecare.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar

/**
 * PrimeCare — Today's Appointments (Admin)
 * ---------------------------------------------------------
 * The real destination behind the Admin Dashboard's "View
 * Appointments" quick action. Shows every appointment for today —
 * proper loading / empty / error states, pull-to-refresh, and
 * genuinely reflects whatever a patient booked on the website.
 */
class AppointmentsListFragment :
    BaseFragment<FragmentAppointmentsListBinding>(FragmentAppointmentsListBinding::inflate) {

    private val viewModel: AppointmentsListViewModel by viewModels {
        AppointmentsListViewModel.Factory(AppointmentRepository())
    }

    private val adapter = AppointmentsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Top Bar via ViewBinding (fixes Unresolved reference R)
        binding.topBar.tvTitle.text = "Today's Appointments"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is AppointmentsListUiState.Loading -> {
                    binding.loadingIndicator.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                }
                is AppointmentsListUiState.Loaded -> {
                    binding.loadingIndicator.visibility = View.GONE
                    adapter.submitList(state.appointments)
                    val isEmpty = state.appointments.isEmpty()
                    binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                    binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
                }
                is AppointmentsListUiState.Error -> {
                    binding.loadingIndicator.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.emptyView.visibility = View.GONE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                        .setAction("Retry") { viewModel.load() }
                        .show()
                }
            }
        }
    }
}

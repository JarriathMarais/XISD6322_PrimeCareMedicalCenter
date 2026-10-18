package com.example.primecare.ui.admin

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primecare.data.local.AppSettings
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentQueueBinding
import com.example.primecare.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

/**
 * Live queue: who is currently in consultation and who is waiting.
 * Refreshes every few seconds while the screen is visible.
 */
class QueueFragment : BaseFragment<FragmentQueueBinding>(FragmentQueueBinding::inflate) {

    private val clinic = ClinicRepository()
    private val adapter = QueueAdapter()
    private var errorShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to fix unresolved R reference
        binding.topBar.tvTitle.text = "Queue Management"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                refresh()
                while (AppSettings.isAutoRefreshEnabled(requireContext())) {
                    delay(REFRESH_MS)
                    refresh()
                }
            }
        }
    }

    private suspend fun refresh() {
        when (val result = clinic.queue()) {
            is DataResult.Success -> {
                errorShown = false
                val serving = result.data.nowServing.firstOrNull()
                if (serving == null) {
                    binding.nowServingCard.visibility = View.GONE
                    binding.nowServingEmpty.visibility = View.VISIBLE
                } else {
                    binding.nowServingCard.visibility = View.VISIBLE
                    binding.nowServingEmpty.visibility = View.GONE
                    binding.nowServingNumber.text = serving.queueLabel ?: "—"
                    binding.nowServingName.text = serving.patientFullName
                    binding.nowServingDoctor.text =
                        serving.doctorFullName?.let { "With $it" } ?: "Doctor not assigned"
                }
                adapter.submitList(result.data.waiting)
                binding.waitingEmpty.visibility =
                    if (result.data.waiting.isEmpty()) View.VISIBLE else View.GONE
            }
            is DataResult.Error -> if (!errorShown) {
                errorShown = true   // don't spam a snackbar every refresh
                Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private companion object {
        const val REFRESH_MS = 5_000L
    }
}

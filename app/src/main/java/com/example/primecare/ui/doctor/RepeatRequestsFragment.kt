package com.example.primecare.ui.doctor

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentRepeatRequestsBinding
import com.example.primecare.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

/** Repeat-prescription requests patients submitted on the website. */
class RepeatRequestsFragment : BaseFragment<FragmentRepeatRequestsBinding>(FragmentRepeatRequestsBinding::inflate) {

    private val clinic = ClinicRepository()
    private lateinit var adapter: RepeatRequestsAdapter
    private var busy = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to fix unresolved reference R
        binding.topBar.tvTitle.text = "Repeat Request"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = RepeatRequestsAdapter { request, approve ->
            if (busy) return@RepeatRequestsAdapter
            busy = true
            viewLifecycleOwner.lifecycleScope.launch {
                val message = when (val r = clinic.decideRepeatRequest(request.request_id, approve)) {
                    is DataResult.Success -> if (approve)
                        "${request.medication_name} approved for ${request.patientFullName}"
                    else
                        "${request.medication_name} declined for ${request.patientFullName}"
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
            when (val result = clinic.repeatRequests()) {
                is DataResult.Success -> {
                    adapter.submitList(result.data)
                    val empty = result.data.isEmpty()
                    binding.emptyView.visibility = if (empty) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
                }
                is DataResult.Error ->
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}

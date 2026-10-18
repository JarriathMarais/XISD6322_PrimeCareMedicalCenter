package com.example.primecare.ui.doctor

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentFollowupBinding
import com.example.primecare.ui.common.BaseFragment
import com.example.primecare.util.FormatUtils
import com.example.primecare.util.NavArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class FollowupFragment : BaseFragment<FragmentFollowupBinding>(FragmentFollowupBinding::inflate) {

    private val clinic = ClinicRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to fix unresolved reference R
        binding.topBar.tvTitle.text = "Schedule Follow-up"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val appointmentId = arguments?.getInt(NavArgs.APPOINTMENT_ID, -1) ?: -1
        val patientName = arguments?.getString(NavArgs.PATIENT_NAME).orEmpty()
        binding.followupPatientText.text = patientName.ifBlank { "Patient" }

        binding.dateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val dialog = DatePickerDialog(requireContext(), { _, year, month, day ->
                binding.dateInput.setText(FormatUtils.formatDisplayDate(year, month, day))
                binding.dateInput.error = null
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dialog.datePicker.minDate = System.currentTimeMillis() - 1000
            dialog.show()
        }

        binding.scheduleButton.setOnClickListener {
            val iso = FormatUtils.displayToIso(binding.dateInput.text?.toString().orEmpty())
            if (iso == null) {
                binding.dateInput.error = "Choose a follow-up date"
                return@setOnClickListener
            }
            val reason = binding.reasonInput.text?.toString().orEmpty().trim()

            binding.scheduleButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = clinic.createFollowUp(appointmentId, iso, reason)) {
                    is DataResult.Success -> {
                        Toast.makeText(requireContext(), result.data.message ?: "Follow-up scheduled", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    is DataResult.Error -> {
                        binding.scheduleButton.isEnabled = true
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

package com.example.primecare.ui.doctor

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentPrescriptionBinding
import com.example.primecare.model.PrescriptionRequest
import com.example.primecare.ui.common.BaseFragment
import com.example.primecare.util.NavArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PrescriptionFragment : BaseFragment<FragmentPrescriptionBinding>(FragmentPrescriptionBinding::inflate) {

    private val clinic = ClinicRepository()
    private var saved = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to fix unresolved reference R
        binding.topBar.tvTitle.text = "Add prescription"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val appointmentId = arguments?.getInt(NavArgs.APPOINTMENT_ID, -1) ?: -1
        val patientName = arguments?.getString(NavArgs.PATIENT_NAME).orEmpty()
        binding.prescriptionPatientText.text = patientName.ifBlank { "Patient" }

        binding.savePrescriptionButton.setOnClickListener { submit(appointmentId, addAnother = false) }
        binding.saveAnotherButton.setOnClickListener { submit(appointmentId, addAnother = true) }
    }

    private fun text(input: com.google.android.material.textfield.TextInputEditText) =
        input.text?.toString().orEmpty().trim()

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.savePrescriptionButton.isEnabled = enabled
        binding.saveAnotherButton.isEnabled = enabled
    }

    private fun submit(appointmentId: Int, addAnother: Boolean) {
        val medication = text(binding.medicationInput)
        val dosage = text(binding.dosageInput)
        val frequency = text(binding.frequencyInput)

        var valid = true
        if (medication.isEmpty()) { binding.medicationInput.error = "Enter a medication name"; valid = false }
        if (dosage.isEmpty()) { binding.dosageInput.error = "Enter the dose"; valid = false }
        if (frequency.isEmpty()) { binding.frequencyInput.error = "Enter how often"; valid = false }
        if (!valid) return

        val request = PrescriptionRequest(
            appointmentId = appointmentId,
            medication = medication,
            dosage = dosage,
            frequency = frequency,
            duration = text(binding.durationInput),
            instructions = text(binding.instructionsInput)
        )

        setButtonsEnabled(false)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = clinic.createPrescription(request)) {
                is DataResult.Success -> {
                    saved++
                    if (addAnother) {
                        listOf(binding.medicationInput, binding.dosageInput, binding.frequencyInput,
                            binding.durationInput, binding.instructionsInput).forEach { it.setText("") }
                        binding.medicationInput.requestFocus()
                        setButtonsEnabled(true)
                        Snackbar.make(binding.root, "$medication saved ($saved this visit). Add the next one.", Snackbar.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Prescription saved", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
                is DataResult.Error -> {
                    setButtonsEnabled(true)
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}

package com.example.primecare.ui.doctor

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.primecare.R
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentConsultationBinding
import com.example.primecare.model.ConsultationResponse
import com.example.primecare.model.PrescriptionDto
import com.example.primecare.model.SaveConsultationRequest
import com.example.primecare.ui.common.BaseFragment
import com.example.primecare.util.FormatUtils
import com.example.primecare.util.NavArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * PrimeCare - Consultation
 * Opening this screen marks the patient "in consultation" on the server
 * and loads any saved draft. Save Draft keeps work in progress; Finalize
 * writes the patient's medical record, completes the appointment and
 * books the follow-up (if a date was chosen).
 */
class ConsultationFragment : BaseFragment<FragmentConsultationBinding>(FragmentConsultationBinding::inflate) {

    private val clinic = ClinicRepository()
    private var appointmentId = -1
    private var patientName = ""
    private var loaded = false
    private var prescriptionCount = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to avoid unresolved reference R
        binding.topBar.tvTitle.text = "Consultation"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        appointmentId = arguments?.getInt(NavArgs.APPOINTMENT_ID, -1) ?: -1
        patientName = arguments?.getString(NavArgs.PATIENT_NAME).orEmpty()
        if (appointmentId <= 0) {
            Toast.makeText(requireContext(), "No patient selected.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setActionsEnabled(false)

        binding.followUpDateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val dialog = DatePickerDialog(requireContext(), { _, year, month, day ->
                binding.followUpDateInput.setText(FormatUtils.formatDisplayDate(year, month, day))
                binding.followUpDateInput.error = null
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dialog.datePicker.minDate = System.currentTimeMillis() - 1000
            dialog.show()
        }

        binding.btnAddPrescription.setOnClickListener {
            findNavController().navigate(R.id.action_consultation_to_prescription, navArgs())
        }
        binding.diagnosisInput.doAfterTextChanged { binding.diagnosisInputLayout.error = null }
        binding.saveDraftButton.setOnClickListener { save(finalize = false) }
        binding.finalizeButton.setOnClickListener { confirmFinalize() }

        startConsultation(restoreFields = savedInstanceState == null)
    }

    override fun onResume() {
        super.onResume()
        if (loaded) refreshPrescriptions()   // a prescription may have just been added
    }

    private fun navArgs() = bundleOf(
        NavArgs.APPOINTMENT_ID to appointmentId,
        NavArgs.PATIENT_NAME to patientName
    )

    private fun startConsultation(restoreFields: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = clinic.startConsultation(appointmentId)) {
                is DataResult.Success -> {
                    bind(result.data, restoreFields)
                    loaded = true
                    setActionsEnabled(true)
                }
                is DataResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun bind(response: ConsultationResponse, restoreFields: Boolean) {
        val appt = response.appointment
        if (appt != null) {
            patientName = appt.patientFullName
            binding.patientHeaderText.text =
                "${appt.patientFullName} - ${appt.queueLabel ?: "Walk-in"} - ${appt.reason}"
        }

        // Don't overwrite what the doctor typed if the view is being recreated (e.g. rotation).
        val c = response.consultation
        if (restoreFields && c != null) {
            binding.symptomsInput.setText(c.symptoms.orEmpty())
            binding.bpInput.setText(c.bloodPressure.orEmpty())
            binding.tempInput.setText(c.temperature.orEmpty())
            binding.pulseInput.setText(c.pulse.orEmpty())
            binding.spo2Input.setText(c.spo2.orEmpty())
            binding.historyInput.setText(c.medicalHistory.orEmpty())
            binding.allergiesInput.setText(c.allergies.orEmpty())
            binding.diagnosisInput.setText(c.diagnosis.orEmpty())
            binding.treatmentInput.setText(c.treatment.orEmpty())
            binding.investigationsInput.setText(c.investigations.orEmpty())
            binding.notesInput.setText(c.notes.orEmpty())
            binding.sickLeaveInput.setText(c.sickLeaveDays.orEmpty())
            val followUp = c.followUpDate.orEmpty()
            binding.followUpDateInput.setText(if (followUp.isBlank()) "" else FormatUtils.isoToDisplay(followUp))
        }
        showPrescriptions(response.prescriptions)
    }

    private fun refreshPrescriptions() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = clinic.prescriptions(appointmentId)
            if (result is DataResult.Success) showPrescriptions(result.data)
        }
    }

    private fun showPrescriptions(items: List<PrescriptionDto>) {
        prescriptionCount = items.size
        binding.prescriptionSummaryText.text = if (items.isEmpty()) {
            "No prescriptions added yet."
        } else {
            items.joinToString("\n") { item ->
                val dose = item.dosage?.takeIf { it.isNotBlank() }?.let { " - $it" }.orEmpty()
                "\u2022 ${item.medication_name}$dose"
            }
        }
    }

    private fun text(input: com.google.android.material.textfield.TextInputEditText) =
        input.text?.toString().orEmpty().trim()

    private fun buildRequest(finalize: Boolean): SaveConsultationRequest? {
        val diagnosis = text(binding.diagnosisInput)
        if (finalize && diagnosis.isEmpty()) {
            binding.diagnosisInputLayout.error = "A diagnosis is required to finalize"
            binding.diagnosisInput.requestFocus()
            return null
        }

        val followUpDisplay = text(binding.followUpDateInput)
        var followUpIso = ""
        if (followUpDisplay.isNotEmpty()) {
            val iso = FormatUtils.displayToIso(followUpDisplay)
            if (iso == null) {
                binding.followUpDateInput.error = "Choose a valid date"
                return null
            }
            followUpIso = iso
        }

        return SaveConsultationRequest(
            appointmentId = appointmentId,
            symptoms = text(binding.symptomsInput),
            bloodPressure = text(binding.bpInput),
            temperature = text(binding.tempInput),
            pulse = text(binding.pulseInput),
            spo2 = text(binding.spo2Input),
            medicalHistory = text(binding.historyInput),
            allergies = text(binding.allergiesInput),
            diagnosis = diagnosis,
            treatment = text(binding.treatmentInput),
            investigations = text(binding.investigationsInput),
            notes = text(binding.notesInput),
            sickLeaveDays = text(binding.sickLeaveInput),
            followUpDate = followUpIso,
            finalizeNow = finalize
        )
    }

    /** Finalizing can't be undone, so ask first and say what will be sent. */
    private fun confirmFinalize() {
        if (buildRequest(finalize = true) == null) return   // shows the diagnosis error
        val rx = if (prescriptionCount == 0) "No prescriptions were added." else "$prescriptionCount prescription(s) will be sent."
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Finalize consultation?")
            .setMessage("This completes the visit and adds it to the patient's records. $rx You cannot edit it afterwards.")
            .setPositiveButton("Finalize") { _, _ -> save(finalize = true) }
            .setNegativeButton("Keep editing", null)
            .show()
    }

    private fun save(finalize: Boolean) {
        val request = buildRequest(finalize) ?: return
        setActionsEnabled(false)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = clinic.saveConsultation(request)) {
                is DataResult.Success -> {
                    if (finalize) {
                        val message = listOfNotNull("Consultation finalized.", result.data.followUpMessage)
                            .joinToString("\n")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    } else {
                        Snackbar.make(binding.root, "Draft saved", Snackbar.LENGTH_SHORT).show()
                        setActionsEnabled(true)
                    }
                }
                is DataResult.Error -> {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    setActionsEnabled(true)
                }
            }
        }
    }

    private fun setActionsEnabled(enabled: Boolean) {
        binding.saveDraftButton.isEnabled = enabled
        binding.finalizeButton.isEnabled = enabled
        binding.btnAddPrescription.isEnabled = enabled
    }
}

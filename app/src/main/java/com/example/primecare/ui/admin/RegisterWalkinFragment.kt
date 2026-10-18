package com.example.primecare.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.primecare.data.repository.ClinicRepository
import com.example.primecare.data.repository.DataResult
import com.example.primecare.databinding.FragmentRegisterWalkinBinding
import com.example.primecare.ui.common.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterWalkinFragment : BaseFragment<FragmentRegisterWalkinBinding>(FragmentRegisterWalkinBinding::inflate) {

    private val clinic = ClinicRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Use ViewBinding for the top bar to fix unresolved R reference
        binding.topBar.tvTitle.text = "Register Walk-in"
        binding.topBar.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text?.toString().orEmpty().trim()
            val phone = binding.phoneInput.text?.toString().orEmpty().trim()
            val reason = binding.reasonInput.text?.toString().orEmpty().trim()

            if (name.isBlank()) {
                binding.nameInput.error = "Enter the patient's name"
                return@setOnClickListener
            }

            binding.registerButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                when (val result = clinic.registerWalkIn(name, phone, reason)) {
                    is DataResult.Success -> {
                        val queue = result.data.queueNumber?.let { " Queue number Q-%02d.".format(it) }.orEmpty()
                        Toast.makeText(requireContext(), "$name registered.$queue", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    is DataResult.Error -> {
                        binding.registerButton.isEnabled = true
                        Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

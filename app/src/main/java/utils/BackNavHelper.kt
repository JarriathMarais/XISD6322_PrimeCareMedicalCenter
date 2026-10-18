package com.example.primecare.utils

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.primecare.R

/**
 * Shared utility to handle the "Back" button and Title for all fragments
 * that use include_topbar_back.xml.
 */
object BackNavHelper {
    fun setupBack(fragment: Fragment, title: String) {
        val view = fragment.view ?: return
        
        // Find and setup the back button if it exists
        view.findViewById<android.view.View>(R.id.btnBack)?.setOnClickListener {
            val name = fragment::class.simpleName
            when {
                name == "AdminDashboardFragment" -> {
                    fragment.findNavController().navigate(R.id.action_adminDashboard_to_login)
                }
                name == "DoctorDashboardFragment" -> {
                    fragment.findNavController().navigate(R.id.action_doctorDashboard_to_login)
                }
                name == "LoginFragment" -> {
                    // Start of the graph, so we finish the activity (return to Splash/Welcome)
                    fragment.activity?.finish()
                }
                else -> {
                    // Standard back navigation for all other screens
                    fragment.findNavController().navigateUp()
                }
            }
        }
        
        // Find and set the title if it exists
        view.findViewById<android.widget.TextView>(R.id.tvTitle)?.text = title
    }
}

package com.example.primecare

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.primecare.data.api.SessionEvents
import com.example.primecare.data.local.SessionManager
import com.example.primecare.data.local.StaffRole
import com.example.primecare.data.repository.AuthRepository
import com.example.primecare.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * PrimeCare Staff App - single-Activity host.
 *
 * - On a cold start, checks the stored token against the SERVER and skips
 *   straight to the right dashboard if it is still valid.
 * - Listens for SessionEvents.expired (a 401 from any screen) and returns
 *   to the login screen.
 * - Shows a bottom navigation bar (different tabs for Admin and Doctor) on
 *   the main screens only. It is hidden on login, register and the
 *   consultation screens so nobody leaves unsaved clinical work by accident.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private var menuRole: StaffRole? = null

    private val navController
        get() = (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val authRepository = AuthRepository(sessionManager)

        setupBottomNav()

        // Only on a cold start: after rotation the nav graph restores itself.
        if (savedInstanceState == null && sessionManager.isLoggedIn()) {
            lifecycleScope.launch {
                val role = authRepository.validateSession()
                if (role != null) {
                    navController.navigate(
                        homeFor(role),
                        null,
                        NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()
                    )
                }
                // role == null: session invalid, already cleared; login screen stays.
            }
        }

        SessionEvents.expired.observe(this) { expired ->
            if (expired == true) {
                SessionEvents.consume()
                if (navController.currentDestination?.id != R.id.loginFragment) {
                    Toast.makeText(this, "Your session has expired. Please log in again.", Toast.LENGTH_LONG).show()
                    navController.navigate(
                        R.id.loginFragment,
                        null,
                        NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build()
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------ bottom navigation

    private fun homeFor(role: StaffRole): Int =
        if (role == StaffRole.ADMIN) R.id.adminDashboardFragment else R.id.doctorDashboardFragment

    /**
     * Which tab should be highlighted for a screen? Returns null when the
     * bar must be hidden. Secondary screens highlight their parent tab.
     */
    private fun tabFor(destinationId: Int, role: StaffRole): Int? {
        return if (role == StaffRole.ADMIN) {
            when (destinationId) {
                R.id.adminDashboardFragment,
                R.id.appointmentsListFragment,
                R.id.registerWalkinFragment -> R.id.adminDashboardFragment
                R.id.checkinFragment -> R.id.checkinFragment
                R.id.queueFragment -> R.id.queueFragment
                R.id.assignDoctorFragment -> R.id.assignDoctorFragment
                R.id.settingsFragment,
                R.id.helpFragment -> R.id.settingsFragment
                else -> null
            }
        } else {
            when (destinationId) {
                R.id.doctorDashboardFragment -> R.id.doctorDashboardFragment
                R.id.queueFragment -> R.id.queueFragment
                R.id.repeatRequestsFragment -> R.id.repeatRequestsFragment
                R.id.settingsFragment,
                R.id.helpFragment -> R.id.settingsFragment
                else -> null
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val role = sessionManager.getRole()
            if (role != null && item.itemId != navController.currentDestination?.id) {
                val home = homeFor(role)
                val options = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    // Tabs sit on top of Home, so Back always returns to Home first.
                    .setPopUpTo(home, item.itemId == home)
                    .build()
                navController.navigate(item.itemId, null, options)
            }
            true
        }
        binding.bottomNav.setOnItemReselectedListener { /* already here - do nothing */ }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNav(destination.id)
        }
    }

    private fun updateBottomNav(destinationId: Int) {
        val role = if (sessionManager.isLoggedIn()) sessionManager.getRole() else null
        val tab = if (role == null) null else tabFor(destinationId, role)

        if (role == null || tab == null) {
            binding.bottomNav.visibility = View.GONE
            return
        }

        if (menuRole != role) {
            binding.bottomNav.menu.clear()
            binding.bottomNav.inflateMenu(
                if (role == StaffRole.ADMIN) R.menu.menu_admin_nav else R.menu.menu_doctor_nav
            )
            menuRole = role
        }
        binding.bottomNav.visibility = View.VISIBLE
        binding.bottomNav.menu.findItem(tab)?.isChecked = true
    }
}
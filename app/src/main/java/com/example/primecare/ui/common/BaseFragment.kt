package com.example.primecare.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * PrimeCare — Base ViewBinding Fragment
 * ---------------------------------------------------------
 * Standard delegate pattern that avoids memory leaks: ViewBinding
 * references a Fragment's views, but a Fragment's underlying View
 * can be destroyed (e.g. on back-stack navigation) while the
 * Fragment instance itself stays alive. Without nulling the binding
 * in onDestroyView(), you leak the old View tree.
 *
 * Every screen Fragment extends this instead of Fragment() directly,
 * so this cleanup is handled once, centrally.
 */
abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "Binding accessed outside of view lifecycle (between onDestroyView and onCreateView)."
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

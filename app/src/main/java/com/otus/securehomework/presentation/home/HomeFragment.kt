package com.otus.securehomework.presentation.home

import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.databinding.FragmentHomeBinding
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.isBiometricAvailable
import com.otus.securehomework.presentation.logout
import com.otus.securehomework.presentation.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        binding.progressbar.visible(false)
        val biometricManager = BiometricManager.from(this.requireContext())

        binding.biometricLoginEnabled.visible(biometricManager.isBiometricAvailable())

        viewModel.getUser()

        viewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is Response.Success -> {
                    binding.progressbar.visible(false)
                    updateUI(it.value.user)
                }

                is Response.Loading -> {
                    binding.progressbar.visible(true)
                }

                is Response.Failure -> {
                    handleApiError(it)
                }
            }
        }

        viewModel.isBiometricEnabled.observe(viewLifecycleOwner) {
            binding.biometricLoginEnabled.isChecked = it ?: false
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }

        binding.biometricLoginEnabled.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onBiometricLoginEnabled(isChecked)
        }
    }

    private fun updateUI(user: User) {
        with(binding) {
            textViewId.text = user.id.toString()
            textViewName.text = user.name
            textViewEmail.text = user.email
        }
    }
}
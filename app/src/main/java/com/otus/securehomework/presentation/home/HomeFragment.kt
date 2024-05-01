package com.otus.securehomework.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.databinding.FragmentHomeBinding
import com.otus.securehomework.domain.biometric.BiometricHelper
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.logout
import com.otus.securehomework.presentation.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var biometricHelper: BiometricHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        binding.progressbar.visible(false)

        viewModel.getUser()

        viewModel.user.observe(viewLifecycleOwner) {
            onGetUser(it)
        }

        binding.buttonLogout.setOnClickListener { logout() }
        binding.biometricAuthDisabled.setOnClickListener {
            lifecycleScope.launch { biometricHelper.enableAuth(false) }
        }
        binding.biometricAuthEnabled.setOnClickListener {
            lifecycleScope.launch { biometricHelper.enableAuth(false) }
        }
    }

    private fun onGetUser(it: Response<LoginResponse>) {
        when (it) {
            is Response.Success -> {
                binding.progressbar.visible(false)
                updateUI(it.value.user)
            }

            is Response.Loading -> {
                binding.progressbar.visible(true)
            }

            is Response.Failure -> {
                binding.progressbar.visible(false)
                handleApiError(it)
            }
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
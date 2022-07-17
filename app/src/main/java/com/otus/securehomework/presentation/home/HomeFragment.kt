package com.otus.securehomework.presentation.home

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.databinding.FragmentHomeBinding
import com.otus.securehomework.presentation.handleApiError
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

        viewModel.getUser()

        viewModel.user.observe(viewLifecycleOwner, {
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
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.enableBiometric.visibility = View.VISIBLE

            viewModel.getBiometricSettings().asLiveData().observe(this.requireActivity(), Observer {
               updateBiometricSettings(it)
            })
        }

        binding.buttonLogout.setOnClickListener {
            logout()
        }


        binding.enableBiometric.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.saveBiometricSetting(isChecked)
        }

    }

    private fun updateBiometricSettings(isEnabled: Boolean) {
        if (binding.enableBiometric.isChecked != isEnabled) {
            binding.enableBiometric.isChecked = isEnabled
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
package com.otus.securehomework.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.data.repository.BiometricRepository
import com.otus.securehomework.data.repository.BiometricState
import com.otus.securehomework.databinding.FragmentHomeBinding
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.logout
import com.otus.securehomework.presentation.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {


    @Inject
    lateinit var biometricRepository: BiometricRepository

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

        binding.buttonLogout.setOnClickListener {
            logout()
        }


        when(biometricRepository.getBiometricState()){
            BiometricState.OFF -> binding.biometricMode.check(R.id.off)
            BiometricState.WEAK -> binding.biometricMode.check(R.id.weak)
            BiometricState.STRONG -> binding.biometricMode.check(R.id.strong)
        }

        binding.biometricMode.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.off -> biometricRepository.setBiometricState(BiometricState.OFF)
                R.id.weak -> biometricRepository.setBiometricState(BiometricState.WEAK)
                R.id.strong -> biometricRepository.setBiometricState(BiometricState.STRONG)
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
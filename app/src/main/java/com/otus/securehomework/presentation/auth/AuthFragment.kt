package com.otus.securehomework.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.otus.securehomework.R
import com.otus.securehomework.data.security.authenticate
import com.otus.securehomework.databinding.FragmentAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var binding: FragmentAuthBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAuthBinding.bind(view)

        with(binding) {
            weakBiometryButton.setOnClickListener {
                val success = BiometricManager.from(requireContext())
                    .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
                if (success) {
                    val authPrompt = Class2BiometricAuthPrompt.Builder("Weak biometry", "dismiss").apply {
                        setSubtitle("Input your biometry")
                        setDescription("We need your finger")
                        setConfirmationRequired(true)
                    }.build()

                    lifecycleScope.launch {
                        try {
                            authPrompt.authenticate(AuthPromptHost(this@AuthFragment))
                        } catch (e: Exception) {
                            findNavController().navigate(R.id.to_login_action)
                        }
                    }
                } else {
                    findNavController().navigate(R.id.to_login_action)
                }
            }
            strongBiometryButton.setOnClickListener {
                val success = BiometricManager.from(requireContext())
                    .canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
                if (success) {
                    val authPrompt = Class3BiometricAuthPrompt.Builder("Strong biometry", "dismiss").apply {
                        setSubtitle("Input your biometry")
                        setDescription("We need your finger")
                        setConfirmationRequired(true)
                    }.build()

                    lifecycleScope.launch {
                        try {
                            authPrompt.authenticate(AuthPromptHost(this@AuthFragment), null)
                        } catch (e: Exception) {
                            findNavController().navigate(R.id.to_login_action)
                        }
                    }
                } else {
                    findNavController().navigate(R.id.to_login_action)
                }
            }
            buttonLogin.setOnClickListener {
                findNavController().navigate(R.id.to_login_action)
            }
            buttonRegister.setOnClickListener {
                findNavController().navigate(R.id.to_register_action)
            }
        }
    }
}
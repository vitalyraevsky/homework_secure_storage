package com.otus.securehomework.presentation.auth

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.databinding.FragmentLoginBinding
import com.otus.securehomework.presentation.enable
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import com.otus.securehomework.presentation.visible
import com.otus.securehomework.security.biometry.BiometricAuthenticator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.E

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.progressbar.visible(false)
        binding.buttonLogin.enable(false)

        viewModel.loginResponse.observe(viewLifecycleOwner) {
            binding.progressbar.visible(it is Response.Loading)
            when (it) {
                is Response.Success -> lifecycleScope.launch {
                    viewModel.saveAccessTokens(
                        it.value.user.access_token!!,
                        it.value.user.refresh_token!!
                    )
                    binding.progressbar.visible(false)
                    requireActivity().startNewActivity(HomeActivity::class.java)
                }

                is Response.Failure -> {
                    binding.progressbar.visible(false)
                    handleApiError(it) { login() }
                }

                is Response.Loading -> binding.progressbar.visible(true)
            }
        }

        viewModel.isBiometricEnabled.observe(viewLifecycleOwner) {
            binding.fingerprintButton.visible(it)
        }

        binding.fingerprintButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = biometricAuthenticator.authenticate()
                    result.cryptoObject?.cipher?.let {
                        requireActivity().startNewActivity(HomeActivity::class.java)
                    } ?: throw Exception("Error auth")
                } catch (e: AuthPromptErrorException) {
                    Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
                } catch(e: AuthPromptFailureException) {
                    Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
                } catch(e: java.lang.Exception) {
                    Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.editTextTextPassword.addTextChangedListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            binding.buttonLogin.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }
        binding.buttonLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        viewModel.login(email, password)
    }
}
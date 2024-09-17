package com.otus.securehomework.presentation.auth

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.auth.AuthPromptErrorException
import androidx.biometric.auth.AuthPromptFailureException
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.biometric.auth.Class3BiometricAuthPrompt
import androidx.biometric.auth.authenticate
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.biometrics.BiometricCipher
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import com.otus.securehomework.databinding.FragmentLoginBinding
import com.otus.securehomework.presentation.enable
import com.otus.securehomework.presentation.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private val keyAlias by lazy { "${requireActivity().applicationContext.packageName}.biometricKey"}

@Inject
   lateinit var biometricCipher : BiometricCipher

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

    private lateinit var mainHandler: Handler

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        mainHandler = Handler(Looper.getMainLooper())

        binding.progressbar.visible(false)
        binding.buttonLogin.enable(false)

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer {
            binding.progressbar.visible(it.response is Response.Loading)
            if (it.isLoggedIn){
                    mainHandler.post {
                        biometricAccess()
                    }
            }else{
                when (it.response) {
                    is Response.Success -> {
                        lifecycleScope.launch {
                            showTwoOptionDialog(
                                context = requireContext(),
                                title = requireContext().getString(R.string.enable_biometric_authentication),
                                message = requireContext().getString(R.string.use_your_biometric_to_sign_in),
                                positiveButtonText = requireContext().getString(R.string.enable),
                                negativeButtonText = requireContext().getString(R.string.not_now),
                                onNegativeClick = {
                                    requireActivity().startNewActivity(HomeActivity::class.java)
                                },
                                onPositiveClick = {
                                    requireActivity().startNewActivity(HomeActivity::class.java)
                                }
                            )
                            viewModel.saveAccessTokens(
                                it.response.value.user.access_token!!,
                                it.response.value.user.refresh_token!!
                            )
                        }
                    }
                    is Response.Failure -> handleApiError(it.response) { login() }
                    Response.Loading -> Unit
                    else ->{

                    }
                }
            }

        })
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



    @RequiresApi(Build.VERSION_CODES.M)
    private fun biometricAccess() {
        if (BiometricManager.from(requireContext())
                .canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS
        ) {
//            val biometricCipher = BiometricCipher(requireActivity().applicationContext, keySpecProvider.provideKeyGenParameterSpec(keyAlias))
            val encryptor = biometricCipher.getEncryptor()
            val authPrompt = Class3BiometricAuthPrompt.Builder(requireContext().getString(R.string.strong_biometry), requireContext().getString(R.string.dismiss)).apply {
                setSubtitle(requireContext().getString(R.string.input_your_biometry))
                setDescription(requireContext().getString(R.string.we_need_your_finger))
                setConfirmationRequired(true)
            }.build()
            lifecycleScope.launch {
                try {
                    val authResult =
                        authPrompt.authenticate(AuthPromptHost(requireActivity()), encryptor)
                    val encryptedEntity = authResult.cryptoObject?.cipher?.let { cipher ->
                        biometricCipher.encrypt(requireContext().getString(R.string.secret_data), cipher)
                    }
                    Log.d(AuthActivity::class.simpleName, String(encryptedEntity!!.ciphertext))
                    requireActivity().startNewActivity(HomeActivity::class.java)
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else if (BiometricManager.from(requireContext())
                .canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS
        ) {
            val authPrompt =
                Class2BiometricAuthPrompt.Builder(requireContext().getString(R.string.weak_biometry), requireContext().getString(R.string.dismiss)).apply {
                    setSubtitle(requireContext().getString(R.string.input_your_biometry))
                    setDescription(requireContext().getString(R.string.we_need_your_finger))
                    setConfirmationRequired(true)
                }.build()
            lifecycleScope.launch {
                try {
                    authPrompt.authenticate(AuthPromptHost(requireActivity()))
                    requireActivity().startNewActivity(HomeActivity::class.java)
                } catch (e: AuthPromptErrorException) {
                    Log.e("AuthPromptError", e.message ?: "no message")
                } catch (e: AuthPromptFailureException) {
                    Log.e("AuthPromptFailure", e.message ?: "no message")
                }
            }
        } else {
            Toast.makeText(requireActivity(), requireContext().getString(R.string.biometry_not_supported), Toast.LENGTH_LONG).show()
        }
    }

    private fun showTwoOptionDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick()
            dialog.dismiss()
        }
        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            onNegativeClick()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

}



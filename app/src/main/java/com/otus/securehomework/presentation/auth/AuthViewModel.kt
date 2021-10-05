package com.otus.securehomework.presentation.auth

import androidx.biometric.auth.AuthPromptHost
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.crypto.BiometricAuthManager
import com.otus.securehomework.data.source.local.SecureUserPreferences
import com.otus.securehomework.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject constructor(
    private val repository: AuthRepository,
    private val userPreferences: SecureUserPreferences,
    private val biometricAuthManager: BiometricAuthManager
) : BaseViewModel(repository) {

    private val _loginResponse: MutableLiveData<Response<LoginResponse>> = MutableLiveData()
    val loginResponse: LiveData<Response<LoginResponse>>
        get() = _loginResponse

    private val _biometricsInput: MutableLiveData<Boolean> = MutableLiveData()
    val biometricsInput: LiveData<Boolean>
        get() = _biometricsInput

    val hasBiometric: LiveData<Boolean>
        get() = userPreferences.hasBiometrics.asLiveData()

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _loginResponse.value = Response.Loading
        _loginResponse.value = repository.login(email, password)
    }

    fun saveAccessTokens(accessToken: String, refreshToken: String) {
        userPreferences.saveAccessTokens(accessToken, refreshToken)
    }

    fun startBiometrics(host: AuthPromptHost) {
        viewModelScope.launch {
            try {
                _biometricsInput.value = biometricAuthManager.checkBiometricAuth(host)
            } catch (e: Exception) {
                _biometricsInput.value = false
            }
        }
    }
}
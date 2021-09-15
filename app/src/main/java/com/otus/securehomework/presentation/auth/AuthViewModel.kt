package com.otus.securehomework.presentation.auth

import androidx.biometric.auth.AuthPromptHost
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginData
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.source.crypto.BiometricAuthManager
import com.otus.securehomework.data.source.local.SecureUserPreferences
import com.otus.securehomework.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
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

    private val _showBiometricsError: MutableLiveData<String> = MutableLiveData()
    val showBiometricsError: LiveData<String>
        get() = _showBiometricsError

    val hasBiometric: LiveData<Boolean>
        get() = userPreferences.biometricData.map { it.isNotEmpty() }.asLiveData()

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        _loginResponse.value = Response.Loading
        _loginResponse.value = repository.login(email, password)
        userPreferences.saveTempLoginData(LoginData(email, password))
    }

    fun saveAccessTokens(accessToken: String, refreshToken: String) {
        userPreferences.saveAccessTokens(accessToken, refreshToken)
    }

    fun startBiometrics(host: AuthPromptHost) {
        viewModelScope.launch {
            biometricAuthManager.checkBiometricAuth(host).let {
                if (it != LoginData.STUB) login(it.email, it.password)
                else _showBiometricsError.value = "Wrong finger, try again later"
            }
        }
    }
}
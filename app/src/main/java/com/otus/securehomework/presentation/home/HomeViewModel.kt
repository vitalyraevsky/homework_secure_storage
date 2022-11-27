package com.otus.securehomework.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.LoginResponse
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val repository: UserRepository
) : BaseViewModel(repository) {

    private val _user: MutableLiveData<Response<LoginResponse>> = MutableLiveData()
    val user: LiveData<Response<LoginResponse>>
        get() = _user

    private val _biometricSettings: MutableLiveData<Boolean> = MutableLiveData()
    val biometricSettings: LiveData<Boolean> get() = _biometricSettings

    init {
        viewModelScope.launch {
            repository.getBiometricSettings().collect {
                _biometricSettings.value = it
            }
        }
    }

    fun getUser() = viewModelScope.launch {
        _user.value = Response.Loading
        _user.value = repository.getUser()
    }

    fun enableBiometricAuth() {
        viewModelScope.launch {
            repository.enableBiometricAuth()
        }
    }

    fun disableBiometricAuth() {
        viewModelScope.launch {
            repository.disableBiometricAuth()
        }
    }
}
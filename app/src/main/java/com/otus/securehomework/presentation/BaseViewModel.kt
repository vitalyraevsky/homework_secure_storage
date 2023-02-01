package com.otus.securehomework.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otus.securehomework.data.repository.BaseRepository
import com.otus.securehomework.data.source.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel(
    private val repository: BaseRepository,
    private val userPreferences: UserPreferences,
    private val _isBiometricEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
) : ViewModel() {

    init {
        viewModelScope.launch {
            _isBiometricEnabled.value = userPreferences.isBiometricLoginEnabled.first()
        }
    }

    val isBiometricEnabled: LiveData<Boolean>
        get() = _isBiometricEnabled

    fun setBiometricEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setBiometricEnable(isEnabled)
        }
    }

    suspend fun logout() {
        return withContext(Dispatchers.IO) {
            repository.logout()
        }
    }
}
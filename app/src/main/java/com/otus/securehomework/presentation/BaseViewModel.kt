package com.otus.securehomework.presentation

import androidx.lifecycle.ViewModel
import com.otus.securehomework.data.repository.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseViewModel(
    private val repository: BaseRepository
) : ViewModel() {

    suspend fun logout() {
        return withContext(Dispatchers.IO) {
            repository.logout()
        }
    }
}
package com.otus.securehomework.data.crypto

import androidx.constraintlayout.helper.widget.Flow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

class PreMTokenStorageImpl(
    private val dataStore: DataStore<Preferences>
    ): SecuredTokenStorage {

    override suspend fun saveAccessToken(token: String?) {
        TODO("Not yet implemented")
    }

    override suspend fun saveRefreshToken(token: String?) {
        TODO("Not yet implemented")
    }

    override fun getAccessToken(): kotlinx.coroutines.flow.Flow<String?> {
        TODO("Not yet implemented")
    }

    override fun getRefreshToken(): kotlinx.coroutines.flow.Flow<String?> {
        TODO("Not yet implemented")
    }
}
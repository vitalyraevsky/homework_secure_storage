package com.otus.securehomework.data.crypto

import kotlinx.coroutines.flow.Flow

interface SecuredTokenStorage {

    suspend fun saveAccessToken(token: String?)
    suspend fun saveRefreshToken(token: String?)
    fun getAccessToken(): Flow<String?>
    fun getRefreshToken(): Flow<String?>

}
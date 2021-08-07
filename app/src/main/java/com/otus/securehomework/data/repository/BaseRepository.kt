package com.otus.securehomework.data.repository

import com.otus.securehomework.data.source.SafeApiCall
import com.otus.securehomework.data.source.network.BaseApi

abstract class BaseRepository(
    private val api: BaseApi
) : SafeApiCall {

    suspend fun logout() = safeApiCall {
        api.logout()
    }
}
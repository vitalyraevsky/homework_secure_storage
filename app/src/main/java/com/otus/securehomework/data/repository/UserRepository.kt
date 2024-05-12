package com.otus.securehomework.data.repository

import com.otus.securehomework.data.source.network.UserApi
import javax.inject.Inject

class UserRepository
@Inject constructor(
    private val api: UserApi
) : BaseRepository(api) {

    suspend fun getUser() = safeApiCall { api.getUser() }
}
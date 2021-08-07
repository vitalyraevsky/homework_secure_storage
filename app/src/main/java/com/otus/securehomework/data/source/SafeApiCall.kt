package com.otus.securehomework.data.source

import com.otus.securehomework.data.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

interface SafeApiCall {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Response<T> {
        return withContext(Dispatchers.IO) {
            try {
                Response.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        Response.Failure(false, throwable.code(), throwable.response()?.errorBody())
                    }
                    else -> {
                        Response.Failure(true, null, null)
                    }
                }
            }
        }
    }
}
package com.otus.securehomework.data.myDefence

interface Biometry {
    suspend fun authenticateWithBiometric(doOnError: (throwable: Throwable) -> Unit, doOnSuccess: () -> Unit)
}
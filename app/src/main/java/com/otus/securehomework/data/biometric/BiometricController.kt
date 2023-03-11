package com.otus.securehomework.data.biometric

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

interface BiometricController {

    suspend fun authWithBiometric(
        fragment: Fragment,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit
    )

    suspend fun authWithBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit = {},
        doOnSuccess: () -> Unit
    )
}
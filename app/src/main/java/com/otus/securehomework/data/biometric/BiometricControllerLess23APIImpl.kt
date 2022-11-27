package com.otus.securehomework.data.biometric

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class BiometricControllerLess23APIImpl : BiometricController {
    override suspend fun authWithBiometric(
        fragment: Fragment,
        doOnError: (throwable: Throwable) -> Unit,
        doOnSuccess: () -> Unit
    ) {
        doOnSuccess.invoke()
    }

    override suspend fun authWithBiometric(
        fragmentActivity: FragmentActivity,
        doOnError: (throwable: Throwable) -> Unit,
        doOnSuccess: () -> Unit
    ) {
        doOnSuccess.invoke()
    }
}
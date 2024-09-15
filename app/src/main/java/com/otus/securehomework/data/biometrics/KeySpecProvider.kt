package com.otus.securehomework.data.biometrics

import android.security.keystore.KeyGenParameterSpec

interface KeySpecProvider {
    fun provideKeyGenParameterSpec(keyAlias: String): KeyGenParameterSpec
}
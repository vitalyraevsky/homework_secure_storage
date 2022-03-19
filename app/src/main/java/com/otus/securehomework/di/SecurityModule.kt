package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import com.otus.securehomework.security.*
import com.otus.securehomework.security.biometric.BiometricService
import com.otus.securehomework.security.biometric.BiometricServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SecurityModule {

    @Binds
    fun bindEncryptionService(impl: AesEncryptionService): EncryptionService

    @Binds
    fun bindBiometricService(impl: BiometricServiceImpl): BiometricService

    companion object {
        @Provides
        fun provideAesKeyProvider(@ApplicationContext context: Context): AesKeyProvider {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                DefaultAesKeyProvider(context)
            } else {
                LegacyAesKeyProvider(context)
            }
        }
    }
}
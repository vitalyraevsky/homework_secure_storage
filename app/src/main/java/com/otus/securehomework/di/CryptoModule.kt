package com.otus.securehomework.di

import android.content.Context
import androidx.biometric.BiometricManager
import android.os.Build
import com.otus.securehomework.data.crypto.key.DefaultKeyProvider
import com.otus.securehomework.data.crypto.key.KeyProvider
import com.otus.securehomework.data.crypto.key.LegacyKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Singleton
    @Provides
    fun getKeyProvider(@ApplicationContext context: Context): KeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            DefaultKeyProvider()
        } else {
            LegacyKeyProvider(context)
        }
    }

    @Provides
    fun provideBiometricManager(@ApplicationContext context: Context): BiometricManager {
        return BiometricManager.from(context)
    }
}

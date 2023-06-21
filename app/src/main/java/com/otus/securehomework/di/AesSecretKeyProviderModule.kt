package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import com.otus.securehomework.security.AesSecretKeyProvider
import com.otus.securehomework.security.AesSecretKeyProviderImpl
import com.otus.securehomework.security.AesSecretKeyProviderImplLegacy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AesSecretKeyProviderModule {
    @Provides
    @Singleton
    fun providesAesSecretKeyProvider(@ApplicationContext context: Context): AesSecretKeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AesSecretKeyProviderImpl()
        } else {
            AesSecretKeyProviderImplLegacy(context)
        }
    }
}
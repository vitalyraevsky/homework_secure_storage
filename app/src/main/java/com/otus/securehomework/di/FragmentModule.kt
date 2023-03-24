package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.otus.securehomework.security.biometry.BiometricAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {
    @RequiresApi(Build.VERSION_CODES.M)
    @Provides
    fun provideBiometricAuthenticator(
        @ActivityContext context: Context
    ): BiometricAuthenticator {
        return BiometricAuthenticator(context)
    }
}
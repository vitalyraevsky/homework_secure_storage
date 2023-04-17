package com.otus.securehomework.di

import com.otus.securehomework.data.myDefence.Biometry
import com.otus.securehomework.data.myDefence.impl.BiometryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class BiometryModule {

    @Binds
    abstract fun biometry(biometryImpl: BiometryImpl): Biometry
}
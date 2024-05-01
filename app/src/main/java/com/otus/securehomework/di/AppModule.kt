package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
import com.otus.securehomework.domain.biometric.BiometricHelper
import com.otus.securehomework.domain.secure.KeyProviderImpl
import com.otus.securehomework.domain.secure.KeyProviderLessThanMImp
import com.otus.securehomework.domain.secure.Security
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRemoteDataSource(
        userPreferences: UserPreferences
    ): RemoteDataSource = RemoteDataSource(userPreferences)

    @Provides
    fun provideAuthApi(
        remoteDataSource: RemoteDataSource,
    ): AuthApi = remoteDataSource.buildApi(AuthApi::class.java)

    @Provides
    fun provideUserApi(
        remoteDataSource: RemoteDataSource,
    ): UserApi = remoteDataSource.buildApi(UserApi::class.java)

    @Provides
    fun provideBiometricHelper(
        @ApplicationContext context: Context
    ) = BiometricHelper(context)

    @Singleton
    @Provides
    fun provideUserPreferences(
        @ApplicationContext context: Context,
        security: Security
    ): UserPreferences = UserPreferences(context, security)

    @Provides
    fun provideAuthRepository(
        authApi: AuthApi,
        userPreferences: UserPreferences
    ): AuthRepository = AuthRepository(authApi, userPreferences)

    @Provides
    fun provideUserRepository(
        userApi: UserApi
    ): UserRepository = UserRepository(userApi)

    @Provides
    fun provideSecurity(
        @ApplicationContext context: Context
    ): Security {
        val keyProvider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyProviderImpl(context)
        } else {
            KeyProviderLessThanMImp(context)
        }

        return Security(keyProvider)
    }
}
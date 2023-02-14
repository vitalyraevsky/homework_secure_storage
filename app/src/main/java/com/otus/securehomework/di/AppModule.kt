package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import com.otus.securehomework.data.biometric.BiometricHelper
import com.otus.securehomework.data.crypto.AfterMSecretKey
import com.otus.securehomework.data.crypto.BeforeMSecretKey
import com.otus.securehomework.data.crypto.Crypto
import com.otus.securehomework.data.crypto.ISecretKey
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
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
    ): RemoteDataSource {
        return RemoteDataSource(userPreferences)
    }

    @Provides
    fun provideAuthApi(
        remoteDataSource: RemoteDataSource,
    ): AuthApi {
        return remoteDataSource.buildApi(AuthApi::class.java)
    }

    @Provides
    fun provideUserApi(
        remoteDataSource: RemoteDataSource,
    ): UserApi {
        return remoteDataSource.buildApi(UserApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUserPreferences(
        @ApplicationContext context: Context,
        crypto: Crypto,
        secretKey: ISecretKey
    ): UserPreferences {
        return UserPreferences(context, crypto, secretKey)
    }

    @Provides
    fun provideAuthRepository(
        authApi: AuthApi,
        userPreferences: UserPreferences
    ): AuthRepository {
        return AuthRepository(authApi, userPreferences)
    }

    @Provides
    fun provideUserRepository(
        userApi: UserApi
    ): UserRepository {
        return UserRepository(userApi)
    }

    @Provides
    @Singleton
    fun provideSecretKey(
        @ApplicationContext context: Context
    ): ISecretKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        AfterMSecretKey()
    } else {
        BeforeMSecretKey(context)
    }
}

@Module
@InstallIn(ActivityComponent::class)
object ActivityComponent {

    @Provides
    fun provideBiometricHelper(@ActivityContext context: Context) = BiometricHelper(context)
}

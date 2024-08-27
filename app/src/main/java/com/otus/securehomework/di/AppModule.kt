package com.otus.securehomework.di

import android.content.Context
import com.otus.securehomework.data.crypto.Keys
import com.otus.securehomework.data.crypto.Security
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.SecurityRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
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
        @ApplicationContext context: Context
    ): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    fun provideAuthRepository(
        authApi: AuthApi,
        userPreferences: UserPreferences,
        securityRepository: SecurityRepository
    ): AuthRepository {
        return AuthRepository(authApi, userPreferences, securityRepository)
    }

    @Provides
    fun provideSecurityRepository(
        keys: Keys,
        security: Security
    ): SecurityRepository {
        return SecurityRepository(keys, security)
    }

    @Singleton
    @Provides
    fun provideKeys(
        @ApplicationContext context: Context
    ): Keys {
        return Keys(context)
    }

    @Provides
    fun provideUserRepository(
        userApi: UserApi
    ): UserRepository {
        return UserRepository(userApi)
    }
}
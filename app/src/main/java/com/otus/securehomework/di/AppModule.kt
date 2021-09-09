package com.otus.securehomework.di

import android.content.Context
import com.otus.securehomework.App
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.crypto.Keys
import com.otus.securehomework.data.source.local.SecureUserPreferences
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRemoteDataSource(): RemoteDataSource {
        return RemoteDataSource()
    }

    @Provides
    fun provideAuthApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context,
        userPreferences: SecureUserPreferences
    ): AuthApi {
        return remoteDataSource.buildApi(AuthApi::class.java, context, userPreferences)
    }

    @Provides
    fun provideUserApi(
        remoteDataSource: RemoteDataSource,
        @ApplicationContext context: Context,
        userPreferences: SecureUserPreferences
    ): UserApi {
        return remoteDataSource.buildApi(UserApi::class.java, context, userPreferences)
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
        userPreferences: SecureUserPreferences
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
    fun provideSecureUserPreferences(@ApplicationContext context: Context, keys: Keys): SecureUserPreferences =
        SecureUserPreferences(context, keys.getMasterKey())
}
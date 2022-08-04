package com.otus.securehomework.di

import android.content.Context
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.TokenAuthenticator
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.TokenRefreshApi
import com.otus.securehomework.data.source.network.UserApi
import com.otus.securehomework.data.source.secure.PersistentStoreManager
import com.otus.securehomework.data.source.secure.PreferenceManager
import com.otus.securehomework.data.source.secure.PreferenceManagerImpl
import com.otus.securehomework.data.source.secure.Security
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
        preferenceManager: PreferenceManager
    ): RemoteDataSource {
        return RemoteDataSource(preferenceManager)
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
        preferenceManager: PreferenceManager
    ): AuthRepository {
        return AuthRepository(authApi, preferenceManager)
    }

    @Provides
    fun providePreferenceManager(
        userPreferences: UserPreferences,
        security: Security
    ): PreferenceManager {
        return PreferenceManagerImpl(userPreferences, security)
    }

    @Provides
    fun provideUserRepository(
        userApi: UserApi
    ): UserRepository {
        return UserRepository(userApi)
    }

    @Singleton
    @Provides
    fun provideStoreManager(
        @ApplicationContext context: Context
    ): PersistentStoreManager = PersistentStoreManager(context)

    @Singleton
    @Provides
    fun provideSecurity(
        manager: PersistentStoreManager
    ): Security = Security(manager)
}
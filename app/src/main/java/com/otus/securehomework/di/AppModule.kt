package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.repository.crypto.IKeyProvider
import com.otus.securehomework.data.repository.crypto.Keys
import com.otus.securehomework.data.repository.crypto.LegacyKeys
import com.otus.securehomework.data.repository.crypto.SecureUserPreferences
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
        userPreferences: SecureUserPreferences
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

    @Singleton
    @Provides
    fun provideKeys(
        @ApplicationContext applicationContext: Context
    ): IKeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Keys()
        } else {
            LegacyKeys(applicationContext)
        }
    }

}
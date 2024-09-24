package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.security.KeyProvider
import com.otus.securehomework.data.security.KeyProviderDown23
import com.otus.securehomework.data.security.KeyProviderMore23
import com.otus.securehomework.data.security.UtilsAes
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

    private const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences"

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
        keyProvider: KeyProvider
    ): UserPreferences {
        return UserPreferences(
            context = context,
            keyProvider = keyProvider,
            aesHelper = UtilsAes()
        )
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

    @Singleton
    @Provides
    fun provideKeyProvider(
        @ApplicationContext context: Context
    ): KeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyProviderMore23()
        } else {
            KeyProviderDown23(
                context = context,
                sharedPreferences = context.getSharedPreferences(
                    SHARED_PREFERENCE_NAME,
                    Context.MODE_PRIVATE
                )
            )
        }
    }
}
package com.otus.securehomework.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.otus.securehomework.data.biometrics.DefaultKeySpecProvider
import com.otus.securehomework.data.biometrics.KeySpecProvider
import com.otus.securehomework.data.crypto.KEY_PROVIDER
import com.otus.securehomework.data.crypto.KeyProvider
import com.otus.securehomework.data.crypto.KeyProviderPostM
import com.otus.securehomework.data.crypto.KeyProviderPreM
import com.otus.securehomework.data.crypto.Keys
import com.otus.securehomework.data.crypto.SHARED_PREFERENCE_NAME
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
import java.security.KeyStore
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
        @ApplicationContext context: Context,
        keyProvider: KeyProvider
    ): Keys {
        return Keys(context, keyProvider)
    }

    @Provides
    fun provideUserRepository(
        userApi: UserApi
    ): UserRepository {
        return UserRepository(userApi)
    }

    @Provides
    fun provideKeySpecProvider(@ApplicationContext context: Context): KeySpecProvider {
        return DefaultKeySpecProvider(context)
    }

    @Provides
    @Singleton
    fun provideKeyProvider(@ApplicationContext context: Context, keyStore: KeyStore): KeyProvider {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyProviderPostM(keyStore)
        } else {
            KeyProviderPreM(context, keyStore, provideSharedPreferences(context))
        }
    }

    @Provides
    @Singleton
    fun provideKeyStore(): KeyStore{
        return KeyStore.getInstance(KEY_PROVIDER).apply {
            load(null)
        }
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences{
        return context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

}
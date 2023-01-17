package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import com.otus.securehomework.data.biometrik.BiometricCipherImpl
import com.otus.securehomework.data.biometrik.impl.BiometricCipher
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.security.Aes
import com.otus.securehomework.data.security.KeyGen
import com.otus.securehomework.data.security.impl.AesImpl
import com.otus.securehomework.data.security.impl.KeyGenImpl
import com.otus.securehomework.data.security.impl.OldKeyGenImpl
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
import dagger.Binds
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
    fun provideIKeyGenerator(
        newKeyGen: KeyGenImpl,
        OldKeyGen: OldKeyGenImpl
    ): KeyGen =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            newKeyGen
        } else {
            OldKeyGen
        }

    @Singleton
    @Provides
    fun provideKeyStore(): KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
}
@Module
@InstallIn(SingletonComponent::class)
interface AppModuleInt{
    @Binds
    fun bindBiometricCipher(impl: BiometricCipherImpl): BiometricCipher

    @Binds
    fun bindAes(impl:AesImpl): Aes
}
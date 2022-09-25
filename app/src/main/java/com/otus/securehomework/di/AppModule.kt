package com.otus.securehomework.di

import android.os.Build
import com.otus.securehomework.data.security.IEncryptorDecryptor
import com.otus.securehomework.data.security.IKeyGenerator
import com.otus.securehomework.data.security.biometric.BiometricCipher
import com.otus.securehomework.data.security.biometric.BiometricControllerImpl
import com.otus.securehomework.data.security.biometric.BiometricControllerStub
import com.otus.securehomework.data.security.biometric.IBiometricController
import com.otus.securehomework.data.security.impl.AesKeyGeneratorMImpl
import com.otus.securehomework.data.security.impl.EncryptorDecryptorImpl
import com.otus.securehomework.data.security.impl.OlderVersionKeyGeneratorImpl
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

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
    fun provideIKeyGenerator(
        aesKeyGeneratorMImpl: AesKeyGeneratorMImpl,
        olderVersionKeyGenerator: OlderVersionKeyGeneratorImpl
    ): IKeyGenerator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aesKeyGeneratorMImpl
        } else {
            olderVersionKeyGenerator
        }

    @Provides
    fun provideIEncryptorDecryptor(
        encryptorDecryptorImpl: EncryptorDecryptorImpl
    ): IEncryptorDecryptor = encryptorDecryptorImpl

    @Provides
    fun provideBiometricController(
        biometricCipher: BiometricCipher
    ): IBiometricController =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricControllerImpl(biometricCipher = biometricCipher)
        } else {
            BiometricControllerStub()
        }
}
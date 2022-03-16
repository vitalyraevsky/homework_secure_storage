package com.otus.securehomework.di

import android.content.Context
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.AuthApi
import com.otus.securehomework.data.source.network.UserApi
import com.otus.securehomework.security.AesEncryptionService
import com.otus.securehomework.security.AesKeystoreWrapperImpl
import com.otus.securehomework.security.RsaEncryptionService
import com.otus.securehomework.security.RsaKeystoreWrapperImpl
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
        @ApplicationContext context: Context,
        encryptionService: AesEncryptionService
    ): UserPreferences {
        return UserPreferences(context, encryptionService)
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
    fun provideAesEncryptionService(
        ksWrapper: AesKeystoreWrapperImpl
    ): AesEncryptionService {
        return AesEncryptionService(ksWrapper)
    }

    @Provides
    fun provideAesKeystoreWrapper(
        @ApplicationContext context: Context,
        rsaEncryptionService: RsaEncryptionService
    ): AesKeystoreWrapperImpl {
        return AesKeystoreWrapperImpl(context, rsaEncryptionService)
    }

    @Provides
    fun provideRsaEncryptionService(
        ksWrapper: RsaKeystoreWrapperImpl
    ): RsaEncryptionService {
        return RsaEncryptionService(ksWrapper)
    }

    @Provides
    fun provideRsaKeystoreWrapper(): RsaKeystoreWrapperImpl {
        return RsaKeystoreWrapperImpl()
    }
}
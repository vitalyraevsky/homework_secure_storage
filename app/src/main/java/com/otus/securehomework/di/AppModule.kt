package com.otus.securehomework.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.crypto.PostMTokenStorageImpl
import com.otus.securehomework.data.crypto.PreMTokenStorageImpl
import com.otus.securehomework.data.crypto.SecuredTokenStorage
import com.otus.securehomework.data.repository.AuthRepository
import com.otus.securehomework.data.repository.BiometricRepository
import com.otus.securehomework.data.repository.UserRepository
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.local.UserPreferences.Companion.dataStore
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
    fun provideSecuredTokenStorage(
        @ApplicationContext context: Context
    ): SecuredTokenStorage{
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            PostMTokenStorageImpl(context, context.dataStore)
        } else{
            PreMTokenStorageImpl(context, context.dataStore)
        }
    }

    @Singleton
    @Provides
    fun provideBiometricRepository(
        @ApplicationContext context: Context
    ): BiometricRepository {
        return BiometricRepository(context)
    }

    @Singleton
    @Provides
    fun provideRemoteDataSource(
        tokenStorage: SecuredTokenStorage
    ): RemoteDataSource {
        return RemoteDataSource(tokenStorage)
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
        tokenStorage: SecuredTokenStorage
    ): AuthRepository {
        return AuthRepository(authApi, tokenStorage)
    }

    @Provides
    fun provideUserRepository(
        userApi: UserApi
    ): UserRepository {
        return UserRepository(userApi)
    }
}
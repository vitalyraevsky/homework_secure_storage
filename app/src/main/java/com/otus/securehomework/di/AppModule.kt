package com.otus.securehomework.di

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.otus.securehomework.data.crypto.Encryption
import com.otus.securehomework.data.crypto.EncryptionLessThanM
import com.otus.securehomework.data.crypto.EncryptionMAndMore
import com.otus.securehomework.data.repository.AuthRepository
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

    private const val dataStoreFile: String = "securePref"
    private val Context.dataStore by preferencesDataStore(name = dataStoreFile)

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"

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
        encryption: Encryption,
        dataStore: DataStore<Preferences>
    ): UserPreferences {
        return UserPreferences(encryption, dataStore)
    }

    @Singleton
    @Provides
    fun provideKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
        }

    @Provides
    fun provideEncryption(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>,
        keyStore: KeyStore
    ): Encryption = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        EncryptionMAndMore(keyStore)
    } else {
        EncryptionLessThanM(context, dataStore, keyStore)
    }

    @Singleton
    @Provides
    fun provideDataStorePreferences(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

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
}
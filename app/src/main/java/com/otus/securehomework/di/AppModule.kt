package com.otus.securehomework.di

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import androidx.annotation.RequiresApi
import com.otus.securehomework.data.biometrics.BiometricCipher
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

    @RequiresApi(Build.VERSION_CODES.M)
    @Provides
    fun provideKeyGenParameterSpec(@ApplicationContext context: Context): KeyGenParameterSpec{
        val builder = KeyGenParameterSpec.Builder("${context.packageName}.biometricKey", PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE_GCM) // Устанавливаем режим блоков GCM
            .setEncryptionPaddings(ENCRYPTION_PADDING_NONE) // Без заполнения
            .setKeySize(KEY_SIZE) // Устанавливаем размер ключа
            .setUserAuthenticationRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    setUnlockedDeviceRequired(true) // Ключ можно использовать только если устройство разблокировано
                    val hasStringBox = context
                        .packageManager
                        .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE) // Проверка наличия StrongBox для безопасного хранения ключей
                    setIsStrongBoxBacked(hasStringBox) // Если StrongBox доступен, ключ будет в нем храниться
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    setUserAuthenticationParameters(
                        TIME_OUT_BIOMETRIC,
                        AUTH_BIOMETRIC_STRONG
                    ) // Требование аутентификации с использованием биометрии
                }
            }
        return builder.build()
    }

    @Provides
    fun provideBiometricCipher(@ApplicationContext context: Context, keySpec: KeyGenParameterSpec): BiometricCipher{
        return BiometricCipher(context, keySpec)
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

private const val TIME_OUT_BIOMETRIC = 0
private const val KEY_SIZE = 256
package com.otus.securehomework.data.biometrics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
import android.security.keystore.KeyProperties.BLOCK_MODE_GCM
import android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultKeySpecProvider @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : KeySpecProvider {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun provideKeyGenParameterSpec(keyAlias: String): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE_GCM) // Устанавливаем режим блоков GCM
            .setEncryptionPaddings(ENCRYPTION_PADDING_NONE) // Без заполнения
            .setKeySize(KEY_SIZE) // Устанавливаем размер ключа
            .setUserAuthenticationRequired(true) // Требуется аутентификация пользователя для использования ключа
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUnlockedDeviceRequired(true) // Ключ можно использовать только если устройство разблокировано

                    val hasStringBox = applicationContext
                        .packageManager
                        .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE) // Проверка наличия StrongBox для безопасного хранения ключей
                    setIsStrongBoxBacked(hasStringBox) // Если StrongBox доступен, ключ будет в нем храниться
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(
                        0,
                        AUTH_BIOMETRIC_STRONG
                    ) // Требование аутентификации с использованием биометрии
                }
            }
        return builder.build()
    }

    companion object {
        private const val KEY_SIZE = 256 // Размер ключа в битах (256 бит)

    }
}
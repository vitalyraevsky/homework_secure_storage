package com.otus.securehomework.data.crypto

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class BiometricCipher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStore: KeyStore
) {

    fun cipher(): Cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getKey())
        }

    private fun getKey(): SecretKey = keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateKey()

    private fun generateKey() = KeyGenerator.getInstance(KEY_ALGORITHM_AES, keyStore.type).apply {
        init(
            KeyGenParameterSpec.Builder(
                AES_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(BLOCK_MODE_CBC)
                .setEncryptionPaddings(PADDING_PKCS7)
                .setKeySize(KEY_SIZE)
                .setUserAuthenticationRequired(true)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        setUnlockedDeviceRequired(true)

                        val hasStringBox = context
                            .packageManager
                            .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

                        setIsStrongBoxBacked(hasStringBox)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                    }
                }.build()        )
    }.generateKey()

    companion object {

        private const val KEY_ALGORITHM_AES = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE_CBC = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING_PKCS7 = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val AES_TRANSFORMATION = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$PADDING_PKCS7"
        private const val AES_KEY_ALIAS = "AES_BIOMETRIC_KEY"
        private const val KEY_SIZE = 256
    }

}
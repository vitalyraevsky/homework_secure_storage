package com.otus.securehomework.data.biometrik

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.otus.securehomework.data.biometrik.impl.BiometricCipher
import com.otus.securehomework.data.security.Constants.AES_KEY_ALIAS
import com.otus.securehomework.data.security.Constants.AES_TRANSFORMATION
import com.otus.securehomework.data.security.Constants.BLOCK_MODE_CBC
import com.otus.securehomework.data.security.Constants.ENCRYPTION_PADDING_PKCS7
import com.otus.securehomework.data.security.Constants.KEY_ALGORITHM_AES
import com.otus.securehomework.data.security.Constants.KEY_SIZE
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
class BiometricCipherImpl  @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStore: KeyStore
): BiometricCipher {

    override fun cipher(): Cipher = Cipher.getInstance(AES_TRANSFORMATION).apply {
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
                .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7)
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

}
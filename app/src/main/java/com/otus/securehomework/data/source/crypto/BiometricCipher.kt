package com.otus.securehomework.data.source.crypto

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject


class BiometricCipher @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {
    private val keyAlias by lazy { "${applicationContext.packageName}.biometricKey" }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getEncryptor(): BiometricPrompt.CryptoObject = BiometricPrompt.CryptoObject(
        Cipher.getInstance(TRANSFORMATION)
            .apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }
    )

    @RequiresApi(Build.VERSION_CODES.M)
    fun getDecryptor(iv: ByteArray): BiometricPrompt.CryptoObject {
        return BiometricPrompt.CryptoObject(
            Cipher.getInstance(TRANSFORMATION)
                .apply {
                    init(
                        Cipher.DECRYPT_MODE,
                        getOrCreateKey(),
                        GCMParameterSpec(AUTH_TAG_SIZE, iv)
                    )
                }
        )
    }

    fun encrypt(plaintext: String, encryptor: Cipher): EncryptedEntity {
        require(plaintext.isNotEmpty()) { "Plaintext cannot be empty" }
        val ciphertext = encryptor.doFinal(plaintext.toByteArray())
        return EncryptedEntity(
            ciphertext,
            encryptor.iv
        )
    }

    fun decrypt(ciphertext: ByteArray, decryptor: Cipher): String {
        val plaintext = decryptor.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateKey(): SecretKey {
        val keystore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

        keystore.getKey(keyAlias, null)?.let { return it as SecretKey }

        val keySpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setUnlockedDeviceRequired(true)

                    val hasStringBox = applicationContext
                        .packageManager
                        .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

                    setIsStrongBoxBacked(hasStringBox)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                }
            }
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            .apply { init(keySpec) }
            .generateKey()
    }

    data class EncryptedEntity(
        val ciphertext: ByteArray,
        val iv: ByteArray
    )

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val AUTH_TAG_SIZE = 128
        private const val KEY_SIZE = 256

        private const val TRANSFORMATION = "${KeyProperties.KEY_ALGORITHM_AES}/" +
                "${KeyProperties.BLOCK_MODE_GCM}/" +
                "${KeyProperties.ENCRYPTION_PADDING_NONE}"
    }
}
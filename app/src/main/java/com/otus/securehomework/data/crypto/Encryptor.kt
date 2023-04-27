package com.otus.securehomework.data.crypto

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Base64
import com.otus.securehomework.data.crypto.key_manager.KeyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Encryptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyManager: KeyManager
) {

    private val ivSpec by lazy { getInitializationVector() }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, keyManager.getSecretKey(), ivSpec)
        val encodedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encodedBytes, Base64.NO_WRAP)
    }

    fun decrypt(encrypted: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, keyManager.getSecretKey(), ivSpec)
        val encodedBytes = Base64.decode(encrypted, Base64.NO_WRAP)
        val decodedBytes = cipher.doFinal(encodedBytes)
        return String(decodedBytes, Charsets.UTF_8)
    }

    @SuppressLint("HardwareIds")
    private fun getInitializationVector(): AlgorithmParameterSpec {
        val deviceID = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return IvParameterSpec(deviceID.toByteArray())
    }

    companion object {
        private const val AES_MODE = "AES/CBC/PKCS7Padding"
    }

}
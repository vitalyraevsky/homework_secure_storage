package com.otus.securehomework.data.source.crypto

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class Keys @Inject constructor(
    @ApplicationContext val context: Context
) {

    fun getMasterKey() = MasterKey.Builder(context)
        .setSpecs()
        .build()

    private fun MasterKey.Builder.setSpecs(): MasterKey.Builder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder(
                    MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_LENGTH)
                    .build()
            )
        } else {
            setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        }

    companion object {
        const val KEY_LENGTH = 256
    }
}
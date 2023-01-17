package com.otus.securehomework.data.security

import android.security.keystore.KeyProperties

object Constants {
     const val KEY_PROVIDER = "AndroidKeyStore"
     const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences"
     const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName"
     const val RSA_MODE_LESS_THAN_M = "RSA/ECB/PKCS1Padding"
     const val RSA_KEY_ALIAS = "RSA_DEMO"
     const val RSA_ALGORITHM = "RSA"
     const val KEY_ALGORITHM_AES = KeyProperties.KEY_ALGORITHM_AES
     const val BLOCK_MODE_CBC = KeyProperties.BLOCK_MODE_CBC
     const val ENCRYPTION_PADDING_PKCS7 = KeyProperties.ENCRYPTION_PADDING_PKCS7
     const val AES_TRANSFORMATION = "$KEY_ALGORITHM_AES/$BLOCK_MODE_CBC/$ENCRYPTION_PADDING_PKCS7"
     const val AES_KEY_ALIAS = "AES_BIOMETRIC_KEY"
     const val KEY_SIZE = 256
}
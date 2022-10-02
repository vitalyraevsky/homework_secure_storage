package com.otus.securehomework.data.settings

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppSettings @Inject constructor(
    @ApplicationContext
    private val applicationContext: Context
) {

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    var useBiometry: Boolean
        get() = sharedPreferences.getBoolean(USE_BIOMETRY_KEY, true)
        set(value) = sharedPreferences.edit {
            putBoolean(USE_BIOMETRY_KEY, value)
        }

    private companion object {
        const val SHARED_PREFS_FILE_NAME = "app_settings"
        const val USE_BIOMETRY_KEY = "use_biometry"
    }
}
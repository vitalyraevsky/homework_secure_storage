package com.otus.securehomework.presentation.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.otus.securehomework.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }
}
package com.dzadafa.gitreak

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("GITREAK_PREFS", MODE_PRIVATE)
        val token = prefs.getString("GITHUB_TOKEN", null)

        val targetActivity = if (token.isNullOrEmpty()) {
            LoginActivity::class.java
        } else {
            MainActivity::class.java
        }

        startActivity(Intent(this, targetActivity))
        finish()
    }
}

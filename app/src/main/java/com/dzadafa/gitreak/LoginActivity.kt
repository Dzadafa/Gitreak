package com.dzadafa.gitreak

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dzadafa.gitreak.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val token = binding.etToken.text.toString()

            if (username.isNotEmpty() && token.isNotEmpty()) {
                saveCredentialsAndProceed(username, token)
            }
        }
    }

    private fun saveCredentialsAndProceed(username: String, token: String) {
        val prefs = getSharedPreferences("GITREAK_PREFS", MODE_PRIVATE)
        prefs.edit()
            .putString("GITHUB_USERNAME", username)
            .putString("GITHUB_TOKEN", token)
            .apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

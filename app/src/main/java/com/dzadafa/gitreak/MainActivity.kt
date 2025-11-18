package com.dzadafa.gitreak

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dzadafa.gitreak.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var freezeManager: StreakFreezeManager

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
            } else {
                Toast.makeText(this, "Notifications permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        freezeManager = StreakFreezeManager(this)
        setupObservers()
        askNotificationPermission()
        setupFreezeButton()
        updateFreezeUI()

        binding.progressBar.visibility = View.VISIBLE
        viewModel.fetchData()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupFreezeButton() {
        binding.btnFreeze.setOnClickListener {
            val prefs = getSharedPreferences("GITREAK_PREFS", Context.MODE_PRIVATE)
            val token = prefs.getString("GITHUB_TOKEN", null)
            val username = prefs.getString("GITHUB_USERNAME", null)

            if (token != null && username != null) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnFreeze.isEnabled = false
                
                lifecycleScope.launch {
                    val success = freezeManager.useFreeze(username, token)
                    binding.progressBar.visibility = View.GONE
                    binding.btnFreeze.isEnabled = true
                    
                    if (success) {
                        Toast.makeText(this@MainActivity, "Streak Frozen! Refreshing...", Toast.LENGTH_LONG).show()
                        updateFreezeUI()
                        viewModel.fetchData()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed. Check 'repo' scope.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateFreezeUI() {
        val remaining = freezeManager.getRemainingFreezes()
        binding.tvFreezeCount.text = "Freezes remaining this week: $remaining/2"
        
        binding.btnFreeze.isEnabled = remaining > 0
        if (remaining == 0) {
            binding.btnFreeze.alpha = 0.5f
        }
    }

    private fun setupObservers() {
        viewModel.streakInfo.observe(this) { info ->
            binding.progressBar.visibility = View.GONE
            binding.cardStreak.visibility = View.VISIBLE
            binding.cardLastActivity.visibility = View.VISIBLE
            binding.cardDetails.visibility = View.VISIBLE

            val displayStreak = if (info.contributedToday) info.streakCount + 1 else info.streakCount
            val streakText = "Streak: $displayStreak"
            binding.tvStreak.text = streakText

            if (displayStreak == 0) {
                binding.tvStreak.text = "No Streak"
                binding.tvStreakStatus.text = "Start your streak today!"
                binding.ivStreakIcon.setImageResource(R.drawable.ic_fire_off)
                binding.btnFreeze.visibility = View.VISIBLE 
                binding.tvFreezeCount.visibility = View.VISIBLE
            } else if (info.contributedToday) {
                binding.tvStreakStatus.text = "Today: Contributed!"
                binding.ivStreakIcon.setImageResource(R.drawable.ic_fire)
                binding.btnFreeze.visibility = View.GONE
                binding.tvFreezeCount.visibility = View.GONE
            } else {
                binding.tvStreakStatus.text = "Today: Pending"
                binding.ivStreakIcon.setImageResource(R.drawable.ic_fire_off)
                binding.btnFreeze.visibility = View.GONE
                binding.tvFreezeCount.visibility = View.GONE
            }
        }

        viewModel.lastActivity.observe(this) { activityText ->
            binding.tvLastActivity.text = activityText
        }

        viewModel.detailedContributions.observe(this) { detailsText ->
            binding.tvDetailed.text = detailsText
        }

        viewModel.error.observe(this) { errorText ->
            binding.progressBar.visibility = View.GONE
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = errorText
            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show()
        }
    }
}

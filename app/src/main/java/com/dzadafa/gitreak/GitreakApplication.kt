package com.dzadafa.gitreak

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class GitreakApplication : Application() {

    companion object {
        const val CHANNEL_ID = "GITREAK_STREAK_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupRecurringWork()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Streak Reminders"
            val descriptionText = "Reminders to keep your streak alive"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupRecurringWork() {
        val workRequest = PeriodicWorkRequestBuilder<StreakUpdateWorker>(
            4, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "StreakUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

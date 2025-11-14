package com.dzadafa.gitreak

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class GitreakApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
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

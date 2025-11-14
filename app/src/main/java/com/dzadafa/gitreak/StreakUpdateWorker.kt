package com.dzadafa.gitreak

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dzadafa.gitreak.data.ContributionDay
import com.dzadafa.gitreak.data.GraphqlQuery
import com.dzadafa.gitreak.data.RetrofitClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StreakUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = context.getSharedPreferences("GITREAK_PREFS", Context.MODE_PRIVATE)
        val token = prefs.getString("GITHUB_TOKEN", null)
        val username = prefs.getString("GITHUB_USERNAME", null)

        if (token == null || username == null) {
            return Result.failure()
        }

        return try {
            fetchContributionStreak("Bearer $token", username)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun fetchContributionStreak(authHeader: String, username: String) {
        val query = """
            query {
              user(login: "$username") {
                contributionsCollection {
                  contributionCalendar {
                    weeks {
                      contributionDays {
                        contributionCount
                        date
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val response = RetrofitClient.instance.getContributionData(
            token = authHeader,
            query = GraphqlQuery(query)
        )

        if (response.isSuccessful) {
            val calendar = response.body()?.data?.user?.contributionsCollection?.contributionCalendar
            val allDays = calendar?.weeks.orEmpty().flatMap { it.contributionDays }
            val info = calculateStreakInfo(allDays)

            val prefs = context.getSharedPreferences("GITREAK_PREFS", Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(MainViewModel.PREF_STREAK_COUNT, info.streakCount)
                .putBoolean(MainViewModel.PREF_CONTRIBUTED_TODAY, info.contributedToday)
                .apply()

            sendDataUpdatedBroadcast()
        }
    }

    private fun calculateStreakInfo(days: List<ContributionDay>): StreakInfo {
        if (days.isEmpty()) return StreakInfo(0, false)

        val sortedDays = days.sortedByDescending { it.date }
        val todayDate = LocalDate.now()
        val yesterdayDate = todayDate.minusDays(1)

        val todayEntry = sortedDays.firstOrNull { it.date == todayDate.toString() }
        val contributedToday = (todayEntry?.contributionCount ?: 0) > 0

        var currentStreak = 0
        var expectedDate = yesterdayDate
        val pastDays = sortedDays.filter { it.date != todayDate.toString() }

        for (day in pastDays) {
            val dayDate = LocalDate.parse(day.date, DateTimeFormatter.ISO_LOCAL_DATE)
            if (dayDate.isAfter(expectedDate)) continue
            if (dayDate.isEqual(expectedDate)) {
                if (day.contributionCount > 0) {
                    currentStreak++
                    expectedDate = expectedDate.minusDays(1)
                } else {
                    break
                }
            } else {
                break
            }
        }
        return StreakInfo(currentStreak, contributedToday)
    }

    private fun sendDataUpdatedBroadcast() {
        val intent = Intent(context, GitreakWidget::class.java).apply {
            action = MainViewModel.ACTION_STREAK_UPDATED
        }
        context.sendBroadcast(intent)
    }
}

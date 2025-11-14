package com.dzadafa.gitreak

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dzadafa.gitreak.data.ContributionDay
import com.dzadafa.gitreak.data.GithubApiService
import com.dzadafa.gitreak.data.GraphqlQuery
import com.dzadafa.gitreak.data.RetrofitClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class StreakInfo(val streakCount: Int, val contributedToday: Boolean)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: GithubApiService = RetrofitClient.instance

    private val _streakInfo = MutableLiveData<StreakInfo>()
    val streakInfo: LiveData<StreakInfo> = _streakInfo

    private val _lastActivity = MutableLiveData<String>()
    val lastActivity: LiveData<String> = _lastActivity

    private val _detailedContributions = MutableLiveData<String>()
    val detailedContributions: LiveData<String> = _detailedContributions

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val prefs = application.getSharedPreferences("GITREAK_PREFS", Context.MODE_PRIVATE)
    private val token = prefs.getString("GITHUB_TOKEN", null)
    private val username = prefs.getString("GITHUB_USERNAME", null)

    companion object {
        const val ACTION_STREAK_UPDATED = "com.dzadafa.gitreak.ACTION_STREAK_UPDATED"
        const val PREF_STREAK_COUNT = "WIDGET_STREAK_COUNT"
        const val PREF_CONTRIBUTED_TODAY = "WIDGET_CONTRIBUTED_TODAY"
    }

    fun fetchData() {
        if (token == null || username == null) {
            _error.postValue("User is not logged in.")
            return
        }

        val authHeader = "Bearer $token"

        viewModelScope.launch {
            fetchContributionStreak(authHeader, username)
            fetchLastActivity(authHeader, username)
        }
    }

    private suspend fun fetchContributionStreak(authHeader: String, username: String) {
        val query = """
            query {
              user(login: "$username") {
                contributionsCollection {
                  contributionCalendar {
                    totalContributions
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

        try {
            val response = apiService.getContributionData(token = authHeader, query = GraphqlQuery(query))
            if (response.isSuccessful) {
                val calendar = response.body()?.data?.user?.contributionsCollection?.contributionCalendar
                val allDays = calendar?.weeks.orEmpty().flatMap { it.contributionDays }

                val info = calculateStreakInfo(allDays)
                _streakInfo.postValue(info)

                prefs.edit()
                    .putInt(PREF_STREAK_COUNT, info.streakCount)
                    .putBoolean(PREF_CONTRIBUTED_TODAY, info.contributedToday)
                    .apply()

                sendDataUpdatedBroadcast()

                _detailedContributions.postValue("Total Contributions (Last Year): ${calendar?.totalContributions ?: 0}")
            } else {
                _error.postValue("Error fetching contributions: ${response.message()}")
            }
        } catch (e: Exception) {
            _error.postValue("Exception: ${e.message}")
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

            if (dayDate.isAfter(expectedDate)) {
                continue
            }

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

    private suspend fun fetchLastActivity(authHeader: String, username: String) {
        try {
            val response = apiService.getPublicEvents(token = authHeader, username = username)
            if (response.isSuccessful && response.body() != null) {
                val lastEvent = response.body()?.firstOrNull()
                if (lastEvent != null) {
                    _lastActivity.postValue("Last Activity: ${lastEvent.type} on ${lastEvent.repo?.name}")
                } else {
                    _lastActivity.postValue("No recent public activity found.")
                }
            } else {
                _error.postValue("Error fetching activity: ${response.message()}")
            }
        } catch (e: Exception) {
            _error.postValue("Exception: ${e.message}")
        }
    }

    private fun sendDataUpdatedBroadcast() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, GitreakWidget::class.java).apply {
            action = ACTION_STREAK_UPDATED
        }
        context.sendBroadcast(intent)
    }
}

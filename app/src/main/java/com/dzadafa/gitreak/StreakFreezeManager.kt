package com.dzadafa.gitreak

import android.content.Context
import android.content.SharedPreferences
import com.dzadafa.gitreak.data.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class StreakFreezeManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("GITREAK_FREEZE_PREFS", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FREEZE_LOG = "freeze_usage_log"
        private const val BRANCH = "main"
        private const val HISTORY_FOLDER = "gitreak_history"
    }

    fun isDateFrozen(date: LocalDate): Boolean {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return getFreezeLog().contains(dateString)
    }

    suspend fun syncHistory(username: String, token: String) {
        try {
            val authHeader = "Bearer $token"
            val response = RetrofitClient.instance.getContents(authHeader, username, username, HISTORY_FOLDER)
            
            if (response.isSuccessful && response.body() != null) {
                val remoteLog = mutableSetOf<String>()
                response.body()!!.forEach { file ->
                    if (file.name.startsWith("freeze_") && file.name.endsWith(".txt")) {
                        val datePart = file.name.removePrefix("freeze_").removeSuffix(".txt")
                        remoteLog.add(datePart)
                    }
                }
                prefs.edit().putStringSet(KEY_FREEZE_LOG, remoteLog).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRemainingFreezes(): Int {
        val log = getFreezeLog()
        val currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        
        val usedThisWeek = log.count { 
            val date = try {
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) { LocalDate.MIN }
            !date.isBefore(currentWeekStart)
        }

        return (2 - usedThisWeek).coerceAtLeast(0)
    }

    suspend fun useFreeze(username: String, token: String): Boolean {
        if (getRemainingFreezes() <= 0) return false

        try {
            val authHeader = "Bearer $token"
            val api = RetrofitClient.instance
            val repoName = username

            val refResponse = api.getRef(authHeader, username, repoName, BRANCH)
            if (!refResponse.isSuccessful) return false
            val latestCommitSha = refResponse.body()!!.`object`.sha

            val yesterday = LocalDateTime.now().minusDays(1)
            val dateString = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val content = "Streak Freeze used for $dateString. Kept the streak alive!"
            val blobResponse = api.createBlob(authHeader, username, repoName, CreateBlobRequest(content))
            val blobSha = blobResponse.body()!!.sha

            val treeElement = TreeElement(
                path = "$HISTORY_FOLDER/freeze_$dateString.txt",
                sha = blobSha
            )
            val treeResponse = api.createTree(authHeader, username, repoName, CreateTreeRequest(base_tree = latestCommitSha, tree = listOf(treeElement)))
            val treeSha = treeResponse.body()!!.sha

            val isoDate = yesterday.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val user = CommitUser(username, "$username@users.noreply.github.com", isoDate)
            
            val commitReq = CreateCommitRequest(
                message = "❄️ Gitreak: Freeze used for $dateString",
                tree = treeSha,
                parents = listOf(latestCommitSha),
                author = user,
                committer = user
            )
            val commitResponse = api.createCommit(authHeader, username, repoName, commitReq)
            val newCommitSha = commitResponse.body()!!.sha

            val updateResponse = api.updateRef(authHeader, username, repoName, BRANCH, UpdateRefRequest(sha = newCommitSha))
            
            if (updateResponse.isSuccessful) {
                recordUsage(dateString)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun getFreezeLog(): MutableSet<String> {
        return prefs.getStringSet(KEY_FREEZE_LOG, mutableSetOf()) ?: mutableSetOf()
    }

    private fun recordUsage(date: String) {
        val log = getFreezeLog()
        log.add(date)
        prefs.edit().putStringSet(KEY_FREEZE_LOG, log).apply()
    }
}

package com.dzadafa.gitreak.data

import com.google.gson.annotations.SerializedName

data class GraphqlQuery(val query: String)

data class ContributionData(
    val data: UserData?
)

data class UserData(
    val user: User?
)

data class User(
    val contributionsCollection: ContributionsCollection?
)

data class ContributionsCollection(
    val contributionCalendar: ContributionCalendar?
)

data class ContributionCalendar(
    val totalContributions: Int,
    val weeks: List<Week>
)

data class Week(
    val contributionDays: List<ContributionDay>
)

data class ContributionDay(
    val contributionCount: Int,
    val date: String
)

data class GithubEvent(
    val type: String,
    @SerializedName("created_at")
    val createdAt: String,
    val repo: Repo?,
    val payload: EventPayload?
)

data class Repo(
    val name: String
)

data class EventPayload(
    val action: String?,
    val commits: List<Commit>?,
    val size: Int?,
    val pullRequest: PullRequest?,
    @SerializedName("ref_type")
    val refType: String?,
    @SerializedName("pusher_type")
    val pusherType: String?
)

data class Commit(
    val sha: String,
    val message: String
)

data class PullRequest(
    val title: String,
    val number: Int
)

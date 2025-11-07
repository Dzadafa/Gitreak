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
    val repo: Repo?
)

data class Repo(
    val name: String
)

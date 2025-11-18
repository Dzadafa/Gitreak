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

data class GitReference(
    val ref: String,
    val node_id: String,
    val url: String,
    val `object`: GitObject
)

data class GitObject(
    val sha: String,
    val type: String,
    val url: String
)

data class CreateBlobRequest(
    val content: String,
    val encoding: String = "utf-8"
)

data class GitBlob(val sha: String)

data class CreateTreeRequest(
    val base_tree: String,
    val tree: List<TreeElement>
)

data class TreeElement(
    val path: String,
    val mode: String = "100644", 
    val type: String = "blob",
    val sha: String
)

data class GitTree(val sha: String)

data class CreateCommitRequest(
    val message: String,
    val tree: String,
    val parents: List<String>,
    val author: CommitUser,
    val committer: CommitUser
)

data class CommitUser(
    val name: String,
    val email: String,
    val date: String 
)

data class GitCommit(val sha: String)

data class UpdateRefRequest(
    val sha: String,
    val force: Boolean = false
)

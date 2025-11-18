package com.dzadafa.gitreak.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

interface GithubApiService {

    @POST
    suspend fun getContributionData(
        @Url url: String = "https://api.github.com/graphql",
        @Header("Authorization") token: String,
        @Body query: GraphqlQuery
    ): Response<ContributionData>

    @GET("users/{username}/events")
    suspend fun getPublicEvents(
        @Header("Authorization") token: String,
        @Path("username") username: String
    ): Response<List<GithubEvent>>

    @GET("repos/{owner}/{repo}/git/ref/heads/{branch}")
    suspend fun getRef(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String
    ): Response<GitReference>

    @POST("repos/{owner}/{repo}/git/commits")
    suspend fun createCommit(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body commit: CreateCommitRequest
    ): Response<GitCommit>

    @PATCH("repos/{owner}/{repo}/git/refs/heads/{branch}")
    suspend fun updateRef(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String,
        @Body update: UpdateRefRequest
    ): Response<GitReference>

    @POST("repos/{owner}/{repo}/git/blobs")
    suspend fun createBlob(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body blob: CreateBlobRequest
    ): Response<GitBlob>

    @POST("repos/{owner}/{repo}/git/trees")
    suspend fun createTree(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body tree: CreateTreeRequest
    ): Response<GitTree>
    
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContents(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String
    ): Response<List<RepoContent>>
}

data class RepoContent(
    val name: String,
    val path: String,
    val sha: String,
    val type: String
)

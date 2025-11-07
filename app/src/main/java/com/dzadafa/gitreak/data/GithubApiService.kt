package com.dzadafa.gitreak.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
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
}

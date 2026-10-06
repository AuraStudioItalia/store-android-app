package com.example.data.remote

import com.example.data.model.GitHubContentItem
import retrofit2.http.GET
import retrofit2.http.Url

interface GitHubApiService {
    @GET("repos/AuraStudioItalia/aura-store/contents/repo")
    suspend fun getRepoContents(): List<GitHubContentItem>

    @GET
    suspend fun RawDataJson(@Url url: String): String
}

package com.maxi.newsonlyokhttp.data.sources.remote

import com.maxi.newsonlyokhttp.data.sources.remote.api.NetworkApiService
import com.maxi.newsonlyokhttp.data.sources.remote.dto.SourcesResponseDto
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: NetworkApiService
) {

    suspend fun getNewsSources(
        forceRefresh: Boolean = false
    ): Response<SourcesResponseDto> {
        return apiService.getNewsSources(forceRefresh = if (forceRefresh) "true" else null)
    }
}

/**
 * Sending the header at all when it's false is unnecessary noise.
 * The cleaner approach is to only add the header when it is actually needed — and the idiomatic way in Retrofit is to pass null to omit the header entirely.
 * When Retrofit sees a null value for a @Header parameter it omits the header from the request entirely, so the interceptor won't find it and will follow
 * the normal cache path. When it is "true" the interceptor picks it up and forces a network call.
 * This also means your interceptor check remains clean and semantically correct — the header either exists with value "true", or it doesn't exist at all.
 */
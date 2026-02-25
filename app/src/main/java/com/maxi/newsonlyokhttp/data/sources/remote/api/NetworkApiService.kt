package com.maxi.newsonlyokhttp.data.sources.remote.api

import com.maxi.newsonlyokhttp.data.common.DataConstants.EndPoints.SOURCES
import com.maxi.newsonlyokhttp.data.common.DataConstants.Headers.X_FORCE_REFRESH
import com.maxi.newsonlyokhttp.data.sources.remote.dto.SourcesResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface NetworkApiService {

    @GET(SOURCES)
    suspend fun getNewsSources(
        @Header(X_FORCE_REFRESH) forceRefresh: String? = null
    ): Response<SourcesResponseDto>
}
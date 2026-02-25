package com.maxi.newsonlyokhttp.domain.repository

import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.domain.model.NewsSource
import kotlinx.coroutines.flow.Flow

interface NewsSourcesRepository {

    fun getNewsSources(
        forceRefresh: Boolean
    ): Flow<Resource<List<NewsSource>>>
}
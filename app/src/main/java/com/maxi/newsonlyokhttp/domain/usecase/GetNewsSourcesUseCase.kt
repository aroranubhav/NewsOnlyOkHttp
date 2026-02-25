package com.maxi.newsonlyokhttp.domain.usecase

import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.domain.model.NewsSource
import com.maxi.newsonlyokhttp.domain.repository.NewsSourcesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface GetNewsSourcesUseCase {

    fun getNewsSources(forceRefresh: Boolean): Flow<Resource<List<NewsSource>>>
}

class DefaultGetNewsSourcesUseCase @Inject constructor(
    private val repository: NewsSourcesRepository
) : GetNewsSourcesUseCase {

    override fun getNewsSources(forceRefresh: Boolean): Flow<Resource<List<NewsSource>>> =
        repository.getNewsSources(forceRefresh)
}
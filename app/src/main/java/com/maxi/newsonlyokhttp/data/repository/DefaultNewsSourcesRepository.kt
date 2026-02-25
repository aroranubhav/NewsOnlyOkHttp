package com.maxi.newsonlyokhttp.data.repository

import com.maxi.newsonlyokhttp.common.DispatcherProvider
import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.data.common.safeApiCall
import com.maxi.newsonlyokhttp.data.mappers.toDomainList
import com.maxi.newsonlyokhttp.data.sources.remote.RemoteDataSource
import com.maxi.newsonlyokhttp.domain.model.NewsSource
import com.maxi.newsonlyokhttp.domain.repository.NewsSourcesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class DefaultNewsSourcesRepository @Inject constructor(
    private val remote: RemoteDataSource,
    private val dispatchers: DispatcherProvider
) : NewsSourcesRepository {

    override fun getNewsSources(forceRefresh: Boolean): Flow<Resource<List<NewsSource>>> = flow {
        emit(Resource.Loading)

        val response = safeApiCall {
            remote.getNewsSources(forceRefresh)
        }

        emit(
            when (response) {
                is Resource.Success -> Resource.Success(
                    response.data.sources.toDomainList()
                )

                else -> response as Resource<List<NewsSource>>
            }
        )
    }.flowOn(dispatchers.io)
}

/**
 * The cast is safe here because Error, Loading, and NoChange are all Resource<Nothing>
 * which is a valid subtype of Resource<R> for any R.
 * The UNCHECKED_CAST suppress is just to satisfy the compiler since it can't
 * verify the variance at the cast site.
 */
fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> =
    when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        else -> @Suppress("UNCHECKED_CAST") (this as Resource<R>)
    }
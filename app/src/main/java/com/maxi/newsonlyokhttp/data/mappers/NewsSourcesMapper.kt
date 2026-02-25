package com.maxi.newsonlyokhttp.data.mappers

import com.maxi.newsonlyokhttp.data.sources.remote.dto.NewsSourceDto
import com.maxi.newsonlyokhttp.domain.model.NewsSource

fun NewsSourceDto.toDomain(): NewsSource =
    NewsSource(id, name, description, url)

fun List<NewsSourceDto>.toDomainList(): List<NewsSource> = map {
    it.toDomain()
}
package com.maxi.newsonlyokhttp.data.sources.remote.dto

import com.maxi.newsonlyokhttp.data.common.DataConstants.Keys as Keys
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourcesResponseDto(
    @SerialName(Keys.STATUS)
    val status: String,
    @SerialName(Keys.SOURCES)
    val sources: List<NewsSourceDto>
)

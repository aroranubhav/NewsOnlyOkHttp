package com.maxi.newsonlyokhttp.data.sources.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.maxi.newsonlyokhttp.data.common.DataConstants.Keys as Keys

@Serializable
data class NewsSourceDto(
    @SerialName(Keys.ID)
    val id: String,
    @SerialName(Keys.NAME)
    val name: String,
    @SerialName(Keys.DESCRIPTION)
    val description: String,
    @SerialName(Keys.URL)
    val url: String
)

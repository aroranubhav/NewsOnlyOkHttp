package com.maxi.newsonlyokhttp.ui.common

import com.maxi.newsonlyokhttp.domain.model.NewsSource

sealed interface NewsSourcesUiState {

    data class Success(val data: List<NewsSource>) : NewsSourcesUiState

    data object Loading : NewsSourcesUiState

    data object Empty : NewsSourcesUiState
}
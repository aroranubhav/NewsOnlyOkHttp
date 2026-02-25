package com.maxi.newsonlyokhttp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxi.newsonlyokhttp.common.DispatcherProvider
import com.maxi.newsonlyokhttp.common.ErrorType
import com.maxi.newsonlyokhttp.common.NetworkConnectivityHelper
import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.domain.usecase.GetNewsSourcesUseCase
import com.maxi.newsonlyokhttp.ui.common.UiConstants
import com.maxi.newsonlyokhttp.ui.common.NewsSourcesUiState
import com.maxi.newsonlyokhttp.ui.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewSourcesViewModel @Inject constructor(
    private val useCase: GetNewsSourcesUseCase,
    private val dispatchers: DispatcherProvider,
    private val connectivityHelper: NetworkConnectivityHelper
) : ViewModel() {

    //TODO: Do it the modern way!!
    private val _uiState = MutableStateFlow<NewsSourcesUiState>(NewsSourcesUiState.Empty)
    val uiState get() = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(replay = 0)
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        getNewsSources(false)
    }

    private fun getNewsSources(forceRefresh: Boolean) {
        useCase
            .getNewsSources(forceRefresh)
            .flowOn(dispatchers.io)
            .onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val sources = resource.data
                        _uiState.value = if (sources.isEmpty()) {
                            NewsSourcesUiState.Empty
                        } else {
                            NewsSourcesUiState.Success(sources)
                        }
                    }

                    is Resource.Error -> {
                        val message = mapErrorTypeToMessage(resource.type)
                        emitUiEvent(UiEvent.Error(message))

                        val currentUiState = _uiState.value
                        _uiState.value = when (currentUiState) {
                            is NewsSourcesUiState.Success -> currentUiState
                            else -> NewsSourcesUiState.Empty
                        }
                    }

                    is Resource.NoChange -> {
                        emitUiEvent(UiEvent.NoChange())
                    }

                    is Resource.Loading -> {
                        Log.d(TAG, "Loading news sources!")
                    }
                }
            }
            .onStart {
                if (!forceRefresh) {
                    _uiState.value = NewsSourcesUiState.Loading
                }
            }
            .onCompletion {
                if (forceRefresh) {
                    emitUiEvent(UiEvent.RefreshCompleted())
                }
            }
            .launchIn(viewModelScope)
    }

    private fun mapErrorTypeToMessage(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.UNAUTHORISED -> UiConstants.AUTHORIZATION_FAILED
            ErrorType.FORBIDDEN -> UiConstants.FORBIDDEN_REQUEST
            ErrorType.NOT_FOUND -> UiConstants.NOT_FOUND
            ErrorType.SERVER_ERROR -> UiConstants.SERVER_ERROR
            ErrorType.NO_CONNECTIVITY -> UiConstants.NETWORK_ISSUE
            ErrorType.TIMEOUT -> UiConstants.CONNECTION_TIME_OUT
            ErrorType.UNKNOWN -> UiConstants.UNKNOWN_ERROR
        }
    }

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    fun refreshSources() {
        if (connectivityHelper.hasNetworkConnectivity()) {   // Optimistic connectivity check to avoid a network attempt we know will fail
            emitUiEvent(UiEvent.RefreshStarted())
            getNewsSources(true)
        } else {
            emitUiEvent(UiEvent.Error(UiConstants.NETWORK_ISSUE))
        }
    }

    fun showMessage(message: String) {
        emitUiEvent(UiEvent.Message(message))
    }
}

private const val TAG = "NewSourcesViewModelTAG"

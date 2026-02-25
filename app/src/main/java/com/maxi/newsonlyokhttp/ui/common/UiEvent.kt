package com.maxi.newsonlyokhttp.ui.common

sealed interface UiEvent {

    data class Error(val error: String): UiEvent

    data class NoChange(val message: String = "No change in data!"): UiEvent

    /*
       Additional Ui Events
     */
    data class Message(val message: String): UiEvent

    data class RefreshStarted(val message: String = "Refresh started!"): UiEvent

    data class RefreshCompleted(val message: String = "Refresh Completed!"): UiEvent

    //data class NavigateTo(val destination: NewsSourceDestination) : UiEvent
}

/**
 * The distinction between UiState and UiEvent is exactly the right pattern —
 * state is what the screen looks like, events are one-time side effects like snackbars, toasts, and navigation.
 * This is the MVI-adjacent pattern you see in most modern Android codebases.
 */
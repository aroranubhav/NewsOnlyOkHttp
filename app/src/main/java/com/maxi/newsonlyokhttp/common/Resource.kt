package com.maxi.newsonlyokhttp.common

sealed class Resource<out T> {

    data class Success<T>(val data: T) : Resource<T>()

    data class Error(
        val type: ErrorType,
        val message: String?,
        val code: Int? = null
    ) : Resource<Nothing>()

    data object Loading : Resource<Nothing>()

    data object NoChange: Resource<Nothing>()
}

enum class ErrorType {
    NO_CONNECTIVITY,  // TransportException.NoConnectivity
    TIMEOUT,          // TransportException.Timeout
    UNAUTHORISED,     // HttpException.Unauthorized
    FORBIDDEN,        // HttpException.Forbidden
    NOT_FOUND,        // HttpException.NotFound
    SERVER_ERROR,     // HttpException.ServerError
    UNKNOWN           // everything else
}

/**
 * Nothing is Kotlin's bottom type — it is a subtype of every type.
 * Combined with the out T covariance declaration on Resource, this means:
 * -- Resource<Nothing> is a subtype of Resource<T> for any T
 */
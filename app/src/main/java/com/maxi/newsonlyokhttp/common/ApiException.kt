package com.maxi.newsonlyokhttp.common

import java.io.IOException

open class ApiException(
    val errorCode: Int,
    val errorMessage: String?,
    val errorBody: String?,
    val requestMethod: String?,
    val requestUrl: String?
) : IOException() {

    override val message: String
        get() = buildString {
            append("HTTP Exception $errorCode")
            if (!errorMessage.isNullOrEmpty()) {
                append(" -- $errorMessage\n")
            }
            if (!requestMethod.isNullOrEmpty() && !requestUrl.isNullOrEmpty()) {
                append("[$requestMethod -- $requestUrl]\n")
            }
            append(errorBody)
        }
}

/** Error propagation flow
 *
 * HttpException.Unauthorised(errorMessage, errorBody, requestMethod, requestUrl)
 *         ↓ passes to
 * HttpException(401, errorMessage, errorBody, requestMethod, requestUrl)
 *         ↓ passes to
 * ApiException(401, errorMessage, errorBody, requestMethod, requestUrl)
 *         ↓ builds
 * "HTTP Exception 401 -- Unauthorized\n[GET - https://api.example.com/user]\n..."
 *
 * So the message override lives in one place — ApiException —
 * and all subclasses inherit it automatically. If you ever wanted a specific
 * subclass to format its message differently,
 * it could override message again and that would take precedence.
 */
sealed class HttpException(
    errorCode: Int,
    errorMessage: String?,
    errorBody: String?,
    requestMethod: String?,
    requestUrl: String?
) : ApiException(
    errorCode, errorMessage, errorBody, requestMethod, requestUrl
) {
    class Unauthorized(
        errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(401, errorMessage, errorBody, requestMethod, requestUrl)

    class Forbidden(
        errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(403, errorMessage, errorBody, requestMethod, requestUrl)

    class NotFound(
        errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(404, errorMessage, errorBody, requestMethod, requestUrl)

    class ServerError(
        errorCode: Int,
        errorMessage: String?,
        errorBody: String?,
        requestMethod: String?,
        requestUrl: String?
    ) : HttpException(errorCode, errorMessage, errorBody, requestMethod, requestUrl)

    class Unknown(
        errorCode: Int,
        errorMessage: String?,
        errorBody: String?,
        requestMethod: String?,
        requestUrl: String?
    ) : HttpException(errorCode, errorMessage, errorBody, requestMethod, requestUrl)
}

/**
 *
 * sealed class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause) {
 *     class NoConnectivity(cause: Throwable? = null) : NetworkException("No network connectivity", cause)
 *     class Unauthorised(message: String) : NetworkException(message)
 *     class Forbidden(message: String) : NetworkException(message)
 *     class NotFound(message: String) : NetworkException(message)
 *     class ServerError(message: String, val code: Int) : NetworkException(message)
 *     class Unknown(message: String, val code: Int) : NetworkException(message)
 * }
 */


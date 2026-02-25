package com.maxi.newsonlyokhttp.data.sources.remote.interceptor

import com.maxi.newsonlyokhttp.common.HttpException
import com.maxi.newsonlyokhttp.common.TransportException
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class ErrorResponse(
    val error: String? = null,
    val message: String? = null
)

class ErrorHandlingInterceptor(
    private val json: Json
) : Interceptor {

    companion object {

        private const val MAX_ERROR_BODY_BYTES = 64 * 1024L // 64 KB
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val networkResponse: Response

        try {
            networkResponse = chain.proceed(originalRequest)
        } catch (e: UnknownHostException) {
            throw TransportException.NoConnectivity(e)
        } catch (e: ConnectException) {
            throw TransportException.NoConnectivity(e)
        } catch (e: SocketTimeoutException) {
            throw TransportException.Timeout(e)
        } catch (e: IOException) {
            throw TransportException.Unknown(e)
        }

        if (!networkResponse.isSuccessful) {
            val errorBody = networkResponse.peekBody(MAX_ERROR_BODY_BYTES).string()
            val requestMethod = originalRequest.method
            val responseCode = networkResponse.code
            val requestUrl = originalRequest.url.toString()

            val parsedError = try {
                json.decodeFromString<ErrorResponse>(errorBody)
            } catch (e: Exception) {
                null
            }

            val errorMessage = parsedError?.let {
                it.error ?: it.message
            } ?: networkResponse.message.takeIf {
                it.isNotBlank()
            } ?: "Unknown error $responseCode"

            when (responseCode) {
                401 -> throw HttpException.Unauthorized(
                    errorMessage, errorBody, requestMethod, requestUrl
                )

                403 -> throw HttpException.Forbidden(
                    errorMessage, errorBody, requestMethod, requestUrl
                )

                404 -> throw HttpException.NotFound(
                    errorMessage, errorBody, requestMethod, requestUrl
                )

                in 500..599 -> throw HttpException.ServerError(
                    responseCode, errorMessage, errorBody, requestMethod, requestUrl
                )

                else -> throw HttpException.Unknown(
                    responseCode, errorMessage, errorBody, requestMethod, requestUrl
                )
            }
        }

        return networkResponse
    }
}
package com.maxi.newsonlyokhttp.data.sources.remote.interceptor

import com.maxi.newsonlyokhttp.data.common.DataConstants.Headers
import okhttp3.logging.HttpLoggingInterceptor

class HttpLoggingInterceptorFactory(
    private val isDebug: Boolean
) {

    fun create(): HttpLoggingInterceptor =
        HttpLoggingInterceptor()
            .apply {
                level = if (isDebug) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
                redactHeader(Headers.X_API_KEY)
                redactHeader(Headers.USER_AGENT)
            }
}
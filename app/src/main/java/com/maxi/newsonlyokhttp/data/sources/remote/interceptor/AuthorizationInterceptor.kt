package com.maxi.newsonlyokhttp.data.sources.remote.interceptor

import com.maxi.newsonlyokhttp.data.common.DataConstants.Headers
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
    private val apiKey: String,
    private val userAgent: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val updatedRequest = originalRequest
            .newBuilder()
            .header(Headers.X_API_KEY, apiKey)
            .header(Headers.USER_AGENT, userAgent)
            .build()

        return chain.proceed(updatedRequest)
    }
}
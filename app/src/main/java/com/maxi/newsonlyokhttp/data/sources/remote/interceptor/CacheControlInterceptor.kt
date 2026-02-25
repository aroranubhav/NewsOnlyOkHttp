package com.maxi.newsonlyokhttp.data.sources.remote.interceptor

import com.maxi.newsonlyokhttp.data.common.DataConstants.Headers.CACHE_CONTROL
import com.maxi.newsonlyokhttp.data.common.DataConstants.Headers.PRAGMA
import com.maxi.newsonlyokhttp.data.common.DataConstants.Headers.X_FORCE_REFRESH
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheControlInterceptor : Interceptor {

    companion object {
        private const val MAX_AGE_SECONDS = 86400 //1 day
        private const val MAX_STALE_SECONDS = 604800 //7 days
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.method != "GET") {
            return chain.proceed(originalRequest)
        }

        val forceRefresh = originalRequest.header(X_FORCE_REFRESH) == "true"

        val updatedRequest = originalRequest
            .newBuilder()
            .removeHeader(X_FORCE_REFRESH)
            .removeHeader(PRAGMA)
            .apply {
                if (forceRefresh) {
                    cacheControl(CacheControl.FORCE_NETWORK)
                } else {
                    cacheControl(
                        CacheControl.Builder()
                            .maxAge(MAX_AGE_SECONDS, TimeUnit.SECONDS)
                            .maxStale(MAX_STALE_SECONDS, TimeUnit.SECONDS)
                            .build()
                    )
                }
            }
            .build()

        val networkResponse = chain.proceed(updatedRequest)

        val cacheHeader = networkResponse.header(CACHE_CONTROL)

        val isCaching = cacheHeader != null &&
                !cacheHeader.contains("no-cache") && !cacheHeader.contains("no-store")

        return networkResponse
            .newBuilder()
            .removeHeader(PRAGMA)
            .apply { // API returns no-cache by default; overriding to enable client-side caching
                if (!isCaching) {
                    header(CACHE_CONTROL, "public, max-age=$MAX_AGE_SECONDS")
                }
            }.build()
    }
}
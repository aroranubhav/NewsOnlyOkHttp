package com.maxi.newsonlyokhttp.framework.di.module

import android.content.Context
import android.util.Base64
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.maxi.newsonlyokhttp.BuildConfig
import com.maxi.newsonlyokhttp.common.DefaultDispatcherProvider
import com.maxi.newsonlyokhttp.common.DefaultNetworkConnectivityHelper
import com.maxi.newsonlyokhttp.common.DispatcherProvider
import com.maxi.newsonlyokhttp.common.NetworkConnectivityHelper
import com.maxi.newsonlyokhttp.data.sources.remote.api.NetworkApiService
import com.maxi.newsonlyokhttp.data.sources.remote.interceptor.AuthorizationInterceptor
import com.maxi.newsonlyokhttp.data.sources.remote.interceptor.CacheControlInterceptor
import com.maxi.newsonlyokhttp.data.sources.remote.interceptor.ErrorHandlingInterceptor
import com.maxi.newsonlyokhttp.data.sources.remote.interceptor.HttpLoggingInterceptorFactory
import com.maxi.newsonlyokhttp.framework.di.module.AppModule.Constants.BASE_URL
import com.maxi.newsonlyokhttp.framework.di.module.AppModule.Constants.CONNECTION_TIME_OUT
import com.maxi.newsonlyokhttp.framework.di.module.AppModule.Constants.OKHTTP_CACHE
import com.maxi.newsonlyokhttp.framework.di.module.AppModule.Constants.READ_TIME_OUT
import com.maxi.newsonlyokhttp.framework.di.qualifier.ApiKey
import com.maxi.newsonlyokhttp.framework.di.qualifier.BaseUrl
import com.maxi.newsonlyokhttp.framework.di.qualifier.IsDebug
import com.maxi.newsonlyokhttp.framework.di.qualifier.UserAgent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private object Constants {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val OKHTTP_CACHE = "okhttp_cache"

        const val CONNECTION_TIME_OUT = 30L
        const val READ_TIME_OUT = 30L
    }

    @BaseUrl
    @Provides
    fun provideBaseUrl(): String =
        BASE_URL

    @ApiKey
    @Provides
    fun provideApiKey(): String =
        String(
            Base64
                .decode(
                    BuildConfig.API_KEY,
                    Base64.DEFAULT
                ),
            Charsets.UTF_8
        )

    @UserAgent
    @Provides
    fun provideUserAgent(): String =
        BuildConfig.USER_AGENT

    @IsDebug
    @Provides
    fun provideIsDebug(): Boolean =
        BuildConfig.DEBUG

    @Singleton
    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    @Singleton
    @Provides
    fun provideCache(
        @ApplicationContext context: Context
    ): Cache =
        Cache(
            File(
                context.cacheDir,
                OKHTTP_CACHE
            ),
            10L * 1024 * 1024
        )

    @Singleton
    @Provides
    fun provideAuthorizationInterceptor(
        @ApiKey apiKey: String,
        @UserAgent userAgent: String
    ): AuthorizationInterceptor =
        AuthorizationInterceptor(apiKey, userAgent)

    @Singleton
    @Provides
    fun provideCacheControlInterceptor(): CacheControlInterceptor =
        CacheControlInterceptor()

    @Singleton
    @Provides
    fun provideErrorHandlingInterceptor(
        json: Json
    ): ErrorHandlingInterceptor =
        ErrorHandlingInterceptor(json)

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(
        @IsDebug isDebug: Boolean
    ): HttpLoggingInterceptor =
        HttpLoggingInterceptorFactory(isDebug)
            .create()

    @Singleton
    @Provides
    fun provideHttpClient(
        cache: Cache,
        authorizationInterceptor: AuthorizationInterceptor,
        cacheControlInterceptor: CacheControlInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .cache(cache)
            .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(authorizationInterceptor)
            .addInterceptor(errorHandlingInterceptor)
            .addNetworkInterceptor(cacheControlInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        @BaseUrl baseUrl: String,
        httpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit
    }

    @Singleton
    @Provides
    fun provideNetworkApiService(
        retrofit: Retrofit
    ): NetworkApiService =
        retrofit.create(NetworkApiService::class.java)

    @Singleton
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider =
        DefaultDispatcherProvider()

    @Singleton
    @Provides
    fun provideNetworkConnectivityHelper(
        @ApplicationContext context: Context
    ): NetworkConnectivityHelper =
        DefaultNetworkConnectivityHelper(context)

}
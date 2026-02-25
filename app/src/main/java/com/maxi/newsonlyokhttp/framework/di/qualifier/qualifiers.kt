package com.maxi.newsonlyokhttp.framework.di.qualifier

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class ApiKey

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class UserAgent

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class IsDebug
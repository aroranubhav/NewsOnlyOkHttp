package com.maxi.newsonlyokhttp.framework.di.module

import com.maxi.newsonlyokhttp.data.repository.DefaultNewsSourcesRepository
import com.maxi.newsonlyokhttp.domain.repository.NewsSourcesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideNewsSourcesRepository(
        repository: DefaultNewsSourcesRepository
    ): NewsSourcesRepository
}
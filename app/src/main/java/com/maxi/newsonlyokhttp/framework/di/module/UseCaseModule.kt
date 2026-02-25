package com.maxi.newsonlyokhttp.framework.di.module

import com.maxi.newsonlyokhttp.domain.usecase.DefaultGetNewsSourcesUseCase
import com.maxi.newsonlyokhttp.domain.usecase.GetNewsSourcesUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindNewsSourcesUseCase(
        useCase: DefaultGetNewsSourcesUseCase
    ): GetNewsSourcesUseCase
}
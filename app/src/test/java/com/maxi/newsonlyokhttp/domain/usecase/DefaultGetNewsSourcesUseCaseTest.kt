package com.maxi.newsonlyokhttp.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.maxi.newsonlyokhttp.common.ErrorType
import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.domain.model.NewsSource
import com.maxi.newsonlyokhttp.domain.repository.NewsSourcesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

import org.junit.Before
import org.junit.Test

class DefaultGetNewsSourcesUseCaseTest {

    private lateinit var repository: NewsSourcesRepository

    private lateinit var useCase: DefaultGetNewsSourcesUseCase

    @Before
    fun setUp() {
        repository = mockk()

        useCase = DefaultGetNewsSourcesUseCase(repository)
    }

    @Test
    fun `when repository emits Loading, use case emits Loading`() = runTest {
        //GIVEN
        every { repository.getNewsSources(any()) } returns flowOf(Resource.Loading)

        //WHEN
        useCase.getNewsSources(forceRefresh = false).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)
            awaitComplete()
        }
    }

    @Test
    fun `when repository emits Success, use case emits Success with same data`() = runTest {
        //GIVEN
        val fakeSources = listOf(
            NewsSource("1", "BBC", "BBC worldwide", "someurl.com"),
            NewsSource("2", "BBC Local", "BBC local", "someotherurl.com")
        )

        every { repository.getNewsSources(any()) } returns flowOf(Resource.Success(fakeSources))

        //WHEN
        useCase.getNewsSources(forceRefresh = false).test {
            //THEN
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Success::class.java)
            assertThat((result as Resource.Success).data).isEqualTo(fakeSources)
            awaitComplete()
        }
    }

    @Test
    fun `when repository emits Error, use case emits same Error`() = runTest {
        //GIVEN
        val fakeError = Resource.Error(
            ErrorType.UNAUTHORISED,
            "Authorization failed",
            401
        )

        every { repository.getNewsSources(any()) } returns flowOf(fakeError)

        //WHEN
        useCase.getNewsSources(forceRefresh = false).test {
            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat(result).isEqualTo(fakeError)
            awaitComplete()
        }
    }

    @Test
    fun `correct forceRefresh parameter is passed to the repository`() = runTest {
        //GIVEN
        every { repository.getNewsSources(any()) } returns flowOf(Resource.Loading)

        //WHEN
        useCase.getNewsSources(forceRefresh = true).test {
            awaitItem()
            awaitComplete()
        }

        //THEN
        verify(exactly = 1) {
            repository.getNewsSources(true)
        }
    }
}
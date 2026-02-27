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
/**
 * mockk() creates a fake NewsSourcesRepository. It looks real to the use case but you control what it returns. Your use case doesn't know the difference.
 * runTest is a special coroutine test runner from kotlinx-coroutines-test. Since your flow runs in coroutines, you need this instead of plain @Test.
 * every { ... } returns ... is how you tell the mock what to return when a specific function is called. Think of it as "when this is called, respond with this."
 * .test { } from Turbine is a helper that lets you collect flow emissions one at a time with awaitItem(). Without Turbine, testing flows would be much more verbose.
 * assertThat(...).isEqualTo(...) is Google Truth's assertion style. It reads like plain English and gives much better error messages than plain JUnit assertions.
 * verify { ... } checks that a mock function was actually called — and with what arguments. This is how we confirm forceRefresh is really being passed through.
 *
 * awaitItem() vs awaitComplete()
 * Think of a Flow like a conveyor belt at a factory. Items come out one by one, and at some point the belt stops — meaning there's nothing more to send.
 * awaitItem() reaches out and grabs the next item coming off that conveyor belt. It waits (suspends) until something arrives, then hands it to you so you can inspect it.
 * If you call awaitItem() but nothing ever arrives, Turbine will fail the test with a timeout — which is actually helpful because it means your flow isn't emitting when you expected it to.
 * awaitComplete() waits for the conveyor belt to stop completely.
 * A flow completes when it has nothing more to emit. In our tests, flowOf(Resource.Loading) emits exactly one item and then finishes,
 * so after we grab that one item with awaitItem(), we call awaitComplete() to confirm the flow actually ended cleanly and didn't hang open unexpectedly.
 * The reason you need both together is important: if you only called awaitItem() and returned,
 * Turbine wouldn't know if the flow was done or if there were more uncollected emissions. awaitComplete() is your way of saying
 * "I've collected everything I expected, and I'm also confirming the flow closed properly." If there were a second unexpected emission,
 * awaitComplete() would catch that and fail the test — which protects you from flows that emit more than they should.
 * A quick way to remember the distinction: awaitItem() is about what came through, and awaitComplete() is about the belt stopping. You always want to verify both.
 */
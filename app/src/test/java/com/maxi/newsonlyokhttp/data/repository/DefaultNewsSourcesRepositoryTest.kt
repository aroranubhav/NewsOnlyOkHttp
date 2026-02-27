package com.maxi.newsonlyokhttp.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.maxi.newsonlyokhttp.common.DispatcherProvider
import com.maxi.newsonlyokhttp.common.ErrorType
import com.maxi.newsonlyokhttp.common.HttpException
import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.common.TransportException
import com.maxi.newsonlyokhttp.data.sources.remote.RemoteDataSource
import com.maxi.newsonlyokhttp.data.sources.remote.dto.NewsSourceDto
import com.maxi.newsonlyokhttp.data.sources.remote.dto.SourcesResponseDto
import com.maxi.newsonlyokhttp.domain.model.NewsSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import retrofit2.Response
import java.io.IOException
import kotlin.test.Test

class DefaultNewsSourcesRepositoryTest {

    private lateinit var remote: RemoteDataSource
    private lateinit var repository: DefaultNewsSourcesRepository

    /*
     A test dispatcher provider that uses UnconfinedTestDispatcher
     so flows run synchronously and predictably in tests
     */
    private val testDispatcher = object : DispatcherProvider {
        override val main: CoroutineDispatcher
            get() = Dispatchers.Unconfined
        override val io: CoroutineDispatcher
            get() = Dispatchers.Unconfined
        override val default: CoroutineDispatcher
            get() = Dispatchers.Unconfined
    }

    // Reusable fake DTO data
    private val fakeSourceDto = listOf(
        NewsSourceDto(id = "bbc-news", name = "BBC News", description = "BBC desc", url = "https://bbc.com"),
        NewsSourceDto(id = "cnn", name = "CNN", description = "CNN desc", url = "https://cnn.com")
    )

    // What the above DTOs should look like after mapping to domain models
    private val fakeDomainSources = listOf(
        NewsSource(id = "bbc-news", name = "BBC News", description = "BBC desc", url = "https://bbc.com"),
        NewsSource(id = "cnn", name = "CNN", description = "CNN desc", url = "https://cnn.com")
    )

    private val fakeSuccessResponse = SourcesResponseDto(
        status = "ok",
        sources = fakeSourceDto
    )

    @Before
    fun setUp() {
        remote = mockk()
        repository = DefaultNewsSourcesRepository(
            remote,
            testDispatcher
        )
    }

    @Test
    fun `repository emits Loading as first emission always`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } returns Response.success(fakeSuccessResponse)

        //WHEN
        repository.getNewsSources(forceRefresh = false).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)
            awaitItem() //consume the success emission
            awaitComplete()
        }
    }

    @Test
    fun `when remote returns success, repository emits Loading then Success with mapped domain models`() =
        runTest {
            //GIVEN
            coEvery { remote.getNewsSources(any()) } returns Response.success(fakeSuccessResponse)

            //WHEN
            repository.getNewsSources(forceRefresh = true).test {
                //THEN
                assertThat(awaitItem()).isEqualTo(Resource.Loading)
                val result = awaitItem()
                assertThat(result).isInstanceOf(Resource.Success::class.java)
                assertThat((result as Resource.Success).data).isEqualTo(fakeDomainSources)
                awaitComplete()
            }
        }

    @Test
    fun `when remote returns empty body, repository emits Loading then Error`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } returns Response.success(null)

        //WHEN
        repository.getNewsSources(forceRefresh = true).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat((result as Resource.Error).type).isEqualTo(ErrorType.UNKNOWN)
            awaitComplete()
        }
    }

    @Test
    fun `when remote returns empty body, repository emits Loading then Error with NO_CONNECTIVITY`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } throws TransportException.NoConnectivity()

        //WHEN
        repository.getNewsSources(forceRefresh = false).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat((result as Resource.Error).type).isEqualTo(ErrorType.NO_CONNECTIVITY)
            awaitComplete()
        }
    }

    @Test
    fun `when remote returns empty body, repository emits Loading then Error with TIMEOUT`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } throws TransportException.Timeout()

        //WHEN
        repository.getNewsSources(true).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat((result as Resource.Error).type).isEqualTo(ErrorType.TIMEOUT)
            awaitComplete()
        }
    }

    @Test
    fun `when remote returns empty body, repository emits Loading then Error with UNAUTHORIZED_ERROR type`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } throws HttpException.Unauthorized("", "","", "")

        //WHEN
        repository.getNewsSources(true).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat((result as Resource.Error).type).isEqualTo(ErrorType.UNAUTHORISED)
            awaitComplete()
        }
    }

    @Test
    fun `when remote returns empty body, repository emits Loading then Error with FORBIDDEN_ERROR type`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } throws HttpException.Forbidden("", "","", "")

        //WHEN
        repository.getNewsSources(true).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat((result as Resource.Error).type).isEqualTo(ErrorType.FORBIDDEN)
            awaitComplete()
        }
    }

    @Test
    fun `when remote returns empty body, repository emits Loading then Error with SERVER_ERROR type`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } throws HttpException.ServerError(500, "", "", "", "")

        //WHEN
        repository.getNewsSources(true).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat((result as Resource.Error).type).isEqualTo(ErrorType.SERVER_ERROR)
            awaitComplete()
        }
    }

    @Test
    fun `when remote returns empty body, repository emits Loading then Error with UNKNOWN type`() = runTest {
        //GIVEN
        coEvery { remote.getNewsSources(any()) } throws IOException()

        //WHEN
        repository.getNewsSources(true).test {
            //THEN
            assertThat(awaitItem()).isEqualTo(Resource.Loading)

            val result = awaitItem()
            assertThat(result).isInstanceOf(Resource.Error::class.java)
            assertThat((result as Resource.Error).type).isEqualTo(ErrorType.UNKNOWN)
            awaitComplete()
        }
    }
}
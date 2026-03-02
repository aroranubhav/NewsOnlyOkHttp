package com.maxi.newsonlyokhttp.ui.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.maxi.newsonlyokhttp.common.DispatcherProvider
import com.maxi.newsonlyokhttp.common.ErrorType
import com.maxi.newsonlyokhttp.common.MainDispatcherRule
import com.maxi.newsonlyokhttp.common.NetworkConnectivityHelper
import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.domain.model.NewsSource
import com.maxi.newsonlyokhttp.domain.usecase.GetNewsSourcesUseCase
import com.maxi.newsonlyokhttp.ui.common.NewsSourcesUiState
import com.maxi.newsonlyokhttp.ui.common.UiConstants
import com.maxi.newsonlyokhttp.ui.common.UiEvent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewSourcesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var useCase: GetNewsSourcesUseCase
    private lateinit var connectivityHelper: NetworkConnectivityHelper
    private lateinit var viewModel: NewSourcesViewModel

    private val testDispatcher = object : DispatcherProvider {
        override val main: CoroutineDispatcher
            get() = mainDispatcherRule.testDispatcher

        override val io: CoroutineDispatcher
            get() = mainDispatcherRule.testDispatcher

        override val default: CoroutineDispatcher
            get() = mainDispatcherRule.testDispatcher
    }

    private val fakeSources = listOf(
        NewsSource(
            id = "bbc-news",
            name = "BBC News",
            description = "BBC desc",
            url = "https://bbc.com"
        ),
        NewsSource(id = "cnn", name = "CNN", description = "CNN desc", url = "https://cnn.com")
    )

    @Before
    fun setUp() {
        useCase = mockk()
        connectivityHelper = mockk()
    }

    fun createViewModel() {
        viewModel = NewSourcesViewModel(useCase, testDispatcher, connectivityHelper)
    }

    @Test
    fun `on init, uiState is Loading before use case emits`() = runTest {
        //GIVEN
        every { useCase.getNewsSources(any()) } returns flowOf(Resource.Loading)

        createViewModel()

        //WHEN
        viewModel.uiState.test {
            //THEN
            assertThat(awaitItem()).isEqualTo(NewsSourcesUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `on init use case returns Success with empty list, uiState is Empty`() = runTest {
        //GIVEN
        every { useCase.getNewsSources(any()) } returns flowOf(Resource.Success(emptyList()))

        createViewModel()
        //WHEN
        viewModel.uiState.test {
            //THEN
            assertThat(awaitItem()).isInstanceOf(NewsSourcesUiState.Empty::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `on init use case returns Error, uiState is Empty`() = runTest {
        //GIVEN
        every { useCase.getNewsSources(any()) } returns flowOf(
            Resource.Error(ErrorType.NO_CONNECTIVITY, "No connection")
        )

        createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(NewsSourcesUiState.Empty)
    }

    @Test
    fun `when error occurs, UiEvent Error is emitted with correct message`() = runTest {
        //every { useCase.getNewsSources(false) } returns flowOf(Resource.Success(fakeSources))
        every { useCase.getNewsSources(any()) } returns flowOf(
            Resource.Error(
                ErrorType.NO_CONNECTIVITY,
                "No connection"
            )
        )
        every {
            connectivityHelper.hasNetworkConnectivity()
        } returns true

        createViewModel()

        viewModel.uiEvent.test {
            viewModel.refreshSources()
            awaitItem() //refresh event
            val eventError = awaitItem()
            assertThat(eventError).isInstanceOf(UiEvent.Error::class.java)
            assertThat((eventError as UiEvent.Error).error).isEqualTo(UiConstants.NETWORK_ISSUE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `on init, when use case returns NoChange, UiEvent NoChange is emitted`() = runTest {
        //GIVEN
        every { useCase.getNewsSources(any()) } returns flowOf(Resource.NoChange)
        every { connectivityHelper.hasNetworkConnectivity() } returns true

        createViewModel()

        //WHEN
        viewModel.uiEvent.test {
            viewModel.refreshSources()
            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each ErrorType maps to the correct UiConstants message`() = runTest {
        val errorMappings = mapOf(
            ErrorType.NO_CONNECTIVITY to UiConstants.NETWORK_ISSUE,
            ErrorType.TIMEOUT to UiConstants.CONNECTION_TIME_OUT,
            ErrorType.UNAUTHORISED to UiConstants.AUTHORIZATION_FAILED,
            ErrorType.FORBIDDEN to UiConstants.FORBIDDEN_REQUEST,
            ErrorType.NOT_FOUND to UiConstants.NOT_FOUND,
            ErrorType.SERVER_ERROR to UiConstants.SERVER_ERROR,
            ErrorType.UNKNOWN to UiConstants.UNKNOWN_ERROR
        )

        every {
            connectivityHelper.hasNetworkConnectivity()
        } returns true

        errorMappings.forEach { (type, expectedMessage) ->
            every {
                useCase.getNewsSources(any())
            } returns flowOf(Resource.Error(
                type, expectedMessage
            ))

            createViewModel()

            viewModel.uiEvent.test {
                viewModel.refreshSources()
                awaitItem() //refresh started event
                val errorEvent = awaitItem()
                assertThat(errorEvent).isInstanceOf(UiEvent::class.java)
                assertThat((errorEvent as UiEvent.Error).error).isEqualTo(expectedMessage)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `refreshSources, when no connectivity emits UiEvent Error with network issue`() = runTest {
        every {
            useCase.getNewsSources(any())
        } returns flowOf(Resource.Success(fakeSources))

        every {
            connectivityHelper.hasNetworkConnectivity()
        } returns false

        createViewModel()

        viewModel.uiEvent.test {
            viewModel.refreshSources()
            val errorEvent = awaitItem()
            assertThat((errorEvent as UiEvent.Error).error).isEqualTo(UiConstants.NETWORK_ISSUE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showMessage emits UiEvent Message with correct message`() = runTest {
        every {
            useCase.getNewsSources(any())
        } returns flowOf(Resource.Success(fakeSources))

        createViewModel()

        viewModel.uiEvent.test {
            viewModel.showMessage("Hola World!")
            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.Message::class.java)
            assertThat((event as UiEvent.Message).message).isEqualTo("Hola World!")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
package com.maxi.newsonlyokhttp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.maxi.newsonlyokhttp.domain.model.NewsSource
import com.maxi.newsonlyokhttp.ui.common.NewsSourcesUiState
import com.maxi.newsonlyokhttp.ui.common.UiEvent
import com.maxi.newsonlyokhttp.ui.viewmodel.NewSourcesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsSourcesScreen() {

    val viewModel: NewSourcesViewModel = hiltViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel
            .uiEvent
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .collect { event ->
                val message = when (event) {
                    is UiEvent.Error -> {
                        event.error
                    }

                    is UiEvent.NoChange -> {
                        event.message
                    }

                    is UiEvent.Message -> {
                        event.message
                    }

                    is UiEvent.RefreshStarted -> {
                        isRefreshing = true
                        null
                    }

                    is UiEvent.RefreshCompleted -> {
                        isRefreshing = false
                        event.message
                    }
                }
                message?.let {
                    snackBarHostState.showSnackbar(
                        message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "News Sources",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is NewsSourcesUiState.Success -> {
                    NewsSourcesList(
                        state.data,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            viewModel.refreshSources()
                        },
                        onClick = { source ->
                            viewModel.showMessage(source)
                        }
                    )
                }

                is NewsSourcesUiState.Loading -> {
                    Loader()
                }

                is NewsSourcesUiState.Empty -> {
                    NoSourcesView()
                }
            }
        }
    }
}

@Composable
fun NewsSourcesList(
    sources: List<NewsSource>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onClick: (source: String) -> Unit
) {

    val refreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                isRefreshing = isRefreshing,
                state = refreshState,
                onRefresh = onRefresh
            )
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(8.dp)
        ) {
            items(sources) { item ->
                NewsSourceItem(item, onClick)
            }
        }
        PullToRefreshDefaults.Indicator(
            state = refreshState,
            isRefreshing = isRefreshing,
            modifier = Modifier
                .align(Alignment.TopCenter),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun NewsSourceItem(
    source: NewsSource,
    onClick: (source: String) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
        onClick = {
            onClick(source.name)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = source.name,
                style = MaterialTheme.typography.headlineMedium,
            )


            Text(
                text = source.url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = source.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun Loader() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun NoSourcesView() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No news sources found!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal
        )
    }
}
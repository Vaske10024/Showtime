package rs.edu.raf.rma.showtime.ui.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.showtime.domain.MovieSummary
import rs.edu.raf.rma.showtime.domain.SortBy
import rs.edu.raf.rma.showtime.domain.SortOrder
import rs.edu.raf.rma.showtime.ui.components.CenterMessage
import rs.edu.raf.rma.showtime.ui.components.MovieRow
import rs.edu.raf.rma.showtime.util.oneDecimal

@Composable
fun MoviesScreen(
    onMovieClick: (String) -> Unit,
    viewModel: MoviesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MoviesEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    MoviesContent(
        state = state,
        movies = movies,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onMovieClick = onMovieClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesContent(
    state: MoviesViewState,
    movies: LazyPagingItems<MovieSummary>,
    snackbarHostState: SnackbarHostState,
    onIntent: (MoviesIntent) -> Unit,
    onMovieClick: (String) -> Unit,
) {
    val refreshLoadState = movies.loadState.refresh
    val appendLoadState = movies.loadState.append
    val isPagingRefreshLoading = refreshLoadState is LoadState.Loading
    val isPagingAppendLoading = appendLoadState is LoadState.Loading

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    IconButton(onClick = { onIntent(MoviesIntent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FilterPanel(state = state, onIntent = onIntent)
            if (state.isOffline) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    text = "Offline: showing locally cached catalog data.",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            when {
                (state.isInitialLoading || isPagingRefreshLoading) && movies.itemCount == 0 -> {
                    CenterMessage("Loading movies...", Modifier.fillMaxSize(), isLoading = true)
                }
                movies.itemCount == 0 && refreshLoadState is LoadState.NotLoading -> {
                    CenterMessage(state.errorMessage ?: "No movies match the selected filters.", Modifier.fillMaxSize())
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                ) {
                    items(
                        count = movies.itemCount,
                        key = movies.itemKey { it.imdbId },
                    ) { index ->
                        movies[index]?.let { movie ->
                            MovieRow(movie = movie, onClick = { onMovieClick(movie.imdbId) })
                        }
                    }
                    if (isPagingAppendLoading) {
                        item {
                            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPanel(state: MoviesViewState, onIntent: (MoviesIntent) -> Unit) {
    var genreExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    var orderExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.filters.query,
            onValueChange = { onIntent(MoviesIntent.SearchChanged(it)) },
            singleLine = true,
            label = { Text("Search") },
            trailingIcon = {
                if (state.filters.query.isNotBlank()) {
                    IconButton(onClick = { onIntent(MoviesIntent.SearchChanged("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.filters.minYear?.toString() ?: "",
                onValueChange = { onIntent(MoviesIntent.MinYearChanged(it)) },
                label = { Text("Min year") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.filters.maxYear?.toString() ?: "",
                onValueChange = { onIntent(MoviesIntent.MaxYearChanged(it)) },
                label = { Text("Max year") },
                singleLine = true,
            )
        }
        Text("Minimum IMDb rating: ${oneDecimal(state.filters.minRating ?: 0.0)}")
        Slider(
            value = (state.filters.minRating ?: 0.0).toFloat(),
            onValueChange = { onIntent(MoviesIntent.MinRatingChanged(it.toDouble())) },
            valueRange = 0f..10f,
            steps = 19,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { genreExpanded = true }) {
                    Text(
                        state.genres.firstOrNull { it.id == state.filters.genreId }?.name ?: "All genres",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                DropdownMenu(expanded = genreExpanded, onDismissRequest = { genreExpanded = false }) {
                    DropdownMenuItem(text = { Text("All genres") }, onClick = { genreExpanded = false; onIntent(MoviesIntent.GenreChanged(null)) })
                    state.genres.forEach { genre ->
                        DropdownMenuItem(text = { Text(genre.name) }, onClick = { genreExpanded = false; onIntent(MoviesIntent.GenreChanged(genre.id)) })
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { sortExpanded = true }) {
                    Text(state.filters.sortBy.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                    SortBy.values().filter { it != SortBy.Popularity }.forEach { sort ->
                        DropdownMenuItem(text = { Text(sort.label) }, onClick = { sortExpanded = false; onIntent(MoviesIntent.SortByChanged(sort)) })
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { orderExpanded = true }) {
                    Text(state.filters.sortOrder.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(expanded = orderExpanded, onDismissRequest = { orderExpanded = false }) {
                    SortOrder.values().forEach { order ->
                        DropdownMenuItem(text = { Text(order.label) }, onClick = { orderExpanded = false; onIntent(MoviesIntent.SortOrderChanged(order)) })
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onIntent(MoviesIntent.Refresh) }, enabled = !state.isRefreshing) {
                if (state.isRefreshing) CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp) else Text("Sync")
            }
            OutlinedButton(onClick = { onIntent(MoviesIntent.ClearFilters) }) {
                Text("Clear filters")
            }
        }
    }
}

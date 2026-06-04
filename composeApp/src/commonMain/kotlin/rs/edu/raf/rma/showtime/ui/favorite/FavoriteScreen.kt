package rs.edu.raf.rma.showtime.ui.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.showtime.ui.components.CenterMessage
import rs.edu.raf.rma.showtime.ui.components.MovieRow

@Composable
fun FavoriteScreen(
    onMovieClick: (String) -> Unit,
    viewModel: FavoriteViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FavoriteEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    FavoriteContent(state, snackbarHostState, viewModel::onIntent, onMovieClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteContent(
    state: FavoriteViewState,
    snackbarHostState: SnackbarHostState,
    onIntent: (FavoriteIntent) -> Unit,
    onMovieClick: (String) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Favorite") },
                actions = {
                    IconButton(onClick = { onIntent(FavoriteIntent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Favorite")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading && state.movies.isEmpty() -> CenterMessage("Syncing Favorite movies...", Modifier.fillMaxSize().padding(padding), true)
            state.movies.isEmpty() -> CenterMessage(if (state.isOffline) "Favorite is available offline, but currently empty." else "No Favorite movies yet.", Modifier.fillMaxSize().padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            ) {
                if (state.isOffline) item { Text("Offline: showing cached Favorite movies.") }
                items(state.movies, key = { it.imdbId }) { movie ->
                    MovieRow(
                        movie = movie,
                        onClick = { onMovieClick(movie.imdbId) },
                        onRemove = if (state.isOffline) {
                            null
                        } else {
                            { onIntent(FavoriteIntent.Remove(movie.imdbId)) }
                        },
                    )
                }
            }
        }
    }
}

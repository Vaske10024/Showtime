package rs.edu.raf.rma.showtime.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.showtime.ui.components.Backdrop
import rs.edu.raf.rma.showtime.ui.components.CenterMessage
import rs.edu.raf.rma.showtime.ui.components.GenreChips
import rs.edu.raf.rma.showtime.ui.components.Poster
import rs.edu.raf.rma.showtime.util.ratingText

@Composable
fun MovieDetailScreen(
    onBack: () -> Unit,
    viewModel: MovieDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MovieDetailEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    MovieDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailContent(
    state: MovieDetailViewState,
    snackbarHostState: SnackbarHostState,
    onIntent: (MovieDetailIntent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(state.movie?.title ?: "Movie Detail")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onIntent(MovieDetailIntent.Refresh)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.movie == null -> {
                CenterMessage(
                    message = "Loading movie...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    isLoading = true,
                )
            }

            state.movie == null -> {
                CenterMessage(
                    message = state.errorMessage ?: "Movie is not available offline yet.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            else -> {
                val movie = state.movie

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (state.isOffline) {
                        Text(
                            text = "Offline: showing cached details.",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Backdrop(
                        path = movie.backdropPath,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Poster(
                            path = movie.posterPath,
                            modifier = Modifier
                                .width(125.dp)
                                .aspectRatio(2f / 3f),
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )

                            Text(
                                text = listOfNotNull(
                                    movie.year?.toString(),
                                    movie.runtime?.let { "$it min" },
                                ).joinToString(" | "),
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                )

                                Text(
                                    text = " IMDb ${movie.imdbRating.ratingText()}  TMDB ${movie.tmdbRating.ratingText()}",
                                )
                            }
                        }
                    }

                    GenreChips(movie.genres)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onIntent(MovieDetailIntent.ToggleFavorite)
                            },
                            enabled = !state.isFavoriteUpdating && !state.isOffline,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = if (movie.isFavorite) {
                                    "Favorite"
                                } else {
                                    "Add Favorite"
                                },
                            )
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onIntent(MovieDetailIntent.ToggleWatchlist)
                            },
                            enabled = !state.isWatchlistUpdating && !state.isOffline,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = if (movie.isWatchlist) {
                                    "Watchlist"
                                } else {
                                    "Add Watchlist"
                                },
                            )
                        }
                    }

                    if (state.isOffline) {
                        Text(
                            text = "Favorites and Watchlist are read-only while offline.",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    if (state.isFavoriteUpdating || state.isWatchlistUpdating) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Text(
                        text = movie.overview ?: "No overview available.",
                    )

                    Text(
                        text = "Cast",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    if (movie.cast.isEmpty()) {
                        Text("No cast data cached yet. Tap refresh when online.")
                    } else {
                        movie.cast.take(12).forEach { cast ->
                            Text(
                                text = "• ${cast.name}${cast.department?.let { " ($it)" } ?: ""}",
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
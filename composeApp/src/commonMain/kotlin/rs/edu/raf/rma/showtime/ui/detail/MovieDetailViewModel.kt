package rs.edu.raf.rma.showtime.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.data.api.ShowtimeException
import rs.edu.raf.rma.showtime.data.repository.FavoriteRepository
import rs.edu.raf.rma.showtime.data.repository.MovieRepository
import rs.edu.raf.rma.showtime.data.repository.WatchlistRepository
import rs.edu.raf.rma.showtime.ui.navigation.MOVIE_ID

class MovieDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
    private val favoriteRepository: FavoriteRepository,
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {
    private val movieId: String = savedStateHandle[MOVIE_ID]
        ?: error("$MOVIE_ID argument is required")

    private val _state = MutableStateFlow(MovieDetailViewState())
    val state: StateFlow<MovieDetailViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MovieDetailEffect>()
    val effects: SharedFlow<MovieDetailEffect> = _effects.asSharedFlow()

    init {
        observeMovie()
        refresh()
    }

    fun onIntent(intent: MovieDetailIntent) {
        when (intent) {
            MovieDetailIntent.Refresh -> refresh()
            MovieDetailIntent.ToggleFavorite -> toggleFavorite()
            MovieDetailIntent.ToggleWatchlist -> toggleWatchlist()
            MovieDetailIntent.ClearError -> _state.getAndUpdate { MovieDetailReducer.reduce(it, intent) }
        }
    }

    private fun observeMovie() {
        viewModelScope.launch {
            movieRepository.observeMovieDetail(movieId).collect { movie ->
                _state.getAndUpdate { it.copy(movie = movie, isLoading = it.isLoading && movie == null) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.getAndUpdate { MovieDetailReducer.reduce(it, MovieDetailIntent.Refresh) }
            runCatching { movieRepository.syncMovieDetails(movieId) }
                .onSuccess { _state.getAndUpdate { it.copy(isLoading = false, isOffline = false, errorMessage = null) } }
                .onFailure { throwable ->
                    val message = throwable.messageForUi()
                    _state.getAndUpdate { it.copy(isLoading = false, isOffline = it.movie != null, errorMessage = message) }
                    _effects.emit(MovieDetailEffect.ShowMessage(message))
                }
        }
    }

    private fun toggleFavorite() {
        val movie = _state.value.movie ?: return
        viewModelScope.launch {
            _state.getAndUpdate { MovieDetailReducer.reduce(it, MovieDetailIntent.ToggleFavorite) }
            runCatching { favoriteRepository.setFavorite(movie.imdbId, !movie.isFavorite) }
                .onFailure { throwable ->
                    val message = throwable.messageForUi()
                    _state.getAndUpdate { it.copy(isFavoriteUpdating = false, errorMessage = message) }
                    _effects.emit(MovieDetailEffect.ShowMessage(message))
                }
                .onSuccess { _state.getAndUpdate { it.copy(isFavoriteUpdating = false) } }
        }
    }

    private fun toggleWatchlist() {
        val movie = _state.value.movie ?: return
        viewModelScope.launch {
            _state.getAndUpdate { MovieDetailReducer.reduce(it, MovieDetailIntent.ToggleWatchlist) }
            runCatching { watchlistRepository.setWatchlist(movie.imdbId, !movie.isWatchlist) }
                .onFailure { throwable ->
                    val message = throwable.messageForUi()
                    _state.getAndUpdate { it.copy(isWatchlistUpdating = false, errorMessage = message) }
                    _effects.emit(MovieDetailEffect.ShowMessage(message))
                }
                .onSuccess { _state.getAndUpdate { it.copy(isWatchlistUpdating = false) } }
        }
    }

    private fun Throwable.messageForUi(): String =
        if (this is ShowtimeException) message else "Network error. Showing cached movie details."
}

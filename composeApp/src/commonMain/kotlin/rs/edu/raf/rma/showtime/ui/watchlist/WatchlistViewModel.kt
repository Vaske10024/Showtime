package rs.edu.raf.rma.showtime.ui.watchlist

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
import rs.edu.raf.rma.showtime.data.repository.WatchlistRepository

class WatchlistViewModel(
    private val watchlistRepository: WatchlistRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(WatchlistViewState(isLoading = true))
    val state: StateFlow<WatchlistViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<WatchlistEffect>()
    val effects: SharedFlow<WatchlistEffect> = _effects.asSharedFlow()

    init {
        observeMovies()
        refresh()
    }

    fun onIntent(intent: WatchlistIntent) {
        when (intent) {
            WatchlistIntent.Refresh -> refresh()
            is WatchlistIntent.Remove -> remove(intent.movieId)
            WatchlistIntent.ClearError -> _state.getAndUpdate { WatchlistReducer.reduce(it, intent) }
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            watchlistRepository.observeWatchlistMovies().collect { movies ->
                _state.getAndUpdate { it.copy(movies = movies) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.getAndUpdate { WatchlistReducer.reduce(it, WatchlistIntent.Refresh) }
            runCatching { watchlistRepository.syncWatchlist() }
                .onSuccess { _state.getAndUpdate { it.copy(isLoading = false, isOffline = false) } }
                .onFailure { throwable ->
                    val message = throwable.messageForUi()
                    _state.getAndUpdate { it.copy(isLoading = false, isOffline = it.movies.isNotEmpty(), errorMessage = message) }
                    _effects.emit(WatchlistEffect.ShowMessage(message))
                }
        }
    }

    private fun remove(movieId: String) {
        viewModelScope.launch {
            runCatching { watchlistRepository.setWatchlist(movieId, false) }
                .onFailure { throwable ->
                    val message = throwable.messageForUi()
                    _state.getAndUpdate { it.copy(errorMessage = message) }
                    _effects.emit(WatchlistEffect.ShowMessage(message))
                }
        }
    }

    private fun Throwable.messageForUi(): String =
        if (this is ShowtimeException) message else "Network error. Showing cached Watchlist movies."
}

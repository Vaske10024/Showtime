package rs.edu.raf.rma.showtime.ui.favorite

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

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FavoriteViewState(isLoading = true))
    val state: StateFlow<FavoriteViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<FavoriteEffect>()
    val effects: SharedFlow<FavoriteEffect> = _effects.asSharedFlow()

    init {
        observeMovies()
        refresh()
    }

    fun onIntent(intent: FavoriteIntent) {
        when (intent) {
            FavoriteIntent.Refresh -> refresh()
            is FavoriteIntent.Remove -> remove(intent.movieId)
            FavoriteIntent.ClearError -> _state.getAndUpdate { FavoriteReducer.reduce(it, intent) }
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            favoriteRepository.observeFavoriteMovies().collect { movies ->
                _state.getAndUpdate { it.copy(movies = movies) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.getAndUpdate { FavoriteReducer.reduce(it, FavoriteIntent.Refresh) }
            runCatching { favoriteRepository.syncFavorites() }
                .onSuccess { _state.getAndUpdate { it.copy(isLoading = false, isOffline = false) } }
                .onFailure { throwable ->
                    val message = throwable.messageForUi()
                    _state.getAndUpdate { it.copy(isLoading = false, isOffline = it.movies.isNotEmpty(), errorMessage = message) }
                    _effects.emit(FavoriteEffect.ShowMessage(message))
                }
        }
    }

    private fun remove(movieId: String) {
        viewModelScope.launch {
            runCatching { favoriteRepository.setFavorite(movieId, false) }
                .onFailure { throwable ->
                    val message = throwable.messageForUi()
                    _state.getAndUpdate { it.copy(errorMessage = message) }
                    _effects.emit(FavoriteEffect.ShowMessage(message))
                }
        }
    }

    private fun Throwable.messageForUi(): String =
        if (this is ShowtimeException) message else "Network error. Showing cached Favorite movies."
}

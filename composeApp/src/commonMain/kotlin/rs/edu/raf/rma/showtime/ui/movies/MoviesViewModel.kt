package rs.edu.raf.rma.showtime.ui.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.data.api.ShowtimeException
import rs.edu.raf.rma.showtime.data.repository.MovieRepository
import rs.edu.raf.rma.showtime.domain.MovieSummary

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModel(
    private val movieRepository: MovieRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MoviesViewState(isInitialLoading = true))
    val state: StateFlow<MoviesViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MoviesEffect>()
    val effects: SharedFlow<MoviesEffect> = _effects.asSharedFlow()

    private val filters = MutableStateFlow(_state.value.filters)

    val movies: Flow<PagingData<MovieSummary>> = filters
        .flatMapLatest { movieRepository.pagedMovies(it) }
        .cachedIn(viewModelScope)

    init {
        observeGenres()
        refresh(resetPage = true, bootstrap = true)
    }

    fun onIntent(intent: MoviesIntent) {
        when (intent) {
            MoviesIntent.Refresh -> refresh(resetPage = true)
            is MoviesIntent.SearchChanged,
            is MoviesIntent.GenreChanged,
            is MoviesIntent.MinYearChanged,
            is MoviesIntent.MaxYearChanged,
            is MoviesIntent.MinRatingChanged,
            is MoviesIntent.SortByChanged,
            is MoviesIntent.SortOrderChanged,
            MoviesIntent.ClearFilters -> {
                _state.getAndUpdate { MoviesReducer.reduce(it, intent) }
                filters.value = _state.value.filters
            }
            MoviesIntent.ClearError -> _state.getAndUpdate { MoviesReducer.reduce(it, intent) }
        }
    }

    private fun observeGenres() {
        viewModelScope.launch {
            movieRepository.observeGenres().collect { genres ->
                _state.getAndUpdate { it.copy(genres = genres) }
            }
        }
    }

    private fun refresh(resetPage: Boolean, bootstrap: Boolean = false) {
        viewModelScope.launch {
            if (resetPage) {
                _state.getAndUpdate { it.copy(isRefreshing = true, isInitialLoading = true, errorMessage = null, isOffline = false) }
            }
            runCatching {
                movieRepository.syncCatalog()
                if (bootstrap) runCatching { movieRepository.bootstrapTopMovies() }
            }.onSuccess {
                _state.getAndUpdate {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                        isOffline = false,
                    )
                }
            }.onFailure { throwable ->
                val message = throwable.messageForUi()
                _state.getAndUpdate {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        isOffline = true,
                        errorMessage = message,
                    )
                }
                _effects.emit(MoviesEffect.ShowMessage(message))
            }
        }
    }

    private fun Throwable.messageForUi(): String =
        if (this is ShowtimeException) message else "Network error. Showing locally cached movies."
}

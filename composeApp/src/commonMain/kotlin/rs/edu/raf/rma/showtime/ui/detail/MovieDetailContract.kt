package rs.edu.raf.rma.showtime.ui.detail

import rs.edu.raf.rma.showtime.domain.MovieDetail

data class MovieDetailViewState(
    val movie: MovieDetail? = null,
    val isLoading: Boolean = true,
    val isFavoriteUpdating: Boolean = false,
    val isWatchlistUpdating: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface MovieDetailIntent {
    data object Refresh : MovieDetailIntent
    data object ToggleFavorite : MovieDetailIntent
    data object ToggleWatchlist : MovieDetailIntent
    data object ClearError : MovieDetailIntent
}

sealed interface MovieDetailEffect {
    data class ShowMessage(val message: String) : MovieDetailEffect
}

object MovieDetailReducer {
    fun reduce(state: MovieDetailViewState, intent: MovieDetailIntent): MovieDetailViewState = when (intent) {
        MovieDetailIntent.Refresh -> state.copy(isLoading = true, errorMessage = null, isOffline = false)
        MovieDetailIntent.ToggleFavorite -> state.copy(isFavoriteUpdating = true, errorMessage = null)
        MovieDetailIntent.ToggleWatchlist -> state.copy(isWatchlistUpdating = true, errorMessage = null)
        MovieDetailIntent.ClearError -> state.copy(errorMessage = null)
    }
}

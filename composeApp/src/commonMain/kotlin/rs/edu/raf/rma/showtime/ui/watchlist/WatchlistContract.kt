package rs.edu.raf.rma.showtime.ui.watchlist

import rs.edu.raf.rma.showtime.domain.MovieSummary

data class WatchlistViewState(
    val movies: List<MovieSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface WatchlistIntent {
    data object Refresh : WatchlistIntent
    data class Remove(val movieId: String) : WatchlistIntent
    data object ClearError : WatchlistIntent
}

sealed interface WatchlistEffect {
    data class ShowMessage(val message: String) : WatchlistEffect
}

object WatchlistReducer {
    fun reduce(state: WatchlistViewState, intent: WatchlistIntent): WatchlistViewState = when (intent) {
        WatchlistIntent.Refresh -> state.copy(isLoading = true, errorMessage = null, isOffline = false)
        is WatchlistIntent.Remove -> state.copy(errorMessage = null)
        WatchlistIntent.ClearError -> state.copy(errorMessage = null)
    }
}

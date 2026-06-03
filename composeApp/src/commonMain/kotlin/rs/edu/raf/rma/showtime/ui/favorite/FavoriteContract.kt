package rs.edu.raf.rma.showtime.ui.favorite

import rs.edu.raf.rma.showtime.domain.MovieSummary

data class FavoriteViewState(
    val movies: List<MovieSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface FavoriteIntent {
    data object Refresh : FavoriteIntent
    data class Remove(val movieId: String) : FavoriteIntent
    data object ClearError : FavoriteIntent
}

sealed interface FavoriteEffect {
    data class ShowMessage(val message: String) : FavoriteEffect
}

object FavoriteReducer {
    fun reduce(state: FavoriteViewState, intent: FavoriteIntent): FavoriteViewState = when (intent) {
        FavoriteIntent.Refresh -> state.copy(isLoading = true, errorMessage = null, isOffline = false)
        is FavoriteIntent.Remove -> state.copy(errorMessage = null)
        FavoriteIntent.ClearError -> state.copy(errorMessage = null)
    }
}

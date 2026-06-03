package rs.edu.raf.rma.showtime.ui.movies

import rs.edu.raf.rma.showtime.domain.Genre
import rs.edu.raf.rma.showtime.domain.MovieFilters
import rs.edu.raf.rma.showtime.domain.SortBy
import rs.edu.raf.rma.showtime.domain.SortOrder

data class MoviesViewState(
    val genres: List<Genre> = emptyList(),
    val filters: MovieFilters = MovieFilters(),
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface MoviesIntent {
    data object Refresh : MoviesIntent
    data class SearchChanged(val value: String) : MoviesIntent
    data class GenreChanged(val genreId: Int?) : MoviesIntent
    data class MinYearChanged(val value: String) : MoviesIntent
    data class MaxYearChanged(val value: String) : MoviesIntent
    data class MinRatingChanged(val value: Double) : MoviesIntent
    data class SortByChanged(val sortBy: SortBy) : MoviesIntent
    data class SortOrderChanged(val sortOrder: SortOrder) : MoviesIntent
    data object ClearFilters : MoviesIntent
    data object ClearError : MoviesIntent
}

sealed interface MoviesEffect {
    data class ShowMessage(val message: String) : MoviesEffect
}

object MoviesReducer {
    fun reduce(state: MoviesViewState, intent: MoviesIntent): MoviesViewState = when (intent) {
        MoviesIntent.Refresh -> state.copy(isRefreshing = true, errorMessage = null, isOffline = false)
        is MoviesIntent.SearchChanged -> state.copy(filters = state.filters.copy(query = intent.value), errorMessage = null)
        is MoviesIntent.GenreChanged -> state.copy(filters = state.filters.copy(genreId = intent.genreId), errorMessage = null)
        is MoviesIntent.MinYearChanged -> state.copy(filters = state.filters.copy(minYear = intent.value.toIntOrNull()), errorMessage = null)
        is MoviesIntent.MaxYearChanged -> state.copy(filters = state.filters.copy(maxYear = intent.value.toIntOrNull()), errorMessage = null)
        is MoviesIntent.MinRatingChanged -> state.copy(filters = state.filters.copy(minRating = intent.value.takeIf { it > 0.0 }), errorMessage = null)
        is MoviesIntent.SortByChanged -> state.copy(filters = state.filters.copy(sortBy = intent.sortBy), errorMessage = null)
        is MoviesIntent.SortOrderChanged -> state.copy(filters = state.filters.copy(sortOrder = intent.sortOrder), errorMessage = null)
        MoviesIntent.ClearFilters -> state.copy(filters = MovieFilters(), errorMessage = null)
        MoviesIntent.ClearError -> state.copy(errorMessage = null)
    }
}

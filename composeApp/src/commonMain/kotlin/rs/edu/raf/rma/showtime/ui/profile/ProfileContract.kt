package rs.edu.raf.rma.showtime.ui.profile

import rs.edu.raf.rma.showtime.domain.UserProfile

data class ProfileViewState(
    val user: UserProfile? = null,
    val favoriteCount: Int = 0,
    val watchlistCount: Int = 0,
    val bestScore: Double = 0.0,
    val playedQuizzes: Int = 0,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface ProfileIntent {
    data object Refresh : ProfileIntent
    data object Logout : ProfileIntent
    data object ClearError : ProfileIntent
}

sealed interface ProfileEffect {
    data class ShowMessage(val message: String) : ProfileEffect
}

object ProfileReducer {
    fun reduce(state: ProfileViewState, intent: ProfileIntent): ProfileViewState = when (intent) {
        ProfileIntent.Refresh -> state.copy(isLoading = true, errorMessage = null, isOffline = false)
        ProfileIntent.Logout -> state.copy(isLoading = true, errorMessage = null)
        ProfileIntent.ClearError -> state.copy(errorMessage = null)
    }
}

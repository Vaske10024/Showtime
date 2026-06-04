package rs.edu.raf.rma.showtime.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.data.api.ShowtimeException
import rs.edu.raf.rma.showtime.data.repository.AuthRepository
import rs.edu.raf.rma.showtime.data.repository.FavoriteRepository
import rs.edu.raf.rma.showtime.data.repository.QuizRepository
import rs.edu.raf.rma.showtime.data.repository.WatchlistRepository

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val favoriteRepository: FavoriteRepository,
    private val watchlistRepository: WatchlistRepository,
    private val quizRepository: QuizRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileViewState(isLoading = true))
    val state: StateFlow<ProfileViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>()
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    init {
        observeProfile()
        refresh()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.Refresh -> refresh()
            ProfileIntent.Logout -> logout()
            ProfileIntent.ClearError -> _state.getAndUpdate { ProfileReducer.reduce(it, intent) }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            combine(
                authRepository.observeUserProfile(),
                favoriteRepository.observeFavoriteCount(),
                watchlistRepository.observeWatchlistCount(),
                quizRepository.observeBestScore(),
                quizRepository.observePlayedCount(),
            ) { user, favoriteCount, watchlistCount, bestScore, playedCount ->
                ProfileViewState(
                    user = user,
                    favoriteCount = favoriteCount,
                    watchlistCount = watchlistCount,
                    bestScore = bestScore ?: 0.0,
                    playedQuizzes = playedCount,
                    isLoading = _state.value.isLoading,
                    isOffline = _state.value.isOffline,
                    errorMessage = _state.value.errorMessage,
                )
            }.collect { newState -> _state.value = newState }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.getAndUpdate { ProfileReducer.reduce(it, ProfileIntent.Refresh) }
            runCatching {
                authRepository.refreshProfile()

                val favoriteSync = runCatching { favoriteRepository.syncFavorites() }
                val watchlistSync = runCatching { watchlistRepository.syncWatchlist() }

                favoriteSync.exceptionOrNull()?.let { throw it }
                watchlistSync.exceptionOrNull()?.let { throw it }
            }.onSuccess {
                _state.getAndUpdate { it.copy(isLoading = false, isOffline = false) }
            }.onFailure { throwable ->
                val message = throwable.messageForUi()
                _state.getAndUpdate { it.copy(isLoading = false, isOffline = it.user != null, errorMessage = message) }
                _effects.emit(ProfileEffect.ShowMessage(message))
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.getAndUpdate { ProfileReducer.reduce(it, ProfileIntent.Logout) }
            authRepository.logout()
        }
    }

    private fun Throwable.messageForUi(): String =
        if (this is ShowtimeException) message else "Network error. Showing cached profile data."
}

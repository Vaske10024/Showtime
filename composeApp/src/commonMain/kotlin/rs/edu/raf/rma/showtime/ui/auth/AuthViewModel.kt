package rs.edu.raf.rma.showtime.ui.auth

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
import rs.edu.raf.rma.showtime.data.repository.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthViewState())
    val state: StateFlow<AuthViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            AuthIntent.SubmitLogin -> submitLogin()
            AuthIntent.SubmitSignup -> submitSignup()
            else -> _state.getAndUpdate { AuthReducer.reduce(it, intent) }
        }
    }

    private fun submitLogin() {
        val current = _state.getAndUpdate { AuthReducer.reduce(it, AuthIntent.SubmitLogin) }
        viewModelScope.launch {
            runCatching { authRepository.login(current.username, current.password) }
                .onFailure { handleFailure(it) }
                .onSuccess { _state.value = AuthViewState() }
        }
    }

    private fun submitSignup() {
        val current = _state.getAndUpdate { AuthReducer.reduce(it, AuthIntent.SubmitSignup) }
        viewModelScope.launch {
            runCatching { authRepository.signup(current.fullName, current.username, current.password) }
                .onFailure { handleFailure(it) }
                .onSuccess { _state.value = AuthViewState() }
        }
    }

    private suspend fun handleFailure(throwable: Throwable) {
        val message = if (throwable is ShowtimeException) throwable.message else "Network error. Check your connection and try again."
        _state.getAndUpdate { AuthReducer.loadingDone(it, message) }
        _effects.emit(AuthEffect.ShowMessage(message))
    }
}

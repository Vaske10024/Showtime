package rs.edu.raf.rma.showtime.ui.auth

enum class AuthMode { Landing, Login, Signup }

data class AuthViewState(
    val mode: AuthMode = AuthMode.Landing,
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface AuthIntent {
    data object OpenLogin : AuthIntent
    data object OpenSignup : AuthIntent
    data object BackToLanding : AuthIntent
    data class FullNameChanged(val value: String) : AuthIntent
    data class UsernameChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data object SubmitLogin : AuthIntent
    data object SubmitSignup : AuthIntent
    data object ClearError : AuthIntent
}

sealed interface AuthEffect {
    data class ShowMessage(val message: String) : AuthEffect
}

object AuthReducer {
    fun reduce(state: AuthViewState, intent: AuthIntent): AuthViewState = when (intent) {
        AuthIntent.OpenLogin -> state.copy(mode = AuthMode.Login, errorMessage = null, password = "")
        AuthIntent.OpenSignup -> state.copy(mode = AuthMode.Signup, errorMessage = null, password = "")
        AuthIntent.BackToLanding -> AuthViewState()
        is AuthIntent.FullNameChanged -> state.copy(fullName = intent.value, errorMessage = null)
        is AuthIntent.UsernameChanged -> state.copy(username = intent.value, errorMessage = null)
        is AuthIntent.PasswordChanged -> state.copy(password = intent.value, errorMessage = null)
        AuthIntent.SubmitLogin, AuthIntent.SubmitSignup -> state.copy(isLoading = true, errorMessage = null)
        AuthIntent.ClearError -> state.copy(errorMessage = null)
    }

    fun loadingDone(state: AuthViewState, error: String? = null): AuthViewState =
        state.copy(isLoading = false, errorMessage = error)
}

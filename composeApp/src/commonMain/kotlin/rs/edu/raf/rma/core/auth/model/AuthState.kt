package rs.edu.raf.rma.core.auth.model


import kotlin.time.Clock

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val data: AuthData) : AuthState()
}

fun AuthData.asAuthenticationState(): AuthState {
    val now = Clock.System.now().toEpochMilliseconds() / 1000
    return when {
        accessToken.isBlank() -> AuthState.Unauthenticated
        expiresAtEpochSeconds > 0L && expiresAtEpochSeconds <= now -> AuthState.Unauthenticated
        else -> AuthState.Authenticated(data = this.copy())
    }
}

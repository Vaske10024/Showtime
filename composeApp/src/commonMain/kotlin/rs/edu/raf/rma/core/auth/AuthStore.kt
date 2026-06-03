package rs.edu.raf.rma.core.auth

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.core.auth.model.asAuthenticationState

class AuthStore(
    private val persistence: DataStore<AuthData>,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val authData: StateFlow<AuthData> = persistence.data
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first() },
        )

    val authState: StateFlow<AuthState> = persistence.data
        .map { it.asAuthenticationState() }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = authData.value.asAuthenticationState(),
        )

    suspend fun setAuthData(authData: AuthData) {
        persistence.updateData { authData }
    }

    suspend fun setAccessToken(accessToken: String) {
        persistence.updateData { it.copy(accessToken = accessToken) }
    }

    suspend fun clearAuthData() {
        persistence.updateData { AuthData.empty() }
    }

    suspend fun awaitInitialAuthState(): AuthState {
        return persistence.data
            .map { it.asAuthenticationState() }
            .first()
    }
}

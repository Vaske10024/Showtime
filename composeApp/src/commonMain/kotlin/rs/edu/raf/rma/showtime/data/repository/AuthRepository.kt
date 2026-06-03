package rs.edu.raf.rma.showtime.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.data.api.AuthenticationApi
import rs.edu.raf.rma.showtime.data.api.LoginRequestDto
import rs.edu.raf.rma.showtime.data.api.ShowtimeException
import rs.edu.raf.rma.showtime.data.api.SignupRequestDto
import rs.edu.raf.rma.showtime.data.api.UserApi
import rs.edu.raf.rma.showtime.data.api.toShowtimeException
import rs.edu.raf.rma.showtime.data.mappers.toAuthData
import rs.edu.raf.rma.showtime.data.mappers.toDomain
import rs.edu.raf.rma.showtime.data.mappers.toEntity
import rs.edu.raf.rma.showtime.domain.UserProfile

class AuthRepository(
    private val authenticationApi: AuthenticationApi,
    private val userApi: UserApi,
    private val authStore: AuthStore,
    private val sessionManager: AuthSessionManager,
    private val authenticatedCallRunner: AuthenticatedCallRunner,
    private val database: AppDatabase,
) {
    private val dao = database.showtimeDao()

    val authState: Flow<AuthState> = authStore.authState

    fun observeUserProfile(): Flow<UserProfile?> = dao.observeCurrentUser().map { it?.toDomain() }

    suspend fun signup(fullName: String, username: String, password: String) {
        validateSignup(fullName, username, password)
        try {
            val response = authenticationApi.signup(
                SignupRequestDto(
                    fullName = fullName.trim(),
                    username = username.trim(),
                    password = password,
                )
            )
            persistAuthResponse(response.toAuthData(), response.user.toEntity())
        } catch (throwable: Throwable) {
            val mapped = throwable.toShowtimeException()
            throw when (mapped.kind) {
                ShowtimeException.Kind.UsernameTaken -> mapped
                ShowtimeException.Kind.Validation -> mapped
                else -> mapped
            }
        }
    }

    suspend fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            throw ShowtimeException(ShowtimeException.Kind.Validation, "Username and password are required.")
        }
        try {
            val response = authenticationApi.login(LoginRequestDto(username = username.trim(), password = password))
            persistAuthResponse(response.toAuthData(), response.user.toEntity())
        } catch (throwable: Throwable) {
            val mapped = throwable.toShowtimeException()
            if (mapped.kind == ShowtimeException.Kind.Unauthorized) {
                throw ShowtimeException(ShowtimeException.Kind.InvalidCredentials, "Invalid username or password.", throwable)
            }
            throw mapped
        }
    }

    suspend fun refreshProfile() {
        authenticatedCallRunner.execute {
            val user = userApi.me()
            dao.upsertUser(user.toEntity())
        }
    }

    suspend fun logout() {
        sessionManager.logout()
    }

    private suspend fun persistAuthResponse(authData: rs.edu.raf.rma.core.auth.model.AuthData, user: rs.edu.raf.rma.showtime.data.db.UserEntity) {
        authStore.setAuthData(authData)
        dao.upsertUser(user)
    }

    private fun validateSignup(fullName: String, username: String, password: String) {
        val trimmedName = fullName.trim()
        val trimmedUsername = username.trim()
        if (trimmedName.isBlank() || trimmedUsername.isBlank() || password.isBlank()) {
            throw ShowtimeException(ShowtimeException.Kind.Validation, "All registration fields are required.")
        }
        if (!trimmedUsername.matches(Regex("^[A-Za-z0-9_]{3,}$"))) {
            throw ShowtimeException(
                ShowtimeException.Kind.Validation,
                "Username must have at least 3 letters, digits, or underscores.",
            )
        }
        if (password.length < 8) {
            throw ShowtimeException(ShowtimeException.Kind.Validation, "Password must be at least 8 characters.")
        }
    }
}

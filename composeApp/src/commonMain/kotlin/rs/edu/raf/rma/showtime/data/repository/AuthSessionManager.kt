package rs.edu.raf.rma.showtime.data.repository

import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.db.AppDatabase

class AuthSessionManager(
    private val authStore: AuthStore,
    private val database: AppDatabase,
) {
    suspend fun logout() {
        clearLocalSession()
    }

    suspend fun forceLogout() {
        clearLocalSession()
    }

    private suspend fun clearLocalSession() {
        val dao = database.showtimeDao()
        dao.clearFavorites()
        dao.clearWatchlist()
        dao.clearCurrentUser()
        authStore.clearAuthData()
    }
}

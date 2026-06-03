package rs.edu.raf.rma.showtime.data.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import rs.edu.raf.rma.showtime.data.api.ShowtimeException
import rs.edu.raf.rma.showtime.data.api.toShowtimeException

class AuthenticatedCallRunner(
    private val sessionManager: AuthSessionManager,
) {
    suspend fun <T> execute(block: suspend () -> T): T {
        return try {
            block()
        } catch (throwable: Throwable) {
            val mapped = throwable.toShowtimeException()
            if (throwable is ResponseException && throwable.response.status == HttpStatusCode.Unauthorized) {
                sessionManager.forceLogout()
                throw ShowtimeException(
                    kind = ShowtimeException.Kind.Unauthorized,
                    message = "Session expired. Please log in again.",
                    cause = throwable,
                )
            }
            throw mapped
        }
    }
}

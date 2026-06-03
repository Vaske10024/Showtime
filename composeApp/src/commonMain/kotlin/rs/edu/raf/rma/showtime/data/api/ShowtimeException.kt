package rs.edu.raf.rma.showtime.data.api

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

class ShowtimeException(
    val kind: Kind,
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    enum class Kind {
        Network,
        InvalidCredentials,
        UsernameTaken,
        Validation,
        Unauthorized,
        NotFound,
        Unknown,
    }
}

fun Throwable.toShowtimeException(): ShowtimeException {
    if (this is ShowtimeException) return this
    if (this is CancellationException) throw this
    if (this is ResponseException) {
        return when (response.status) {
            HttpStatusCode.Unauthorized -> ShowtimeException(
                kind = ShowtimeException.Kind.Unauthorized,
                message = "Invalid credentials or expired session.",
                cause = this,
            )
            HttpStatusCode.Conflict -> ShowtimeException(
                kind = ShowtimeException.Kind.UsernameTaken,
                message = "Username already taken.",
                cause = this,
            )
            HttpStatusCode.BadRequest -> ShowtimeException(
                kind = ShowtimeException.Kind.Validation,
                message = "Please check all fields and try again.",
                cause = this,
            )
            HttpStatusCode.NotFound -> ShowtimeException(
                kind = ShowtimeException.Kind.NotFound,
                message = "Requested resource was not found.",
                cause = this,
            )
            else -> ShowtimeException(
                kind = ShowtimeException.Kind.Unknown,
                message = "Server error: ${response.status.value}.",
                cause = this,
            )
        }
    }
    return ShowtimeException(
        kind = ShowtimeException.Kind.Network,
        message = "Network error. Check your connection and try again.",
        cause = this,
    )
}

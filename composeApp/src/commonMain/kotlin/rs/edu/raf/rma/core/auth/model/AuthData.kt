package rs.edu.raf.rma.core.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthData(
    val accessToken: String = "",
    val expiresAtEpochSeconds: Long = 0L,
    val userId: Int? = null,
    val username: String = "",
    val fullName: String = "",
) {
    companion object {
        fun empty(): AuthData = AuthData()
    }
}

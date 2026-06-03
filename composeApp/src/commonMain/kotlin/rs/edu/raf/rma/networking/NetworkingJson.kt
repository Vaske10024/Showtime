package rs.edu.raf.rma.networking

import kotlinx.serialization.json.Json

val NetworkingJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

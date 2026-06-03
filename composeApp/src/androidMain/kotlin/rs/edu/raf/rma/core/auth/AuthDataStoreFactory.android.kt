package rs.edu.raf.rma.core.auth

import rs.edu.raf.rma.AppContextHolder

private const val AUTH_DATA_FILE_NAME = "auth_data.json"

actual fun createAuthDataStorePath(): String {
    val context = AppContextHolder.appContext
    return context.filesDir.resolve("datastore/$AUTH_DATA_FILE_NAME").absolutePath
}
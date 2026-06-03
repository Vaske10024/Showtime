package rs.edu.raf.rma.core.auth

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private const val AUTH_DATA_FILE_NAME = "auth_data.json"

@OptIn(ExperimentalForeignApi::class)
actual fun createAuthDataStorePath(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory).path + "/$AUTH_DATA_FILE_NAME"
}

package rs.edu.raf.rma.core.auth

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions

private const val AUTH_DATA_FILE_NAME = "auth_data.json"

actual fun createAuthDataStorePath(): String {
    val appDir = DesktopFileUtils.ensureSecureDirectory(
        File(System.getProperty("user.home"), ".rma/datastore"),
    )
    return File(appDir, AUTH_DATA_FILE_NAME).absolutePath
}


private object DesktopFileUtils {

    private val ownerOnlyDir = PosixFilePermissions.fromString("rwx------")
    private val ownerOnlyFile = PosixFilePermissions.fromString("rw-------")

    fun ensureSecureDirectory(path: File): File {
        val nioPath = path.toPath()
        try {
            if (!Files.exists(nioPath)) {
                Files.createDirectories(nioPath, PosixFilePermissions.asFileAttribute(ownerOnlyDir))
            } else {
                Files.setPosixFilePermissions(nioPath, ownerOnlyDir)
            }
        } catch (_: UnsupportedOperationException) {
            // Windows — POSIX permissions not supported.
            // Home directory ACLs provide per-user isolation by default.
            path.mkdirs()
        }
        return path
    }

    fun writeSecureFile(file: File, content: ByteArray) {
        ensureSecureDirectory(file.parentFile)
        Files.write(file.toPath(), content)
        try {
            Files.setPosixFilePermissions(file.toPath(), ownerOnlyFile)
        } catch (_: UnsupportedOperationException) {
            // Windows — inherits parent ACLs
        }
    }

    fun writeSecureFile(file: File, content: String) {
        writeSecureFile(file, content.encodeToByteArray())
    }
}

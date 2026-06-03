package rs.edu.raf.rma.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import rs.edu.raf.rma.core.auth.model.AuthData

expect fun createAuthDataStorePath(): String

fun createAuthDataStore(): DataStore<AuthData> {
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = AuthDataSerializer,
            producePath = { createAuthDataStorePath().toPath() },
        ),
    )
}

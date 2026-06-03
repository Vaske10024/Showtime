package rs.edu.raf.rma.core.auth.di

import androidx.datastore.core.DataStore
import org.koin.dsl.module
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.createAuthDataStore

val authModule = module {

    single<DataStore<AuthData>> { createAuthDataStore() }

    single<AuthStore> { AuthStore(persistence = get()) }
}

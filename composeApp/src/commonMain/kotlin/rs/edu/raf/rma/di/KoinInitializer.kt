package rs.edu.raf.rma.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import rs.edu.raf.rma.core.auth.di.authModule
import rs.edu.raf.rma.core.db.di.databaseModule
import rs.edu.raf.rma.networking.di.networkingModule
import rs.edu.raf.rma.showtime.di.showtimeModule

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        config?.invoke(this)
        modules(
            databaseModule(),
            authModule,
            networkingModule,
            showtimeModule,
        )
    }
}

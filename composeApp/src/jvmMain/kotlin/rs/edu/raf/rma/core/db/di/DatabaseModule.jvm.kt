package rs.edu.raf.rma.core.db.di

import coil3.PlatformContext
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.core.db.buildAppDatabase
import rs.edu.raf.rma.core.db.getDatabaseBuilder
import org.koin.dsl.module

actual fun databaseModule() = module {
    single<PlatformContext> { PlatformContext.INSTANCE }
    single<AppDatabase> {
        buildAppDatabase(builder = getDatabaseBuilder())
    }
}

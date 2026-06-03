package rs.edu.raf.rma.core.db.di

import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.core.db.buildAppDatabase
import rs.edu.raf.rma.core.db.getDatabaseBuilder
import org.koin.dsl.module

actual fun databaseModule() = module {
    single<AppDatabase> {
        buildAppDatabase(builder = getDatabaseBuilder())
    }
}

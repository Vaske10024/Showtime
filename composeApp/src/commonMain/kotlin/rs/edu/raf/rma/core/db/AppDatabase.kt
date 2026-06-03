package rs.edu.raf.rma.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import rs.edu.raf.rma.showtime.data.db.FavoriteEntity
import rs.edu.raf.rma.showtime.data.db.GenreEntity
import rs.edu.raf.rma.showtime.data.db.MovieEntity
import rs.edu.raf.rma.showtime.data.db.QuizSessionEntity
import rs.edu.raf.rma.showtime.data.db.ShowtimeDao
import rs.edu.raf.rma.showtime.data.db.UserEntity
import rs.edu.raf.rma.showtime.data.db.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class,
        GenreEntity::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        UserEntity::class,
        QuizSessionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showtimeDao(): ShowtimeDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(dropAllTables = true)
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

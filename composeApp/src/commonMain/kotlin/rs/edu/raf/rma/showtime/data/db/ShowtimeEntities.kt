package rs.edu.raf.rma.showtime.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
    indices = [Index(value = ["title"]), Index(value = ["year"])]
)
data class MovieEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Double?,
    val imdbVotes: Int?,
    val tmdbRating: Double? = null,
    val tmdbVotes: Int? = null,
    val popularity: Double? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val runtime: Int? = null,
    val overview: String? = null,
    val releaseDate: String? = null,
    val genresJson: String = "[]",
    val castJson: String = "[]",
    val imagePathsJson: String = "[]",
    val listSyncedAt: Long = 0L,
    val detailsSyncedAt: Long = 0L,
)

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey val id: Int,
    val name: String,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val movieId: String,
    val createdAt: Long,
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val movieId: String,
    val createdAt: Long,
)

@Entity(tableName = "current_user")
data class UserEntity(
    @PrimaryKey val id: Int,
    val username: String,
    val fullName: String,
)

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: Int,
    val score: Double,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val usedTimeSeconds: Int,
    val playedAtEpochMillis: Long,
)

package rs.edu.raf.rma.showtime.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowtimeDao {

    @Upsert
    suspend fun upsertMovie(movie: MovieEntity)

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Query(
        """
        SELECT * FROM movies
        WHERE (:query = '' OR title LIKE '%' || :query || '%')
            AND (:genreNeedle IS NULL OR genresJson LIKE '%' || :genreNeedle || '%')
            AND (:minYear IS NULL OR year >= :minYear)
            AND (:maxYear IS NULL OR year <= :maxYear)
            AND (:minRating IS NULL OR imdbRating >= :minRating)
        ORDER BY
            CASE WHEN :sortBy = 'imdb_votes' AND :sortOrder = 'asc' THEN imdbVotes END ASC,
            CASE WHEN :sortBy = 'imdb_votes' AND :sortOrder = 'desc' THEN imdbVotes END DESC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'asc' THEN year END ASC,
            CASE WHEN :sortBy = 'year' AND :sortOrder = 'desc' THEN year END DESC,
            CASE WHEN :sortBy = 'imdb_rating' AND :sortOrder = 'asc' THEN imdbRating END ASC,
            CASE WHEN :sortBy = 'imdb_rating' AND :sortOrder = 'desc' THEN imdbRating END DESC,
            CASE WHEN :sortBy = 'tmdb_rating' AND :sortOrder = 'asc' THEN tmdbRating END ASC,
            CASE WHEN :sortBy = 'tmdb_rating' AND :sortOrder = 'desc' THEN tmdbRating END DESC,
            CASE WHEN :sortBy = 'popularity' AND :sortOrder = 'asc' THEN popularity END ASC,
            CASE WHEN :sortBy = 'popularity' AND :sortOrder = 'desc' THEN popularity END DESC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'asc' THEN title END COLLATE NOCASE ASC,
            CASE WHEN :sortBy = 'title' AND :sortOrder = 'desc' THEN title END COLLATE NOCASE DESC,
            title COLLATE NOCASE ASC
        """
    )
    fun pagingMoviesFiltered(
        query: String,
        genreNeedle: String?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?,
        sortBy: String,
        sortOrder: String,
    ): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies WHERE imdbId = :movieId LIMIT 1")
    fun observeMovie(movieId: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE imdbId = :movieId LIMIT 1")
    suspend fun getMovie(movieId: String): MovieEntity?

    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE (posterPath IS NOT NULL AND posterPath != '') OR (backdropPath IS NOT NULL AND backdropPath != '') OR (imagePathsJson != '[]')")
    suspend fun getQuizPool(): List<MovieEntity>

    @Query("SELECT COUNT(*) FROM movies WHERE (posterPath IS NOT NULL AND posterPath != '') OR (backdropPath IS NOT NULL AND backdropPath != '') OR (imagePathsJson != '[]')")
    fun observeQuizPoolCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE (posterPath IS NOT NULL AND posterPath != '') OR (backdropPath IS NOT NULL AND backdropPath != '') OR (imagePathsJson != '[]')")
    suspend fun getQuizPoolCount(): Int

    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun observeGenres(): Flow<List<GenreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun deleteFavorite(movieId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    fun observeIsFavorite(movieId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    suspend fun isFavorite(movieId: String): Boolean

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoriteCount(): Flow<Int>

    @Query("SELECT movies.* FROM movies INNER JOIN favorites ON movies.imdbId = favorites.movieId ORDER BY favorites.createdAt DESC")
    fun observeFavoriteMovies(): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(items: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteWatchlistItem(movieId: String)

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movieId = :movieId)")
    fun observeIsWatchlist(movieId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movieId = :movieId)")
    suspend fun isWatchlist(movieId: String): Boolean

    @Query("SELECT COUNT(*) FROM watchlist")
    fun observeWatchlistCount(): Flow<Int>

    @Query("SELECT movies.* FROM movies INNER JOIN watchlist ON movies.imdbId = watchlist.movieId ORDER BY watchlist.createdAt DESC")
    fun observeWatchlistMovies(): Flow<List<MovieEntity>>

    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Query("SELECT * FROM current_user LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("DELETE FROM current_user")
    suspend fun clearCurrentUser()

    @Insert
    suspend fun insertQuizSession(session: QuizSessionEntity)

    @Query("SELECT MAX(score) FROM quiz_sessions")
    fun observeBestQuizScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM quiz_sessions")
    fun observePlayedQuizCount(): Flow<Int>

    @Query("SELECT * FROM quiz_sessions ORDER BY playedAtEpochMillis DESC LIMIT 20")
    fun observeRecentQuizSessions(): Flow<List<QuizSessionEntity>>
}

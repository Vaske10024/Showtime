package rs.edu.raf.rma.showtime.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.data.api.ShowtimeException
import rs.edu.raf.rma.showtime.data.api.UserApi
import rs.edu.raf.rma.showtime.data.db.FavoriteEntity
import rs.edu.raf.rma.showtime.data.mappers.toSummary
import rs.edu.raf.rma.showtime.domain.MovieSummary
import kotlin.time.Clock

class FavoriteRepository(
    private val api: UserApi,
    private val database: AppDatabase,
    private val movieRepository: MovieRepository,
    private val authenticatedCallRunner: AuthenticatedCallRunner,
) {
    private val dao = database.showtimeDao()

    fun observeFavoriteMovies(): Flow<List<MovieSummary>> =
        dao.observeFavoriteMovies().map { movies -> movies.map { it.toSummary() } }

    fun observeFavoriteCount(): Flow<Int> = dao.observeFavoriteCount()

    suspend fun syncFavorites() {
        authenticatedCallRunner.execute {
            val remote = api.getFavorites()
            movieRepository.upsertListItems(remote)
            dao.clearFavorites()
            dao.insertFavorites(
                remote.map {
                    FavoriteEntity(
                        movieId = it.imdbId,
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                    )
                }
            )
        }
    }

    suspend fun setFavorite(movieId: String, enabled: Boolean) {
        val previous = dao.isFavorite(movieId)
        if (previous == enabled) return
        applyLocal(movieId, enabled)
        try {
            authenticatedCallRunner.execute {
                if (enabled) api.addFavorite(movieId) else api.removeFavorite(movieId)
            }
        } catch (throwable: Throwable) {
            if (throwable !is ShowtimeException || throwable.kind != ShowtimeException.Kind.Unauthorized) {
                applyLocal(movieId, previous)
            }
            throw throwable
        }
    }

    private suspend fun applyLocal(movieId: String, enabled: Boolean) {
        if (enabled) {
            dao.insertFavorite(FavoriteEntity(movieId = movieId, createdAt = Clock.System.now().toEpochMilliseconds()))
        } else {
            dao.deleteFavorite(movieId)
        }
    }
}

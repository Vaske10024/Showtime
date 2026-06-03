package rs.edu.raf.rma.showtime.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.data.api.ShowtimeException
import rs.edu.raf.rma.showtime.data.api.UserApi
import rs.edu.raf.rma.showtime.data.db.WatchlistEntity
import rs.edu.raf.rma.showtime.data.mappers.toSummary
import rs.edu.raf.rma.showtime.domain.MovieSummary
import kotlin.time.Clock

class WatchlistRepository(
    private val api: UserApi,
    private val database: AppDatabase,
    private val movieRepository: MovieRepository,
    private val authenticatedCallRunner: AuthenticatedCallRunner,
) {
    private val dao = database.showtimeDao()

    fun observeWatchlistMovies(): Flow<List<MovieSummary>> =
        dao.observeWatchlistMovies().map { movies -> movies.map { it.toSummary() } }

    fun observeWatchlistCount(): Flow<Int> = dao.observeWatchlistCount()

    suspend fun syncWatchlist() {
        authenticatedCallRunner.execute {
            val remote = api.getWatchlist()
            movieRepository.upsertListItems(remote)
            dao.clearWatchlist()
            dao.insertWatchlist(
                remote.map {
                    WatchlistEntity(
                        movieId = it.imdbId,
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                    )
                }
            )
        }
    }

    suspend fun setWatchlist(movieId: String, enabled: Boolean) {
        val previous = dao.isWatchlist(movieId)
        if (previous == enabled) return
        applyLocal(movieId, enabled)
        try {
            authenticatedCallRunner.execute {
                if (enabled) api.addWatchlist(movieId) else api.removeWatchlist(movieId)
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
            dao.insertWatchlistItem(WatchlistEntity(movieId = movieId, createdAt = Clock.System.now().toEpochMilliseconds()))
        } else {
            dao.deleteWatchlistItem(movieId)
        }
    }
}

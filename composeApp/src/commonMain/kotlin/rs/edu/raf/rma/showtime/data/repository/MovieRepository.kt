package rs.edu.raf.rma.showtime.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.data.api.MovieListItemDto
import rs.edu.raf.rma.showtime.data.api.MoviesApi
import rs.edu.raf.rma.showtime.data.api.toShowtimeException
import rs.edu.raf.rma.showtime.data.db.MovieEntity
import rs.edu.raf.rma.showtime.data.mappers.toDetail
import rs.edu.raf.rma.showtime.data.mappers.toDomain
import rs.edu.raf.rma.showtime.data.mappers.toEntity
import rs.edu.raf.rma.showtime.data.mappers.toSummary
import rs.edu.raf.rma.showtime.domain.Genre
import rs.edu.raf.rma.showtime.domain.MovieDetail
import rs.edu.raf.rma.showtime.domain.MovieFilters
import rs.edu.raf.rma.showtime.domain.MovieSummary
import rs.edu.raf.rma.showtime.domain.SortBy
import rs.edu.raf.rma.showtime.domain.SortOrder
import rs.edu.raf.rma.showtime.util.imageUrl

class MovieRepository(
    private val api: MoviesApi,
    private val database: AppDatabase,
    private val imageCacheWarmer: ImageCacheWarmer,
) {
    private val dao = database.showtimeDao()

    fun observeGenres(): Flow<List<Genre>> = dao.observeGenres().map { entities -> entities.map { it.toDomain() } }

    fun pagedMovies(filters: MovieFilters): Flow<PagingData<MovieSummary>> {
        return Pager(
            config = PagingConfig(
                pageSize = MOVIES_PAGE_SIZE,
                prefetchDistance = MOVIES_PREFETCH_DISTANCE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                dao.pagingMoviesFiltered(
                    query = filters.query.trim(),
                    genreNeedle = filters.genreNeedle(),
                    minYear = filters.minYear,
                    maxYear = filters.maxYear,
                    minRating = filters.minRating,
                    sortBy = filters.sortBy.apiValue,
                    sortOrder = filters.sortOrder.apiValue,
                )
            },
        ).flow.map { pagingData -> pagingData.map { it.toSummary() } }
    }

    fun observeMovieDetail(movieId: String): Flow<MovieDetail?> {
        return combine(
            dao.observeMovie(movieId),
            dao.observeIsFavorite(movieId),
            dao.observeIsWatchlist(movieId),
        ) { movie, favorite, watchlist ->
            movie?.toDetail(isFavorite = favorite, isWatchlist = watchlist)
        }
    }

    fun observeQuizPoolCount(): Flow<Int> = dao.observeQuizPoolCount()

    suspend fun syncGenres() {
        try {
            val genres = api.getGenres()
            dao.upsertGenres(genres.map { it.toEntity() })
        } catch (throwable: Throwable) {
            throw throwable.toShowtimeException()
        }
    }

    suspend fun syncCatalog(maxPages: Int = DEFAULT_SYNC_PAGES, pageSize: Int = DEFAULT_SYNC_PAGE_SIZE) {
        try {
            syncGenres()
            var page = 1
            var totalPages = 1
            val quizPosterUrls = mutableListOf<String>()
            while (page <= totalPages && page <= maxPages) {
                val response = api.getMovies(
                    page = page,
                    pageSize = pageSize,
                    query = null,
                    genreId = null,
                    minYear = null,
                    maxYear = null,
                    minRating = null,
                    sortBy = SortBy.ImdbVotes.apiValue,
                    sortOrder = SortOrder.Desc.apiValue,
                )
                upsertListItems(response.items)
                quizPosterUrls += response.items
                    .mapNotNull { item -> imageUrl(item.posterPath, size = QUIZ_POSTER_IMAGE_SIZE) }
                totalPages = response.totalPages
                page++
            }
            warmQuizPosters(quizPosterUrls)
        } catch (throwable: Throwable) {
            throw throwable.toShowtimeException()
        }
    }

    suspend fun bootstrapTopMovies() {
        if (dao.getQuizPoolCount() >= 25) return
        val response = api.getMovies(
            page = 1,
            pageSize = 100,
            query = null,
            genreId = null,
            minYear = null,
            maxYear = null,
            minRating = null,
            sortBy = SortBy.ImdbVotes.apiValue,
            sortOrder = SortOrder.Desc.apiValue,
        )
        upsertListItems(response.items)
        warmQuizPosters(response.items.mapNotNull { item -> imageUrl(item.posterPath, size = QUIZ_POSTER_IMAGE_SIZE) })
        response.items.take(30).forEach { item ->
            runCatching { syncMovieDetails(item.imdbId) }
        }
    }

    suspend fun syncMovieDetails(movieId: String) {
        try {
            val existing = dao.getMovie(movieId)
            val detail = api.getMovie(movieId)
            val cast = runCatching { api.getCast(movieId = movieId, pageSize = 30).items }.getOrElse { emptyList() }
            val images = runCatching { api.getImages(movieId) }.getOrNull()
            dao.upsertMovie(detail.toEntity(existing = existing, cast = cast, images = images))
        } catch (throwable: Throwable) {
            throw throwable.toShowtimeException()
        }
    }

    suspend fun upsertListItems(items: List<MovieListItemDto>) {
        if (items.isEmpty()) return
        val movies = items.map { item -> item.toEntity(existing = dao.getMovie(item.imdbId)) }
        dao.upsertMovies(movies)
        dao.upsertGenres(items.flatMap { it.genres }.distinctBy { it.id }.map { it.toEntity() })
    }

    private suspend fun warmQuizPosters(urls: List<String>) {
        runCatching { imageCacheWarmer.warm(urls) }
    }

    private fun MovieFilters.genreNeedle(): String? = genreId?.let { "\"id\":$it" }

    companion object {
        private const val DEFAULT_SYNC_PAGES = 3
        private const val DEFAULT_SYNC_PAGE_SIZE = 100
        private const val MOVIES_PAGE_SIZE = 20
        private const val MOVIES_PREFETCH_DISTANCE = 5
        private const val QUIZ_POSTER_IMAGE_SIZE = "w342"
    }
}

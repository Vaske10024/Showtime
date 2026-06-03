package rs.edu.raf.rma.showtime.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

private const val BASE_URL = "https://rma.finlab.rs"

class MoviesApi(
    private val client: HttpClient,
) {
    suspend fun getMovies(
        page: Int,
        pageSize: Int,
        query: String?,
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Double?,
        sortBy: String,
        sortOrder: String,
    ): PaginatedResponseDto<MovieListItemDto> = client.get("$BASE_URL/movies") {
        parameter("page", page)
        parameter("page_size", pageSize)
        if (!query.isNullOrBlank()) parameter("query", query)
        if (genreId != null) parameter("genre_id", genreId)
        if (minYear != null) parameter("min_year", minYear)
        if (maxYear != null) parameter("max_year", maxYear)
        if (minRating != null && minRating > 0.0) parameter("min_rating", minRating)
        parameter("sort_by", sortBy)
        parameter("sort_order", sortOrder)
    }.body()

    suspend fun getMovie(movieId: String): MovieDetailDto =
        client.get("$BASE_URL/movies/$movieId").body()

    suspend fun getCast(movieId: String, page: Int = 1, pageSize: Int = 20): PaginatedResponseDto<PersonSummaryDto> =
        client.get("$BASE_URL/movies/$movieId/cast") {
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body()

    suspend fun getImages(movieId: String, type: String? = null): MovieImagesDto =
        client.get("$BASE_URL/movies/$movieId/images") {
            if (type != null) parameter("type", type)
        }.body()

    suspend fun getGenres(): List<GenreDto> = client.get("$BASE_URL/genres").body()

    suspend fun getConfig(): List<ConfigEntryDto> = client.get("$BASE_URL/config").body()
}

class AuthenticationApi(
    private val client: HttpClient,
) {
    suspend fun signup(request: SignupRequestDto): AuthResponseDto =
        client.post("$BASE_URL/auth/signup") { setBody(request) }.body()

    suspend fun login(request: LoginRequestDto): AuthResponseDto =
        client.post("$BASE_URL/auth/login") { setBody(request) }.body()
}

class UserApi(
    private val client: HttpClient,
) {
    suspend fun me(): UserDto = client.get("$BASE_URL/me").body()

    suspend fun getFavorites(): List<MovieListItemDto> =
        client.get("$BASE_URL/me/favorites").body()

    suspend fun addFavorite(movieId: String) {
        client.post("$BASE_URL/me/favorites/$movieId")
    }

    suspend fun removeFavorite(movieId: String) {
        client.delete("$BASE_URL/me/favorites/$movieId")
    }

    suspend fun getWatchlist(): List<MovieListItemDto> =
        client.get("$BASE_URL/me/watchlist").body()

    suspend fun addWatchlist(movieId: String) {
        client.post("$BASE_URL/me/watchlist/$movieId")
    }

    suspend fun removeWatchlist(movieId: String) {
        client.delete("$BASE_URL/me/watchlist/$movieId")
    }

    suspend fun postQuizScore(score: Double, category: Int = 1): PostedQuizResultDto =
        client.post("$BASE_URL/leaderboard") {
            setBody(QuizScoreRequestDto(score = score, category = category))
        }.body()
}

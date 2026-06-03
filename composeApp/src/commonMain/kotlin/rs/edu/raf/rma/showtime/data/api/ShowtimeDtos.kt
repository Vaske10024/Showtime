package rs.edu.raf.rma.showtime.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponseDto<T>(
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val totalPages: Int = 1,
    val items: List<T> = emptyList(),
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)

@Serializable
data class MovieListItemDto(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Double? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val genres: List<GenreDto> = emptyList(),
)

@Serializable
data class MovieDetailDto(
    val imdbId: String,
    val tmdbId: Int? = null,
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val languageCode: String? = null,
    val popularity: Double? = null,
    val imdbRating: Double? = null,
    val imdbVotes: Int? = null,
    val tmdbRating: Double? = null,
    val tmdbVotes: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val homepage: String? = null,
    val genres: List<GenreDto> = emptyList(),
)

@Serializable
data class PersonSummaryDto(
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null,
)

@Serializable
data class MovieImageDto(
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    val voteAverage: Double? = null,
    val language: String? = null,
)

@Serializable
data class MovieImagesDto(
    val posters: List<MovieImageDto> = emptyList(),
    val backdrops: List<MovieImageDto> = emptyList(),
    val logos: List<MovieImageDto> = emptyList(),
)

@Serializable
data class ConfigEntryDto(
    val key: String,
    val value: String,
)

@Serializable
data class SignupRequestDto(
    @SerialName("full_name") val fullName: String,
    val username: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class QuizScoreRequestDto(
    val score: Double,
    val category: Int,
)

@Serializable
data class PostedQuizResultDto(
    val result: QuizResultDto,
    val ranking: Int,
)

@Serializable
data class QuizResultDto(
    val id: Int,
    val category: Int,
    val score: Double,
    @SerialName("played_at") val playedAt: Long,
)

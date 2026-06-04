package rs.edu.raf.rma.showtime.domain

import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    val id: Int,
    val name: String,
)

@Serializable
data class CastMember(
    val imdbId: String,
    val name: String,
    val department: String? = null,
    val profilePath: String? = null,
)

data class MovieSummary(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Double?,
    val imdbVotes: Int?,
    val posterPath: String?,
    val backdropPath: String?,
    val genres: List<Genre>,
)

data class MovieDetail(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val runtime: Int?,
    val genres: List<Genre>,
    val imdbRating: Double?,
    val imdbVotes: Int?,
    val tmdbRating: Double?,
    val tmdbVotes: Int?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val cast: List<CastMember>,
    val imagePaths: List<String>,
    val isFavorite: Boolean,
    val isWatchlist: Boolean,
)

data class UserProfile(
    val id: Int,
    val username: String,
    val fullName: String,
)

enum class SortBy(val apiValue: String, val label: String) {
    ImdbVotes("imdb_votes", "IMDb votes"),
    Year("year", "Year"),
    ImdbRating("imdb_rating", "IMDb rating"),
    TmdbRating("tmdb_rating", "TMDB rating"),
    Popularity("popularity", "Popularity"),
    Title("title", "Title");
}

enum class SortOrder(val apiValue: String, val label: String) {
    Desc("desc", "Descending"),
    Asc("asc", "Ascending");
}

data class MovieFilters(
    val query: String = "",
    val genreId: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Double? = null,
    val sortBy: SortBy = SortBy.ImdbVotes,
    val sortOrder: SortOrder = SortOrder.Desc,
)

enum class QuizQuestionType {
    GuessMovie,
    GuessMovieYear,
    GuessLeadActor,
}

data class QuizAnswer(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
)

data class QuizQuestion(
    val id: String,
    val type: QuizQuestionType,
    val title: String,
    val imagePath: String,
    val imageSize: String = "w342",
    val answers: List<QuizAnswer>,
)

data class QuizResult(
    val score: Double,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val usedTimeSeconds: Int,
)

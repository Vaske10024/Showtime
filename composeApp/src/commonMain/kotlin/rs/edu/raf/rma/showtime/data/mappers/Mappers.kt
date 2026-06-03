package rs.edu.raf.rma.showtime.data.mappers


import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.networking.NetworkingJson
import rs.edu.raf.rma.showtime.data.api.AuthResponseDto
import rs.edu.raf.rma.showtime.data.api.GenreDto
import rs.edu.raf.rma.showtime.data.api.MovieDetailDto
import rs.edu.raf.rma.showtime.data.api.MovieImagesDto
import rs.edu.raf.rma.showtime.data.api.MovieListItemDto
import rs.edu.raf.rma.showtime.data.api.PersonSummaryDto
import rs.edu.raf.rma.showtime.data.api.UserDto
import rs.edu.raf.rma.showtime.data.db.GenreEntity
import rs.edu.raf.rma.showtime.data.db.MovieEntity
import rs.edu.raf.rma.showtime.data.db.UserEntity
import rs.edu.raf.rma.showtime.domain.CastMember
import rs.edu.raf.rma.showtime.domain.Genre
import rs.edu.raf.rma.showtime.domain.MovieDetail
import rs.edu.raf.rma.showtime.domain.MovieSummary
import rs.edu.raf.rma.showtime.domain.UserProfile
import kotlin.time.Clock

fun GenreDto.toDomain(): Genre = Genre(id = id, name = name)
fun GenreDto.toEntity(): GenreEntity = GenreEntity(id = id, name = name)
fun GenreEntity.toDomain(): Genre = Genre(id = id, name = name)

fun UserDto.toEntity(): UserEntity = UserEntity(id = id, username = username, fullName = fullName)
fun UserEntity.toDomain(): UserProfile = UserProfile(id = id, username = username, fullName = fullName)

fun AuthResponseDto.toAuthData(): AuthData {
    val expiresAt = Clock.System.now().toEpochMilliseconds() / 1000 + expiresIn
    return AuthData(
        accessToken = accessToken,
        expiresAtEpochSeconds = expiresAt,
        userId = user.id,
        username = user.username,
        fullName = user.fullName,
    )
}

fun MovieListItemDto.toEntity(existing: MovieEntity? = null): MovieEntity {
    val now = Clock.System.now().toEpochMilliseconds()
    val genreDomains = genres.map { it.toDomain() }
    return MovieEntity(
        imdbId = imdbId,
        title = title,
        year = year,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = existing?.tmdbRating,
        tmdbVotes = existing?.tmdbVotes,
        popularity = existing?.popularity,
        posterPath = posterPath ?: existing?.posterPath,
        backdropPath = existing?.backdropPath,
        runtime = existing?.runtime,
        overview = existing?.overview,
        releaseDate = existing?.releaseDate,
        genresJson = NetworkingJson.encodeToString(genreDomains),
        castJson = existing?.castJson ?: "[]",
        imagePathsJson = existing?.imagePathsJson ?: "[]",
        listSyncedAt = now,
        detailsSyncedAt = existing?.detailsSyncedAt ?: 0L,
    )
}

fun MovieDetailDto.toEntity(
    existing: MovieEntity? = null,
    cast: List<PersonSummaryDto> = emptyList(),
    images: MovieImagesDto? = null,
): MovieEntity {
    val now = Clock.System.now().toEpochMilliseconds()
    val genreDomains = genres.map { it.toDomain() }
    val castDomains = cast.map {
        CastMember(
            imdbId = it.imdbId,
            name = it.name,
            department = it.department,
            profilePath = it.profilePath,
        )
    }
    val imagePaths = buildList {
        posterPath?.let(::add)
        backdropPath?.let(::add)
        images?.posters?.map { it.filePath }?.let(::addAll)
        images?.backdrops?.map { it.filePath }?.let(::addAll)
    }.distinct()

    return MovieEntity(
        imdbId = imdbId,
        title = title,
        year = year,
        imdbRating = imdbRating,
        imdbVotes = imdbVotes,
        tmdbRating = tmdbRating,
        tmdbVotes = tmdbVotes,
        popularity = popularity,
        posterPath = posterPath ?: existing?.posterPath,
        backdropPath = backdropPath ?: existing?.backdropPath,
        runtime = runtime,
        overview = overview,
        releaseDate = releaseDate,
        genresJson = NetworkingJson.encodeToString(genreDomains),
        castJson = NetworkingJson.encodeToString(castDomains),
        imagePathsJson = NetworkingJson.encodeToString(imagePaths),
        listSyncedAt = existing?.listSyncedAt ?: now,
        detailsSyncedAt = now,
    )
}

fun MovieEntity.toSummary(): MovieSummary = MovieSummary(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genres = decodeGenres(),
)

fun MovieEntity.toDetail(isFavorite: Boolean, isWatchlist: Boolean): MovieDetail = MovieDetail(
    imdbId = imdbId,
    title = title,
    year = year,
    runtime = runtime,
    genres = decodeGenres(),
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    tmdbRating = tmdbRating,
    tmdbVotes = tmdbVotes,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    cast = decodeCast(),
    imagePaths = decodeImagePaths(),
    isFavorite = isFavorite,
    isWatchlist = isWatchlist,
)

fun MovieEntity.decodeGenres(): List<Genre> = safeDecode(genresJson)
fun MovieEntity.decodeCast(): List<CastMember> = safeDecode(castJson)
fun MovieEntity.decodeImagePaths(): List<String> = safeDecode(imagePathsJson)

private inline fun <reified T> safeDecode(json: String): List<T> = try {
    NetworkingJson.decodeFromString<List<T>>(json)
} catch (_: Throwable) {
    emptyList()
}

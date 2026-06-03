package rs.edu.raf.rma.showtime.data.repository

import kotlinx.coroutines.flow.Flow

import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.data.api.UserApi
import rs.edu.raf.rma.showtime.data.db.MovieEntity
import rs.edu.raf.rma.showtime.data.db.QuizSessionEntity
import rs.edu.raf.rma.showtime.data.mappers.decodeCast
import rs.edu.raf.rma.showtime.data.mappers.decodeImagePaths
import rs.edu.raf.rma.showtime.domain.QuizAnswer
import rs.edu.raf.rma.showtime.domain.QuizQuestion
import rs.edu.raf.rma.showtime.domain.QuizQuestionType
import rs.edu.raf.rma.showtime.domain.QuizResult
import rs.edu.raf.rma.showtime.domain.QuizScoring
import kotlin.random.Random
import kotlin.time.Clock

class QuizRepository(
    private val api: UserApi,
    private val database: AppDatabase,
    private val authenticatedCallRunner: AuthenticatedCallRunner,
) {
    private val dao = database.showtimeDao()

    fun observeQuizPoolCount(): Flow<Int> = dao.observeQuizPoolCount()
    fun observeBestScore(): Flow<Double?> = dao.observeBestQuizScore()
    fun observePlayedCount(): Flow<Int> = dao.observePlayedQuizCount()

    suspend fun generateSession(): List<QuizQuestion> {
        val pool = dao.getQuizPool()
            .filter { it.title.isNotBlank() && it.imageCandidates().isNotEmpty() }
            .shuffled()
        if (pool.size < 10) {
            throw IllegalStateException("Browse the catalog first to populate your quiz pool.")
        }

        val allActors = pool.flatMap { it.decodeCast() }
            .filter { it.department.equals("Acting", ignoreCase = true) }
            .distinctBy { it.name }
            .filter { it.name.isNotBlank() }

        val typePlan = mutableListOf(
            QuizQuestionType.GuessMovie,
            QuizQuestionType.GuessMovie,
            QuizQuestionType.GuessMovie,
            QuizQuestionType.GuessMovieYear,
            QuizQuestionType.GuessMovieYear,
            QuizQuestionType.GuessMovieYear,
            QuizQuestionType.GuessLeadActor,
            QuizQuestionType.GuessLeadActor,
            QuizQuestionType.GuessLeadActor,
        ).apply {
            add(listOf(QuizQuestionType.GuessMovie, QuizQuestionType.GuessMovieYear, QuizQuestionType.GuessLeadActor).random())
            shuffle()
        }

        val usedImages = mutableSetOf<String>()
        val usedMovies = mutableSetOf<String>()
        val questions = mutableListOf<QuizQuestion>()

        for (type in typePlan) {
            val generated = generateQuestion(type, pool, allActors, usedImages, usedMovies)
                ?: QuizQuestionType.values().firstNotNullOfOrNull { fallback ->
                    generateQuestion(fallback, pool, allActors, usedImages, usedMovies)
                }
            if (generated != null) {
                questions += generated
                usedImages += generated.imagePath
                usedMovies += generated.id.substringBefore('|')
            }
            if (questions.size == 10) break
        }

        var safety = 0
        while (questions.size < 10 && safety < 100) {
            safety++
            val generated = QuizQuestionType.values().toList().shuffled().firstNotNullOfOrNull { type ->
                generateQuestion(type, pool, allActors, usedImages, usedMovies)
            }
            if (generated != null) {
                questions += generated
                usedImages += generated.imagePath
                usedMovies += generated.id.substringBefore('|')
            } else break
        }

        if (questions.size < 10) {
            throw IllegalStateException("Browse the catalog first to populate your quiz pool.")
        }
        return questions.take(10)
    }

    suspend fun saveResult(result: QuizResult) {
        dao.insertQuizSession(
            QuizSessionEntity(
                category = 1,
                score = result.score,
                correctAnswers = result.correctAnswers,
                incorrectAnswers = result.incorrectAnswers,
                usedTimeSeconds = result.usedTimeSeconds,
                playedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            )
        )
        runCatching {
            authenticatedCallRunner.execute { api.postQuizScore(score = result.score, category = 1) }
        }
    }

    fun score(correctAnswers: Int, remainingSeconds: Int): Double {
        return QuizScoring.score(correctAnswers, remainingSeconds)
    }

    private fun generateQuestion(
        type: QuizQuestionType,
        pool: List<MovieEntity>,
        allActors: List<rs.edu.raf.rma.showtime.domain.CastMember>,
        usedImages: Set<String>,
        usedMovies: Set<String>,
    ): QuizQuestion? {
        repeat(150) {
            val movie = pool.random()
            if (movie.imdbId in usedMovies && usedMovies.size < pool.size - 1) return@repeat
            val question = when (type) {
                QuizQuestionType.GuessMovie -> guessMovie(movie, pool, usedImages)
                QuizQuestionType.GuessMovieYear -> guessMovieYear(movie, usedImages)
                QuizQuestionType.GuessLeadActor -> guessLeadActor(movie, allActors, usedImages)
            }
            if (question != null) return question
        }
        return null
    }

    private fun guessMovie(movie: MovieEntity, pool: List<MovieEntity>, usedImages: Set<String>): QuizQuestion? {
        val image = movie.imageCandidates().firstOrNull { it !in usedImages } ?: return null
        val wrong = pool.filter { it.imdbId != movie.imdbId }.shuffled().map { it.title }.distinct().take(3)
        if (wrong.size < 3) return null
        val answers = (wrong.map { QuizAnswer(it, it, false) } + QuizAnswer(movie.title, movie.title, true)).shuffled()
        return QuizQuestion(
            id = "${movie.imdbId}|movie|${Random.nextInt()}",
            type = QuizQuestionType.GuessMovie,
            title = "Guess the Movie",
            imagePath = image,
            answers = answers,
        )
    }

    private fun guessMovieYear(movie: MovieEntity, usedImages: Set<String>): QuizQuestion? {
        val year = movie.year ?: return null
        val image = movie.posterPath?.takeIf { it !in usedImages } ?: movie.imageCandidates().firstOrNull { it !in usedImages } ?: return null
        val wrongYears = mutableSetOf<Int>()
        val offsets = ((-10..-1) + (1..10)).shuffled()
        offsets.forEach { offset ->
            if (wrongYears.size < 3) wrongYears += year + offset
        }
        if (wrongYears.size < 3) return null
        val answers = (wrongYears.map { QuizAnswer(it.toString(), it.toString(), false) } + QuizAnswer(year.toString(), year.toString(), true)).shuffled()
        return QuizQuestion(
            id = "${movie.imdbId}|year|${Random.nextInt()}",
            type = QuizQuestionType.GuessMovieYear,
            title = "${movie.title}: release year?",
            imagePath = image,
            answers = answers,
        )
    }

    private fun guessLeadActor(
        movie: MovieEntity,
        allActors: List<rs.edu.raf.rma.showtime.domain.CastMember>,
        usedImages: Set<String>,
    ): QuizQuestion? {
        val cast = movie.decodeCast().filter { it.name.isNotBlank() && it.department.equals("Acting", ignoreCase = true) }
        val correct = cast.take(3).randomOrNull() ?: return null
        val wrong = allActors
            .filter { actor -> cast.none { it.name == actor.name } }
            .map { it.name }
            .distinct()
            .shuffled()
            .take(3)
        if (wrong.size < 3) return null
        val image = movie.posterPath?.takeIf { it !in usedImages } ?: movie.imageCandidates().firstOrNull { it !in usedImages } ?: return null
        val answers = (wrong.map { QuizAnswer(it, it, false) } + QuizAnswer(correct.name, correct.name, true)).shuffled()
        return QuizQuestion(
            id = "${movie.imdbId}|actor|${Random.nextInt()}",
            type = QuizQuestionType.GuessLeadActor,
            title = "Who is a lead actor in ${movie.title}?",
            imagePath = image,
            answers = answers,
        )
    }

    private fun MovieEntity.imageCandidates(): List<String> = buildList {
        backdropPath?.let(::add)
        posterPath?.let(::add)
        addAll(decodeImagePaths())
    }.filter { it.isNotBlank() }.distinct()

    companion object {
        const val QUIZ_DURATION_SECONDS = QuizScoring.DurationSeconds
        const val QUESTION_COUNT = QuizScoring.QuestionCount
    }
}

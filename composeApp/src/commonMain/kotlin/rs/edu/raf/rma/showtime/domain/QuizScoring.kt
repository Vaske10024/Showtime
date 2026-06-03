package rs.edu.raf.rma.showtime.domain

import kotlin.math.min

object QuizScoring {
    const val DurationSeconds = 60
    const val QuestionCount = 10

    fun score(correctAnswers: Int, remainingSeconds: Int): Double {
        val boundedRemaining = remainingSeconds.coerceIn(0, DurationSeconds)
        val boundedCorrect = correctAnswers.coerceIn(0, QuestionCount)
        val raw = boundedCorrect * (9.0 + boundedRemaining.toDouble() / DurationSeconds)
        return min(100.0, raw).coerceAtLeast(0.0)
    }
}

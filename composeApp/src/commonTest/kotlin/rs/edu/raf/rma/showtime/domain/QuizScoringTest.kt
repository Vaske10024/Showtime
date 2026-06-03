package rs.edu.raf.rma.showtime.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class QuizScoringTest {

    @Test
    fun scoreRewardsCorrectAnswersAndRemainingTime() {
        assertEquals(95.0, QuizScoring.score(correctAnswers = 10, remainingSeconds = 30))
    }

    @Test
    fun scoreIsCappedAtOneHundred() {
        assertEquals(100.0, QuizScoring.score(correctAnswers = 10, remainingSeconds = 60))
    }

    @Test
    fun scoreDoesNotGoBelowZeroForInvalidInput() {
        assertEquals(0.0, QuizScoring.score(correctAnswers = -3, remainingSeconds = -20))
    }
}

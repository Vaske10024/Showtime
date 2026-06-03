package rs.edu.raf.rma.showtime.ui.quiz

import rs.edu.raf.rma.showtime.domain.QuizQuestion
import rs.edu.raf.rma.showtime.domain.QuizResult

enum class QuizPhase { Intro, Playing, Result }

data class QuizViewState(
    val phase: QuizPhase = QuizPhase.Intro,
    val quizPoolCount: Int = 0,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswerId: String? = null,
    val showAnswerFeedback: Boolean = false,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val remainingSeconds: Int = 60,
    val isGenerating: Boolean = false,
    val showAbandonDialog: Boolean = false,
    val result: QuizResult? = null,
    val errorMessage: String? = null,
) {
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)
}

sealed interface QuizIntent {
    data object Start : QuizIntent
    data class SelectAnswer(val answerId: String) : QuizIntent
    data object BackPressed : QuizIntent
    data object ConfirmAbandon : QuizIntent
    data object DismissAbandon : QuizIntent
    data object ResetToIntro : QuizIntent
    data object ClearError : QuizIntent
}

sealed interface QuizEffect {
    data class ShowMessage(val message: String) : QuizEffect
    data object ExitQuiz : QuizEffect
}

object QuizReducer {
    fun reduce(state: QuizViewState, intent: QuizIntent): QuizViewState = when (intent) {
        QuizIntent.Start -> state.copy(isGenerating = true, errorMessage = null, result = null)
        is QuizIntent.SelectAnswer -> state.copy(selectedAnswerId = intent.answerId, showAnswerFeedback = true)
        QuizIntent.BackPressed -> state.copy(showAbandonDialog = true)
        QuizIntent.ConfirmAbandon -> QuizViewState(quizPoolCount = state.quizPoolCount)
        QuizIntent.DismissAbandon -> state.copy(showAbandonDialog = false)
        QuizIntent.ResetToIntro -> QuizViewState(quizPoolCount = state.quizPoolCount)
        QuizIntent.ClearError -> state.copy(errorMessage = null)
    }
}

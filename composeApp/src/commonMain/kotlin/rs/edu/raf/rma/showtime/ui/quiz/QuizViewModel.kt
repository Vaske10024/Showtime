package rs.edu.raf.rma.showtime.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.data.repository.QuizRepository
import rs.edu.raf.rma.showtime.domain.QuizResult
import kotlin.time.Clock

class QuizViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(QuizViewState())
    val state: StateFlow<QuizViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<QuizEffect>()
    val effects: SharedFlow<QuizEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null
    private var startedAtMillis: Long = 0L

    init {
        viewModelScope.launch {
            quizRepository.observeQuizPoolCount().collect { count ->
                _state.getAndUpdate { it.copy(quizPoolCount = count) }
            }
        }
    }

    fun onIntent(intent: QuizIntent) {
        when (intent) {
            QuizIntent.Start -> startQuiz()
            is QuizIntent.SelectAnswer -> selectAnswer(intent.answerId)
            QuizIntent.BackPressed -> if (_state.value.phase == QuizPhase.Playing) _state.getAndUpdate { QuizReducer.reduce(it, intent) }
            QuizIntent.ConfirmAbandon -> abandon()
            QuizIntent.DismissAbandon -> _state.getAndUpdate { QuizReducer.reduce(it, intent) }
            QuizIntent.ResetToIntro -> {
                timerJob?.cancel()
                _state.getAndUpdate { QuizReducer.reduce(it, intent) }
            }
            QuizIntent.ClearError -> _state.getAndUpdate { QuizReducer.reduce(it, intent) }
        }
    }

    private fun startQuiz() {
        if (_state.value.isGenerating) return
        viewModelScope.launch {
            _state.getAndUpdate { QuizReducer.reduce(it, QuizIntent.Start) }
            runCatching { quizRepository.generateSession() }
                .onSuccess { questions ->
                    startedAtMillis = Clock.System.now().toEpochMilliseconds()
                    _state.value = QuizViewState(
                        phase = QuizPhase.Playing,
                        quizPoolCount = _state.value.quizPoolCount,
                        questions = questions,
                        remainingSeconds = QuizRepository.QUIZ_DURATION_SECONDS,
                    )
                    startTimer()
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Browse the catalog first to populate your quiz pool."
                    _state.getAndUpdate { it.copy(isGenerating = false, errorMessage = message) }
                    _effects.emit(QuizEffect.ShowMessage(message))
                }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.phase == QuizPhase.Playing && _state.value.remainingSeconds > 0) {
                delay(1000)
                val current = _state.value
                if (current.phase != QuizPhase.Playing) break
                val next = (current.remainingSeconds - 1).coerceAtLeast(0)
                _state.getAndUpdate { it.copy(remainingSeconds = next) }
                if (next == 0) finishQuiz()
            }
        }
    }

    private fun selectAnswer(answerId: String) {
        val current = _state.value
        val question = current.currentQuestion ?: return
        if (current.selectedAnswerId != null || current.showAnswerFeedback) return
        viewModelScope.launch {
            val selected = question.answers.firstOrNull { it.id == answerId } ?: return@launch
            val correct = selected.isCorrect
            _state.getAndUpdate {
                it.copy(
                    selectedAnswerId = answerId,
                    showAnswerFeedback = true,
                    correctAnswers = it.correctAnswers + if (correct) 1 else 0,
                    incorrectAnswers = it.incorrectAnswers + if (correct) 0 else 1,
                )
            }
            delay(850)
            val afterFeedback = _state.value
            if (afterFeedback.phase != QuizPhase.Playing) return@launch
            if (afterFeedback.currentIndex >= afterFeedback.questions.lastIndex) {
                finishQuiz()
            } else {
                _state.getAndUpdate {
                    it.copy(
                        currentIndex = it.currentIndex + 1,
                        selectedAnswerId = null,
                        showAnswerFeedback = false,
                    )
                }
            }
        }
    }

    private suspend fun finishQuiz() {
        val current = _state.value
        if (current.phase != QuizPhase.Playing) return
        val usedTime = (QuizRepository.QUIZ_DURATION_SECONDS - current.remainingSeconds).coerceIn(0, QuizRepository.QUIZ_DURATION_SECONDS)
        val unanswered = (current.questions.size - current.correctAnswers - current.incorrectAnswers).coerceAtLeast(0)
        val result = QuizResult(
            score = quizRepository.score(current.correctAnswers, current.remainingSeconds),
            correctAnswers = current.correctAnswers,
            incorrectAnswers = current.incorrectAnswers + unanswered,
            usedTimeSeconds = usedTime,
        )
        _state.getAndUpdate {
            it.copy(
                phase = QuizPhase.Result,
                result = result,
                showAbandonDialog = false,
                selectedAnswerId = null,
                showAnswerFeedback = false,
            )
        }
        quizRepository.saveResult(result)
    }

    private fun abandon() {
        timerJob?.cancel()
        viewModelScope.launch {
            _state.getAndUpdate { QuizReducer.reduce(it, QuizIntent.ConfirmAbandon) }
            _effects.emit(QuizEffect.ExitQuiz)
        }
    }
}

package rs.edu.raf.rma.showtime.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.backhandler.BackHandler
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.showtime.domain.QuizAnswer
import rs.edu.raf.rma.showtime.domain.QuizQuestion
import rs.edu.raf.rma.showtime.util.imageUrl
import rs.edu.raf.rma.showtime.util.scoreText

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun QuizScreen(
    onExitQuiz: () -> Unit,
    viewModel: QuizViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = state.phase == QuizPhase.Playing) {
        viewModel.onIntent(QuizIntent.BackPressed)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is QuizEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                QuizEffect.ExitQuiz -> onExitQuiz()
            }
        }
    }

    QuizContent(state, snackbarHostState, viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    state: QuizViewState,
    snackbarHostState: SnackbarHostState,
    onIntent: (QuizIntent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Movie Knowledge") })
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.phase) {
                QuizPhase.Intro -> QuizIntro(state, onIntent)
                QuizPhase.Playing -> QuizPlaying(state, onIntent)
                QuizPhase.Result -> QuizResultContent(state, onIntent)
            }
        }
    }
    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(QuizIntent.DismissAbandon) },
            title = { Text("Abandon quiz?") },
            text = { Text("Abandon quiz? Your progress will be lost.") },
            confirmButton = { Button(onClick = { onIntent(QuizIntent.ConfirmAbandon) }) { Text("Abandon") } },
            dismissButton = { OutlinedButton(onClick = { onIntent(QuizIntent.DismissAbandon) }) { Text("Continue") } },
        )
    }
}

@Composable
private fun QuizIntro(state: QuizViewState, onIntent: (QuizIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Text("Movie Knowledge", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("10 questions. 60 seconds. One answer per question.", textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Local quiz pool: ${state.quizPoolCount} movies with images")
        if (state.quizPoolCount < 10) {
            Text(
                "Browse the catalog first to populate your quiz pool.",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onIntent(QuizIntent.Start) },
            enabled = !state.isGenerating && state.quizPoolCount >= 10,
        ) {
            if (state.isGenerating) CircularProgressIndicator(strokeWidth = 2.dp) else Text("Start quiz")
        }
    }
}

@Composable
private fun QuizPlaying(state: QuizViewState, onIntent: (QuizIntent) -> Unit) {
    if (state.questions.isEmpty()) return
    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.questions.size },
    )

    LaunchedEffect(state.currentIndex, state.questions.size) {
        if (pagerState.currentPage != state.currentIndex && state.currentIndex in state.questions.indices) {
            pagerState.animateScrollToPage(state.currentIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Question ${state.currentIndex + 1}/10", style = MaterialTheme.typography.titleMedium)
            Text("${state.remainingSeconds}s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { state.remainingSeconds / 60f },
            modifier = Modifier.fillMaxWidth(),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            userScrollEnabled = false,
        ) { page ->
            val isCurrentPage = page == state.currentIndex
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            ) {
                QuestionCard(
                    question = state.questions[page],
                    selectedAnswerId = state.selectedAnswerId.takeIf { isCurrentPage },
                    showFeedback = state.showAnswerFeedback && isCurrentPage,
                    onAnswer = {
                        if (isCurrentPage) onIntent(QuizIntent.SelectAnswer(it))
                    },
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selectedAnswerId: String?,
    showFeedback: Boolean,
    onAnswer: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(question.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Card(Modifier.fillMaxWidth()) {
            val url = imageUrl(question.imagePath, size = "w780")
            if (url == null) {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f), contentAlignment = Alignment.Center) { Text("No image") }
            } else {
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        question.answers.forEach { answer ->
            AnswerButton(
                answer = answer,
                selected = selectedAnswerId == answer.id,
                showFeedback = showFeedback,
                enabled = selectedAnswerId == null && !showFeedback,
                onClick = { onAnswer(answer.id) },
            )
        }
    }
}

@Composable
private fun AnswerButton(
    answer: QuizAnswer,
    selected: Boolean,
    showFeedback: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val container = when {
        showFeedback && answer.isCorrect -> MaterialTheme.colorScheme.primaryContainer
        showFeedback && selected && !answer.isCorrect -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    OutlinedButton(
        modifier = Modifier.fillMaxWidth().background(container),
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(answer.text)
            if (showFeedback && answer.isCorrect) Icon(Icons.Default.Check, contentDescription = "Correct")
            if (showFeedback && selected && !answer.isCorrect) Icon(Icons.Default.Close, contentDescription = "Wrong")
        }
    }
}

@Composable
private fun QuizResultContent(state: QuizViewState, onIntent: (QuizIntent) -> Unit) {
    val result = state.result ?: return
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Result", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(scoreText(result.score), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text("Score from 0 to 100")
        Spacer(Modifier.height(24.dp))
        Text("Correct answers: ${result.correctAnswers}")
        Text("Incorrect answers: ${result.incorrectAnswers}")
        Text("Used time: ${result.usedTimeSeconds}s")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onIntent(QuizIntent.ResetToIntro) }) { Text("Back to quiz start") }
    }
}

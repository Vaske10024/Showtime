package rs.edu.raf.rma.showtime.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.showtime.ui.components.CenterMessage
import rs.edu.raf.rma.showtime.util.scoreText

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }
    ProfileContent(state, snackbarHostState, viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileViewState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ProfileIntent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { onIntent(ProfileIntent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh profile")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading && state.user == null) {
            CenterMessage("Loading profile...", Modifier.fillMaxSize().padding(padding), true)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.isOffline) Text("Offline: showing cached profile data.", color = MaterialTheme.colorScheme.tertiary)
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.user?.fullName ?: "Unknown user", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("@${state.user?.username ?: "unknown"}")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Favorite", state.favoriteCount.toString(), Modifier.weight(1f))
                    StatCard("Watchlist", state.watchlistCount.toString(), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Best score", scoreText(state.bestScore), Modifier.weight(1f))
                    StatCard("Played quizzes", state.playedQuizzes.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onIntent(ProfileIntent.Logout) },
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Text(" Logout")
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onIntent(ProfileIntent.Refresh) },
                ) { Text("Refresh from server") }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

package rs.edu.raf.rma.showtime.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AuthEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Showtime", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "Movie catalog, Favorite movies, Watchlist and quiz.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            when (state.mode) {
                AuthMode.Landing -> LandingContent(viewModel::onIntent)
                AuthMode.Login -> LoginContent(state, viewModel::onIntent)
                AuthMode.Signup -> SignupContent(state, viewModel::onIntent)
            }
        }
    }
}

@Composable
private fun LandingContent(onIntent: (AuthIntent) -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(AuthIntent.OpenLogin) },
    ) { Text("Login") }
    Spacer(Modifier.height(12.dp))
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(AuthIntent.OpenSignup) },
    ) { Text("Create account") }
}

@Composable
private fun LoginContent(state: AuthViewState, onIntent: (AuthIntent) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.username,
        onValueChange = { onIntent(AuthIntent.UsernameChanged(it)) },
        label = { Text("Username") },
        singleLine = true,
        enabled = !state.isLoading,
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.password,
        onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
        label = { Text("Password") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        enabled = !state.isLoading,
    )
    ErrorText(state.errorMessage)
    Spacer(Modifier.height(16.dp))
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(AuthIntent.SubmitLogin) },
        enabled = !state.isLoading,
    ) {
        if (state.isLoading) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp) else Text("Login")
    }
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(AuthIntent.BackToLanding) },
        enabled = !state.isLoading,
    ) { Text("Back") }
}

@Composable
private fun SignupContent(state: AuthViewState, onIntent: (AuthIntent) -> Unit) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.fullName,
        onValueChange = { onIntent(AuthIntent.FullNameChanged(it)) },
        label = { Text("Full name") },
        singleLine = true,
        enabled = !state.isLoading,
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.username,
        onValueChange = { onIntent(AuthIntent.UsernameChanged(it)) },
        label = { Text("Username") },
        supportingText = { Text("Letters, digits and underscore; at least 3 characters.") },
        singleLine = true,
        enabled = !state.isLoading,
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.password,
        onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
        label = { Text("Password") },
        supportingText = { Text("At least 8 characters.") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        enabled = !state.isLoading,
    )
    ErrorText(state.errorMessage)
    Spacer(Modifier.height(16.dp))
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(AuthIntent.SubmitSignup) },
        enabled = !state.isLoading,
    ) {
        if (state.isLoading) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp) else Text("Register")
    }
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onIntent(AuthIntent.BackToLanding) },
        enabled = !state.isLoading,
    ) { Text("Back") }
}

@Composable
private fun ErrorText(message: String?) {
    if (message != null) {
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

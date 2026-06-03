package rs.edu.raf.rma.showtime

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.showtime.ui.auth.AuthScreen
import rs.edu.raf.rma.showtime.ui.navigation.ShowtimeNavigation
import rs.edu.raf.rma.showtime.ui.root.RootViewModel

@Composable
fun ShowtimeApp() {
    val rootViewModel: RootViewModel = koinViewModel()
    val authState by rootViewModel.authState.collectAsState(AuthState.Unauthenticated)

    MaterialTheme {
        when (authState) {
            is AuthState.Authenticated -> ShowtimeNavigation()
            AuthState.Unauthenticated -> AuthScreen()
        }
    }
}

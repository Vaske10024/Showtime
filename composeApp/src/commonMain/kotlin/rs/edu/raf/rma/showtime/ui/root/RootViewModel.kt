package rs.edu.raf.rma.showtime.ui.root

import androidx.lifecycle.ViewModel
import rs.edu.raf.rma.showtime.data.repository.AuthRepository

class RootViewModel(
    authRepository: AuthRepository,
) : ViewModel() {
    val authState = authRepository.authState
}

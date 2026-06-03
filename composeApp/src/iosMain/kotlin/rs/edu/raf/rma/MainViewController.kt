package rs.edu.raf.rma

import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import rs.edu.raf.rma.di.initKoin
import rs.edu.raf.rma.showtime.ShowtimeApp

@Suppress("unused")
fun MainViewController(): platform.UIKit.UIViewController {
    Napier.base(DebugAntilog())
    initKoin()
    return ComposeUIViewController { ShowtimeApp() }
}

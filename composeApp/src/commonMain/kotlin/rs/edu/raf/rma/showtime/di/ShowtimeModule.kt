package rs.edu.raf.rma.showtime.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.showtime.data.repository.AuthRepository
import rs.edu.raf.rma.showtime.data.repository.AuthSessionManager
import rs.edu.raf.rma.showtime.data.repository.AuthenticatedCallRunner
import rs.edu.raf.rma.showtime.data.repository.FavoriteRepository
import rs.edu.raf.rma.showtime.data.repository.ImageCacheWarmer
import rs.edu.raf.rma.showtime.data.repository.MovieRepository
import rs.edu.raf.rma.showtime.data.repository.QuizRepository
import rs.edu.raf.rma.showtime.data.repository.WatchlistRepository
import rs.edu.raf.rma.showtime.ui.auth.AuthViewModel
import rs.edu.raf.rma.showtime.ui.detail.MovieDetailViewModel
import rs.edu.raf.rma.showtime.ui.favorite.FavoriteViewModel
import rs.edu.raf.rma.showtime.ui.movies.MoviesViewModel
import rs.edu.raf.rma.showtime.ui.profile.ProfileViewModel
import rs.edu.raf.rma.showtime.ui.quiz.QuizViewModel
import rs.edu.raf.rma.showtime.ui.root.RootViewModel
import rs.edu.raf.rma.showtime.ui.watchlist.WatchlistViewModel

val showtimeModule = module {
    single { AuthSessionManager(authStore = get(), database = get()) }
    single { AuthenticatedCallRunner(sessionManager = get()) }
    single { ImageCacheWarmer(platformContext = get()) }
    single { MovieRepository(api = get(), database = get(), imageCacheWarmer = get()) }
    single { FavoriteRepository(api = get(), database = get(), movieRepository = get(), authenticatedCallRunner = get()) }
    single { WatchlistRepository(api = get(), database = get(), movieRepository = get(), authenticatedCallRunner = get()) }
    single { QuizRepository(api = get(), database = get(), authenticatedCallRunner = get()) }
    single {
        AuthRepository(
            authenticationApi = get(),
            userApi = get(),
            authStore = get(),
            sessionManager = get(),
            authenticatedCallRunner = get(),
            database = get(),
        )
    }

    viewModelOf(::RootViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::MoviesViewModel)
    viewModelOf(::MovieDetailViewModel)
    viewModelOf(::FavoriteViewModel)
    viewModelOf(::WatchlistViewModel)
    viewModelOf(::QuizViewModel)
    viewModelOf(::ProfileViewModel)
}

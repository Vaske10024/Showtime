package rs.edu.raf.rma.showtime.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import rs.edu.raf.rma.showtime.ui.detail.MovieDetailScreen
import rs.edu.raf.rma.showtime.ui.favorite.FavoriteScreen
import rs.edu.raf.rma.showtime.ui.movies.MoviesScreen
import rs.edu.raf.rma.showtime.ui.profile.ProfileScreen
import rs.edu.raf.rma.showtime.ui.quiz.QuizScreen
import rs.edu.raf.rma.showtime.ui.watchlist.WatchlistScreen

const val MOVIE_ID = "movieId"

private object Routes {
    const val Movies = "movies"
    const val Favorite = "favorite"
    const val Watchlist = "watchlist"
    const val Quiz = "quiz"
    const val Profile = "profile"
    const val Detail = "movie/{$MOVIE_ID}"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.Movies, "Movies", Icons.Default.Home),
    BottomDestination(Routes.Favorite, "Favorite", Icons.Default.Favorite),
    BottomDestination(Routes.Watchlist, "Watchlist", Icons.Default.Bookmark),
    BottomDestination(Routes.Quiz, "Quiz", Icons.Default.PlayArrow),
    BottomDestination(Routes.Profile, "Profile", Icons.Default.Person),
)

@Composable
fun ShowtimeNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.Detail

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navController.navigateBottom(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Movies,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Movies) {
                MoviesScreen(onMovieClick = { navController.navigateToMovie(it) })
            }
            composable(Routes.Favorite) {
                FavoriteScreen(onMovieClick = { navController.navigateToMovie(it) })
            }
            composable(Routes.Watchlist) {
                WatchlistScreen(onMovieClick = { navController.navigateToMovie(it) })
            }
            composable(Routes.Quiz) {
                QuizScreen(onExitQuiz = { navController.navigateBottom(Routes.Movies) })
            }
            composable(Routes.Profile) {
                ProfileScreen()
            }
            composable(
                route = Routes.Detail,
                arguments = listOf(navArgument(MOVIE_ID) { type = NavType.StringType }),
            ) {
                MovieDetailScreen(onBack = { navController.navigateUp() })
            }
        }
    }
}

private fun NavController.navigateBottom(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavController.navigateToMovie(movieId: String) {
    navigate("movie/$movieId")
}

package ee.ut.cs.HEALTH.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.domain.model.remote.RetrofitInstance
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutine
import ee.ut.cs.HEALTH.ui.screens.*
import ee.ut.cs.HEALTH.viewmodel.*
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineDao


@Composable
fun DarkModeTopBar(
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Icon indicating the current mode
        Icon(
            imageVector = if (darkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
            contentDescription = if (darkMode) "Dark Mode" else "Light Mode",
            modifier = Modifier.padding(end = 8.dp)
        )

        // Switch to toggle dark mode
        Switch(
            checked = darkMode,
            onCheckedChange = { isChecked ->
                onToggleDarkMode(isChecked)
            }
        )
    }
}



@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: NavDestination,
    modifier: Modifier = Modifier,
    dao: RoutineDao,
    profileDao: ProfileDao,
    repository: RoutineRepository,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier.fillMaxSize()
    ) {
        /**
         * This loop handles all static destinations that do not require arguments.
         * These are the screens accessible from the bottom navigation bar.
         */
        NavDestination.entries.forEach { destination ->
            // Process only routes that are static (do not contain arguments).
            if (!destination.route.contains("{")) {
                composable(destination.route) {
                    when (destination) {
                        NavDestination.HOME -> {
                            val homeViewModel: HomeViewModel = viewModel(
                                factory = HomeViewModelFactory(repository)
                            )
                            HomeScreen(viewModel = homeViewModel, darkMode = darkMode,
                                onToggleDarkMode = onToggleDarkMode)
                        }
                        NavDestination.SEARCH -> {
                            val viewModel: SearchViewModel = viewModel(
                                factory = SearchViewModelFactory(repository)
                            )
                            SearchScreen(viewModel = viewModel, navController = navController, darkMode = darkMode,
                                onToggleDarkMode = onToggleDarkMode)
                        }
                        NavDestination.ADD -> {
                            val viewModel: AddRoutineViewModel = viewModel(
                                factory = AddRoutineViewModelFactory(
                                    repository = repository,
                                    initial = NewRoutine(
                                        name = "",
                                        description = null,
                                        routineItems = emptyList()
                                    )
                                )
                            )
                            AddRoutineScreen(viewModel = viewModel, navController = navController, darkMode = darkMode,
                                onToggleDarkMode = onToggleDarkMode)
                        }
                        NavDestination.STATS -> {
                            val statsViewModel: StatsViewModel = viewModel(
                                factory = StatsViewModelFactory(repository)
                            )
                            StatsScreen(viewModel = statsViewModel, darkMode = darkMode,
                                onToggleDarkMode = onToggleDarkMode)
                        }
                        NavDestination.PROFILE -> {
                            val profile by profileDao.getProfile().collectAsState(initial = null)
                            if (profile?.userHasSetTheirInfo == true) {
                                ProfileScreen(
                                    profileDao = profileDao,
                                    navController = navController,
                                    darkMode = darkMode,
                                    onToggleDarkMode = onToggleDarkMode
                                )
                            } else {
                                EditProfileScreen(
                                    profileDao = profileDao,
                                    navController = navController,
                                    darkMode = darkMode,
                                    onToggleDarkMode = onToggleDarkMode
                                )
                            }
                        }
                        NavDestination.EDITPROFILE -> EditProfileScreen(
                            profileDao = profileDao,
                            navController = navController,
                            darkMode = darkMode,
                            onToggleDarkMode = onToggleDarkMode
                        )
                        else -> {
                            // This branch is intentionally left empty. Dynamic routes are handled outside this loop.
                        }
                    }
                }
            }
        }

        /**
         * This block handles the dynamic route for the Exercise Detail screen.
         * It is defined separately to correctly process the 'exerciseName' argument.
         */
        composable(
            route = NavDestination.EXERCISE_DETAIL.route,
            arguments = listOf(navArgument("exerciseName") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName")

            if (exerciseName != null) {
                val detailViewModel: ExerciseDetailViewModel = viewModel(
                    key = exerciseName,
                    factory = ExerciseDetailViewModelFactory(
                        exerciseName = exerciseName,
                        // Use the centralized Retrofit instance, which is the correct approach.
                        exerciseApi = RetrofitInstance.api
                    )
                )
                ExerciseDetailScreen(viewModel = detailViewModel)
            }
        }
    }
}

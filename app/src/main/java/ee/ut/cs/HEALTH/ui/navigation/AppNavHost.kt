package ee.ut.cs.HEALTH.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: NavDestination,
    modifier: Modifier = Modifier,
    dao: RoutineDao,
    profileDao: ProfileDao,
    repository: RoutineRepository
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
                        NavDestination.HOME -> HomeScreen(dao = dao)
                        NavDestination.SEARCH -> {
                            val viewModel: SearchViewModel = viewModel(
                                factory = SearchViewModelFactory(repository)
                            )
                            SearchScreen(viewModel = viewModel, navController = navController)
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
                            AddRoutineScreen(viewModel = viewModel)
                        }
                        NavDestination.STATS -> StatsScreen(dao = dao)
                        NavDestination.PROFILE -> {
                            val profile by profileDao.getProfile().collectAsState(initial = null)
                            if (profile?.userHasSetTheirInfo == true) {
                                ProfileScreen(
                                    profileDao = profileDao,
                                    navController = navController
                                )
                            } else {
                                EditProfileScreen(
                                    profileDao = profileDao,
                                    navController = navController
                                )
                            }
                        }
                        NavDestination.EDITPROFILE -> EditProfileScreen(
                            profileDao = profileDao,
                            navController = navController
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

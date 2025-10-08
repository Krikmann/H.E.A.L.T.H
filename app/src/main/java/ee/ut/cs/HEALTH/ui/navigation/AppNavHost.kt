package ee.ut.cs.HEALTH.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutine
import ee.ut.cs.HEALTH.ui.screens.AddRoutineScreen
import ee.ut.cs.HEALTH.ui.screens.ProfileScreen
import ee.ut.cs.HEALTH.ui.screens.EditProfileScreen
import ee.ut.cs.HEALTH.ui.screens.SearchScreen
import ee.ut.cs.HEALTH.ui.screens.HomeScreen
import ee.ut.cs.HEALTH.ui.screens.StatsScreen
import ee.ut.cs.HEALTH.viewmodel.AddRoutineViewModel
import ee.ut.cs.HEALTH.viewmodel.AddRoutineViewModelFactory

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
        navController,
        startDestination = startDestination.route,
        modifier = modifier.fillMaxSize()
    ) {
        NavDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    NavDestination.HOME -> HomeScreen(dao = dao)
                    NavDestination.SEARCH -> SearchScreen(repository = repository)
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

                        AddRoutineScreen(
                            viewModel = viewModel,
                        )
                    }
                    NavDestination.STATS -> StatsScreen(dao = dao)
                    NavDestination.PROFILE -> {
                        val profile by profileDao.getProfile().collectAsState(initial = null)

                        if (profile?.userHasSetTheirInfo == true) ProfileScreen(
                            profileDao = profileDao,
                            navController = navController
                        )
                        else EditProfileScreen(
                            profileDao = profileDao,
                            navController = navController
                        )    // If user has not set their info
                    }
                    NavDestination.EDITPROFILE -> EditProfileScreen(
                        profileDao = profileDao,
                        navController = navController
                    )
                    //add more screens
                }
            }
        }
    }
}

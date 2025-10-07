package ee.ut.cs.HEALTH.ui.navigation


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.domain.model.Profile
import ee.ut.cs.HEALTH.ui.components.AddScreen
import ee.ut.cs.HEALTH.ui.components.HomeScreen
import ee.ut.cs.HEALTH.ui.screens.ProfileScreen
import ee.ut.cs.HEALTH.ui.screens.EditProfileScreen
import ee.ut.cs.HEALTH.ui.screens.SearchScreen
import ee.ut.cs.HEALTH.ui.components.StatsScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: NavDestination,
    modifier: Modifier = Modifier,
    dao: RoutineDao,
    profileDao: ProfileDao
) {
    NavHost(
        navController,
        startDestination = startDestination.route,
        modifier = modifier.fillMaxSize()
    ) {
        NavDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    NavDestination.HOME -> HomeScreen()
                    NavDestination.SEARCH -> SearchScreen(dao = dao)
                    NavDestination.ADD -> AddScreen()
                    NavDestination.STATS -> StatsScreen()
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

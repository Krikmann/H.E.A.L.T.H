package ee.ut.cs.HEALTH.ui.navigation


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.ui.components.AddScreen
import ee.ut.cs.HEALTH.ui.components.HomeScreen
import ee.ut.cs.HEALTH.ui.screens.ProfileScreen
import ee.ut.cs.HEALTH.ui.screens.SearchScreen
import ee.ut.cs.HEALTH.ui.components.StatsScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: NavDestination,
    modifier: Modifier = Modifier,
    dao: RoutineDao
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
                    NavDestination.PROFILE -> ProfileScreen()   // changed the import
                    //add more screens
                }
            }
        }
    }
}

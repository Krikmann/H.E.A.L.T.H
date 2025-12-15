package ee.ut.cs.HEALTH.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.ui.navigation.NavDestination
import ee.ut.cs.HEALTH.ui.navigation.AppNavHost
import ee.ut.cs.HEALTH.ui.navigation.DarkModeTopBar

/**
 * The main UI structure of the application, providing a scaffold with a top bar and a bottom navigation bar.
 *
 * This composable function sets up the primary navigation for the app. It initializes a [Scaffold]
 * that hosts the [AppNavHost] for displaying different screens. The bottom [NavigationBar]
 * allows the user to switch between the main destinations defined in [NavDestination].
 * It also includes the [DarkModeTopBar] for theme switching.
 *
 * @param modifier A [Modifier] for this composable.
 * @param dao The Data Access Object for routine data.
 * @param profileDao The Data Access Object for profile data.
 * @param repository The repository that provides a single source of truth for all app data.
 * @param darkMode A boolean indicating if dark mode is currently enabled.
 * @param onToggleDarkMode A lambda function to be invoked when the user toggles the dark mode switch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationBar(
    modifier: Modifier = Modifier,
    dao: RoutineDao,
    profileDao: ProfileDao,
    repository: RoutineRepository,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val startDestination = NavDestination.HOME

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(modifier = Modifier.padding(top = 12.dp)) {
                DarkModeTopBar(
                    darkMode = darkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomBarDestinations = NavDestination.entries.filter {
                    it != NavDestination.EDITPROFILE && it != NavDestination.EXERCISE_DETAIL
                }

                bottomBarDestinations.forEach { navItem ->
                    NavigationBarItem(
                        selected = currentDestination?.route?.startsWith(navItem.route.substringBefore('?')) ?: false,
                        onClick = {
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            navItem.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = navItem.contentDescription
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            dao = dao,
            profileDao = profileDao,
            repository = repository,
            darkMode = darkMode,
            onToggleDarkMode = onToggleDarkMode
        )
    }
}

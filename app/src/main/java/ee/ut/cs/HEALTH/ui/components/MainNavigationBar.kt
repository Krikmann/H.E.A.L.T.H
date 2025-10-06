package ee.ut.cs.HEALTH.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.ui.navigation.NavDestination
import ee.ut.cs.HEALTH.ui.navigation.AppNavHost

@Composable
fun MainNavigationBar(modifier: Modifier = Modifier, dao: RoutineDao) {
    val navController = rememberNavController()
    val startDestination = NavDestination.HOME
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavDestination.entries.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            selectedDestination = index
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.startDestinationRoute ?: navItem.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.contentDescription
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            dao = dao
        )
    }
}

//for testing only, replace with actual screens later

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("HomeScreen")
    }
}

@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("SearchScreen")
    }
}

@Composable
fun AddScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("AddScreen")
    }
}
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("StatsScreen")
    }
}
package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ee.ut.cs.HEALTH.ui.components.RoutineInfoCard
import ee.ut.cs.HEALTH.viewmodel.StatsViewModel
import ee.ut.cs.HEALTH.ui.navigation.NavDestination

/**
 * A screen that displays the user's entire workout history, grouped by date.
 *
 * This composable function fetches workout history from the [StatsViewModel] and renders it
 * as a scrollable list. If no history is available, it displays a placeholder message.
 * Each history item is clickable, navigating the user to the corresponding routine's
 * detail view.
 *
 * The `@OptIn(ExperimentalMaterial3Api::class)` annotation is used because this screen
 * utilizes components that might still be experimental in Material3.
 *
 * @param viewModel The [StatsViewModel] instance that provides the workout history data.
 * @param navController The [NavController] used for handling navigation events, such as
 *                      clicking on a history item to view its details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    navController: NavController
) {
    val historyByDate by viewModel.completedRoutinesByDate.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (historyByDate.isEmpty()) {
                Text(
                    text = "No routines completed yet. Finish one to see your stats!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Your Workout History",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    historyByDate.forEach { (date, itemsOnDate) ->
                        item {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(itemsOnDate) { historyItem ->
                            RoutineInfoCard(
                                title = historyItem.name,
                                description = historyItem.completionNote,
                                completionCount = null,
                                onClick = {
                                    val route = NavDestination.SEARCH.route.replace(
                                        "{routineId}",
                                        historyItem.routineId.toString()
                                    )
                                    navController.navigate(route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

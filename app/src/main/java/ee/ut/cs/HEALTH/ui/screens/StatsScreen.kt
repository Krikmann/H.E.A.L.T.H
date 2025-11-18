package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import ee.ut.cs.HEALTH.ui.navigation.DarkModeTopBar
import ee.ut.cs.HEALTH.ui.navigation.NavDestination
import ee.ut.cs.HEALTH.viewmodel.StatsViewModel

/**
 * The StatsScreen composable is annotated with OptIn for ExperimentalMaterial3Api
 * because it uses the Card component with an onClick listener, which is an
 * experimental feature in Material3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    /**
     * NavController is passed to allow navigation from this screen.
     * For example, clicking on a history item will navigate to that routine's detail view.
     */
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
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    historyByDate.forEach { (date, itemsOnDate) ->

                        item {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(itemsOnDate) { historyItem ->
                            /**
                             * This Card is made clickable to enhance user experience.
                             * When a user clicks on a routine in their history,
                             * it navigates them to the SearchScreen, pre-opening
                             * that specific routine for them to view or start again.
                             */
                            Card(
                                onClick = {
                                    val route = NavDestination.SEARCH.route.replace(
                                        "{routineId}",
                                        historyItem.routineId.toString()
                                    )
                                    navController.navigate(route)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .padding(16.dp)
                                ) {
                                    Text(historyItem.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Loo või asenda see fail: ee/ut/cs/HEALTH/ui/screens/StatsScreen.kt
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ee.ut.cs.HEALTH.viewmodel.StatsViewModel

/**
 * A composable function that displays the user's workout history.
 * It collects data from the [StatsViewModel], which provides a map of completed
 * routines grouped by date.
 *
 * @param viewModel The [StatsViewModel] instance that provides the state for this screen.
 */
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val historyByDate by viewModel.completedRoutinesByDate.collectAsStateWithLifecycle()

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
                    // For each routine completed on this date, create a Card item.
                    items(itemsOnDate) { historyItem ->
                        Card(
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

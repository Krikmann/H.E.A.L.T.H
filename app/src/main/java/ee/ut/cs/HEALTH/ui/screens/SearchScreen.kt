package ee.ut.cs.HEALTH.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.viewmodel.SearchViewModel

/**
 * A stateful composable that orchestrates the search screen's UI.
 * It observes state from the [SearchViewModel] and decides whether to show
 * the search list or the routine detail view.
 *
 * @param viewModel The [SearchViewModel] that provides state and handles business logic.
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavHostController
) {
    // Collect state from the ViewModel in a lifecycle-aware manner.
    val query by viewModel.query.collectAsStateWithLifecycle()
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val selectedRoutine by viewModel.selectedRoutine.collectAsStateWithLifecycle()

    // Conditionally display either the search list or the detail view
    // based on whether a routine has been selected.
    if (selectedId == null) {
        SearchListView(
            query = query,
            summaries = summaries,
            onQueryChange = viewModel::onQueryChange, // Delegate event to ViewModel
            onRoutineClick = viewModel::onRoutineSelect   // Delegate event to ViewModel
        )
    } else {
        // When a routine is selected, handle the system back press to clear the selection.
        BackHandler {
            viewModel.onClearSelection()
        }
        RoutineDetailView(
            routine = selectedRoutine,
            onClose = viewModel::onClearSelection,
            navController = navController

        )
    }
}

/**
 * A stateless composable for displaying the search input field and the list of results.
 */
@Composable
private fun SearchListView(
    query: String,
    summaries: List<ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary>,
    onQueryChange: (String) -> Unit,
    onRoutineClick: (Long) -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)) {
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)) {
            items(summaries, key = { it.id.value }) { routine ->
                Card(
                    onClick = { onRoutineClick(routine.id.value) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Text(routine.name)
                    }
                }
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Search routines") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true
        )
    }
}

/**
 * A stateless composable for displaying the details of a selected routine.
 */
@Composable
private fun RoutineDetailView(
    routine: ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine?,
    onClose: () -> Unit,
    navController: NavHostController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }

        // Show routine details if available, otherwise show a loading indicator.
        routine?.let { r ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 72.dp)) {
                Text(r.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                Text(r.description.orEmpty(), fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))

                LazyColumn {
                    items(r.routineItems) { item ->
                        // Display different information based on the type of routine item.
                        when (item) {
                            is SavedExerciseByReps -> {
                                Column {
                                    Text(
                                        text = "Name: ${item.exerciseDefinition.name}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            navController.navigate("exercise_detail/${item.exerciseDefinition.name}")
                                        }
                                    )
                                    Text("Sets: ${item.amountOfSets}")
                                    Text("Rest: ${item.recommendedRestDurationBetweenSets.inWholeSeconds}s")
                                    Text("Reps: ${item.countOfRepetitions}")
                                }
                            }
                            is SavedExerciseByDuration -> {
                                Text(
                                    text = "Name: ${item.exerciseDefinition.name}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        navController.navigate("exercise_detail/${item.exerciseDefinition.name}")
                                    }
                                )
                                Text("Sets: ${item.amountOfSets}")
                                Text("Rest: ${item.recommendedRestDurationBetweenSets.inWholeSeconds}s")
                                Text("Duration: ${item.duration.inWholeSeconds}s")
                            }



                            is SavedRestDurationBetweenExercises -> {
                                Text("Rest for ${item.restDuration.inWholeSeconds}s")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…")
            }
        }
    }
}

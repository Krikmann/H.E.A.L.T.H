package ee.ut.cs.HEALTH.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ee.ut.cs.HEALTH.R
import ee.ut.cs.HEALTH.data.local.dto.ExerciseDetailDto

import ee.ut.cs.HEALTH.domain.model.routine.*
import ee.ut.cs.HEALTH.viewmodel.SearchViewModel
import kotlinx.coroutines.delay

/**
 * A stateful composable that orchestrates the search screen's UI.
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
    // *** THIS IS THE NEW DATA SOURCE FOR THE DETAIL VIEW ***
    val enrichedItems by viewModel.enrichedRoutineItems.collectAsStateWithLifecycle()

    // Conditionally display either the search list or the detail view
    if (selectedId == null) {
        SearchListView(
            query = query,
            summaries = summaries,
            onQueryChange = viewModel::onQueryChange,
            onRoutineClick = viewModel::onRoutineSelect
        )
    } else {
        // When a routine is selected, handle the system back press to clear the selection.
        BackHandler {
            viewModel.onClearSelection()
        }
        // *** PASS THE NEW `enrichedItems` LIST TO THE DETAIL VIEW ***
        RoutineDetailView(
            items = enrichedItems,
            onClose = viewModel::onClearSelection,
            navController = navController
        )
    }
}

/**
 * A stateless composable for displaying the search input field and the list of results.
 * (This function does not need any changes)
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
 * This view now receives a list of `EnrichedRoutineItem` directly from the ViewModel.
 *
 * @param items The pre-prepared list of enriched routine steps.
 * @param onClose A callback function to close the detail view.
 * @param navController The navigation controller.
 */
@Composable
private fun RoutineDetailView(
    items: List<EnrichedRoutineItem>, // <-- CHANGED: Parameter is now the enriched list
    onClose: () -> Unit,
    navController: NavHostController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-right close button.
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }

        // Show a loading indicator if the items are not yet ready.
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading routine...")
            }
            return
        }

        // State to keep track of the current step in the routine.
        var currentIndex by remember { mutableIntStateOf(0) }
        val currentEnrichedItem = items[currentIndex]
        val currentItem = currentEnrichedItem.routineItem // This is SavedExercise or SavedRest
        val details = currentEnrichedItem.details      // This is ExerciseDetailDto (or null for rests)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section for content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                // Decide which view to show based on the item's type.
                when (currentItem) {
                    is SavedExercise -> ExerciseView(item = currentItem, details = details)
                    is SavedRestDurationBetweenExercises -> RestView(item = currentItem)
                    else -> {
                        // This branch will handle NewExercise, UpdatedExercise, and any other types.
                        // For a read-only view, we can show a generic placeholder or simply nothing.
                        // This makes the 'when' block exhaustive and prevents future crashes.
                        Text(
                            "Unsupported item type: ${currentItem::class.simpleName}",
                            modifier = Modifier.padding(top = 60.dp, bottom = 16.dp)
                        )
                    }
                }

                // Determine the timer duration based on the current item's type.
                val timerDuration = when (currentItem) {
                    is SavedExerciseByDuration -> currentItem.duration.inWholeSeconds
                    is SavedRestDurationBetweenExercises -> currentItem.restDuration.inWholeSeconds
                    else -> null
                }

                timerDuration?.let { duration ->
                    Timer(
                        time = duration,
                        currentIndex = currentIndex,
                        onTimerFinished = {
                            if (currentIndex < items.lastIndex) currentIndex++ else onClose()
                        }
                    )
                }
            }

            // Bottom section for navigation controls.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Step ${currentIndex + 1} of ${items.size}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { if (currentIndex > 0) currentIndex-- }, enabled = currentIndex > 0) { Text("Previous") }
                    Button(onClick = { if (currentIndex < items.lastIndex) currentIndex++ else onClose() }) { Text("Next") }
                }
            }
        }
    }
}

/**
 * A helper composable for displaying an exercise.
 * It now accepts the raw `SavedExercise` and the nullable `ExerciseDetailDto`.
 *
 * @param item The [SavedExercise] object from the database.
 * @param details The [ExerciseDetailDto] object from the API (can be null).
 */
@Composable
private fun ExerciseView(item: SavedExercise, details: ExerciseDetailDto?) {
    // The title is determined from the database data.
    val title = when (item) {
        is SavedExerciseByReps -> "${item.exerciseDefinition.name} ${item.countOfRepetitions} times"
        is SavedExerciseByDuration -> "${item.exerciseDefinition.name} for ${item.duration.inWholeSeconds} seconds"
    }
    Text(
        text = title,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 60.dp, bottom = 16.dp),
    )

    // *** THIS IS THE CRITICAL CHANGE ***
    // Image and instructions are now taken from the `details` (DTO) object.
    // If details are null (e.g., network error), nothing is shown.
    details?.let { dto -> // <--- This is the one and only check we need.

        // Display the image using the `imageUrl` from the DTO.
        dto.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = dto.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(vertical = 16.dp),
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground)
            )
        }

        // Display the instructions from the DTO.
        if (!dto.instructions.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Instructions:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            // Kuna me oleme nüüd kindlad, et see pole null, saame .forEach'i ohutult kasutada.
            dto.instructions.forEach { line ->
                Text(
                    text = "• $line",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * A helper composable for displaying a rest period.
 * (This function does not need any changes)
 */
@Composable
private fun RestView(item: SavedRestDurationBetweenExercises) {
    // ... (content is the same as before)
    Text(
        text = "Rest",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 60.dp, bottom = 16.dp),
    )
    Icon(
        imageVector = Icons.Default.Timer,
        contentDescription = "Rest",
        modifier = Modifier
            .size(150.dp)
            .padding(vertical = 16.dp)
    )
    Text(
        text = "${item.restDuration.inWholeSeconds} seconds",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
    )
}


/**
 * A simple timer composable.
 * (This function does not need any changes)
 */
@Composable
fun Timer(time: Long, currentIndex: Int, onTimerFinished: () -> Unit) {
    // ... (content is the same as before)
    var currentTime by remember { mutableStateOf(time) }

    LaunchedEffect(key1 = currentIndex) {
        currentTime = time
    }

    LaunchedEffect(key1 = currentTime) {
        if (currentTime > 0) {
            delay(1000L)
            currentTime--
        } else {
            onTimerFinished()
        }
    }

    Text(
        text = "$currentTime",
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

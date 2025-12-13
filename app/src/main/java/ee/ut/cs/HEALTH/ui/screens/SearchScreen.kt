package ee.ut.cs.HEALTH.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ee.ut.cs.HEALTH.domain.model.remote.RetrofitInstance
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary
import ee.ut.cs.HEALTH.ui.navigation.NavDestination
import ee.ut.cs.HEALTH.viewmodel.SearchViewModel
import ee.ut.cs.HEALTH.R
import kotlinx.coroutines.delay

// --- Components for Timer ---
@Composable
fun Timer(
    time: Long,
    currentIndex: Int,
    onTimerFinished: () -> Unit
) {
    var currentTime by remember(currentIndex) { mutableStateOf(time) }

    LaunchedEffect(key1 = currentTime, key2 = currentIndex) {
        if (currentTime > 0) {
            delay(1000L)
            currentTime -= 1L
        } else {
            onTimerFinished()
        }
    }
    Text(
        text = formatTime(currentTime),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold
    )
}

private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

// --- Component for Exercise Image ---
@Composable
fun ExerciseImageFromApi(exerciseName: String, modifier: Modifier = Modifier) {

    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(exerciseName) {
        isLoading = true
        try {
            val response = RetrofitInstance.api.searchExercisesByName(exerciseName)
            if (response.isSuccessful) {
                imageUrl = response.body()?.exercises?.firstOrNull()?.imageUrl
            }
        } catch (e: Exception) {
            android.util.Log.e("ExerciseImageFromApi", "Failed to load image for $exerciseName", e)
        }
        isLoading = false
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image for $exerciseName",
                modifier = Modifier
                    .fillMaxSize()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.default_profile_pic),
                error = androidx.compose.ui.res.painterResource(id = R.drawable.default_profile_pic)
            )
        }
    }
}


//======================================================================================
//  PRIMARY COMPOSABLE: SearchScreen
//======================================================================================
/**
 * The main stateful composable for the Search feature.
 *
 * This screen acts as a controller, observing state from the [SearchViewModel]
 * and determining which of the three possible views to display:
 * 1. [SearchListView]: The default view for searching and browsing routines.
 * 2. [RoutinePreview]: Shown when a user clicks on a routine to see its details.
 * 3. [WorkoutView]: The active workout session, shown after the user starts a routine.
 *
 * It also handles back-press events to provide intuitive navigation between these states.
 *
 * @param viewModel The [SearchViewModel] that holds the business logic and state.
 * @param navController The [NavHostController] for navigating to other destinations.
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavHostController,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    // Collect state from the ViewModel in a lifecycle-aware manner.
    val query by viewModel.query.collectAsStateWithLifecycle()
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    val selectedRoutine by viewModel.selectedRoutine.collectAsStateWithLifecycle()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsStateWithLifecycle()

    // Listen for one-time navigation events, e.g., after finishing a workout.
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            navController.navigate(NavDestination.STATS.route) {
                popUpTo(NavDestination.HOME.route)
            }
        }
    }

    // This Column orchestrates which view is currently visible.
    Column(modifier = Modifier.fillMaxSize()) {
        when {
            // Case 1: No routine selected -> Show the search list.
            selectedRoutine == null -> {
                SearchListView(
                    query = query,
                    summaries = summaries,
                    onQueryChange = viewModel::onQueryChange,
                    onRoutineClick = viewModel::onRoutineSelect
                )
            }
            // Case 3: Routine is selected AND workout is active -> Show the workout session.
            isWorkoutActive -> {
                BackHandler { viewModel.stopWorkout() } // Back press stops workout, returns to preview
                WorkoutView(
                    routine = selectedRoutine,
                    onClose = viewModel::stopWorkout,
                    onFinish = viewModel::onRoutineFinish,
                    navController = navController
                )
            }
            // Case 2: Routine is selected but workout not active -> Show the preview.
            else -> {
                BackHandler { viewModel.onClearSelection() } // Back press clears selection, returns to list
                RoutinePreview(
                    routine = selectedRoutine!!, // Non-null asserted because of the when-condition
                    onClose = viewModel::onClearSelection,
                    onStartWorkout = viewModel::startWorkout
                )
            }
        }
    }
}


//======================================================================================
//  VIEW 1: SearchListView
//======================================================================================
/**
 * A stateless composable that displays the routine search bar and the list of results.
 *
 * @param query The current text in the search field.
 * @param summaries The list of [RoutineSummary] objects to display.
 * @param onQueryChange Callback invoked when the search query changes.
 * @param onRoutineClick Callback invoked when a user clicks on a routine card.
 */
@Composable
private fun SearchListView(
    query: String,
    summaries: List<RoutineSummary>,
    onQueryChange: (String) -> Unit,
    onRoutineClick: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // The list of routine cards, with padding at the bottom to avoid overlapping the search bar.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            items(summaries, key = { it.id.value }) { routine ->
                Card(
                    onClick = { onRoutineClick(routine.id.value) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        // Display completion count in the top-right corner.
                        Row(
                            modifier = Modifier.align(Alignment.TopEnd),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (routine.completionCount > 0) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Completion count",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = routine.completionCount.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
        // The search text field, aligned to the bottom of the screen.
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


//======================================================================================
//  VIEW 2: RoutinePreview
//======================================================================================
/**
 * A stateless composable that shows a detailed preview of a selected routine.
 * It displays the routine's description, a list of its exercises, and a prominent
 * "Start Workout" button.
 *
 * @param routine The full [SavedRoutine] object to display.
 * @param onClose Callback to close the preview and return to the search list.
 * @param onStartWorkout Callback to transition from preview to the active [WorkoutView].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutinePreview(
    routine: ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine,
    onClose: () -> Unit,
    onStartWorkout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routine.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close preview")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Start Workout") },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Start workout") },
                onClick = onStartWorkout
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Display routine description if it exists.
            if (!routine.description.isNullOrBlank()) {
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            HorizontalDivider()

            // Display a numbered list of all items (exercises and rests) in the routine.
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(routine.routineItems) { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Icon(
                            imageVector = when (item) {
                                is SavedExerciseByReps -> Icons.Default.Repeat
                                is SavedExerciseByDuration -> Icons.Default.Timer
                                is SavedRestDurationBetweenExercises -> Icons.Default.Timer
                            },
                            contentDescription = "Item type",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (item) {
                                is SavedExerciseByReps -> "${item.exerciseDefinition.name} (${item.countOfRepetitions} reps)"
                                is SavedExerciseByDuration -> "${item.exerciseDefinition.name} (${item.duration.inWholeSeconds}s)"
                                is SavedRestDurationBetweenExercises -> "Rest (${item.restDuration.inWholeSeconds}s)"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}


//======================================================================================
//  VIEW 3: WorkoutView
//======================================================================================
/**
 * A stateless composable for the active workout session. This is the "player" view.
 * It displays the current exercise or rest, provides a timer if applicable, and includes
 * navigation controls ("Back", "Next", "Finish").
 * The logic for constructing the sequence of exercises and rests is based on the original
 * implementation using a list of Triples.
 *
 * @param routine The full [SavedRoutine] object being performed.
 * @param onClose Callback to stop the workout and return to the preview.
 * @param onFinish Callback to mark the routine as complete and navigate away.
 * @param navController The [NavHostController] to navigate to the exercise detail screen.
 */
@Composable
private fun WorkoutView(
    routine: ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine?,
    onClose: () -> Unit,
    onFinish: () -> Unit,
    navController: NavHostController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close workout")
        }

        // Show routine details only if they are loaded.
        routine?.let { r ->
            // --- THIS SECTION RECREATES THE ORIGINAL LOGIC FOR THE WORKOUT SEQUENCE ---
            val items = remember(r.routineItems) {
                mutableListOf<Triple<String, Long?, String?>>().apply {
                    for (item in r.routineItems) {
                        when (item) {
                            is SavedExerciseByReps -> {
                                for (i in 1..item.amountOfSets) {
                                    add(Triple("${item.exerciseDefinition.name} ${item.countOfRepetitions} times", null, item.exerciseDefinition.name))
                                    if (item.recommendedRestDurationBetweenSets.inWholeSeconds > 0)
                                        add(Triple("Rest for ${item.recommendedRestDurationBetweenSets.inWholeSeconds} seconds", item.recommendedRestDurationBetweenSets.inWholeSeconds, null))
                                }
                            }
                            is SavedExerciseByDuration -> {
                                for (i in 1..item.amountOfSets) {
                                    add(Triple("${item.exerciseDefinition.name} for ${item.duration.inWholeSeconds} seconds", item.duration.inWholeSeconds, item.exerciseDefinition.name))
                                    if (item.recommendedRestDurationBetweenSets.inWholeSeconds > 0)
                                        add(Triple("Rest for ${item.recommendedRestDurationBetweenSets.inWholeSeconds} seconds", item.recommendedRestDurationBetweenSets.inWholeSeconds, null))
                                }
                            }
                            is SavedRestDurationBetweenExercises -> {
                                if (item.restDuration.inWholeSeconds > 0)
                                    add(Triple("Rest for ${item.restDuration.inWholeSeconds}s", item.restDuration.inWholeSeconds, null))
                            }
                        }
                    }
                }
            }
            // --- END OF ORIGINAL SEQUENCE LOGIC ---

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("This routine is empty.")
                }
                return@let
            }

            var currentIndex by remember { mutableIntStateOf(0) }
            val currentItem = items[currentIndex]
            val currentExerciseName = currentItem.third

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    // Add padding to not be under the close button
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Top part: Exercise/Rest title and image.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f) // Allow this section to take available space
                        .clickable(enabled = currentExerciseName != null) {
                            currentExerciseName?.let {
                                navController.navigate(
                                    NavDestination.EXERCISE_DETAIL.route.replace("{exerciseName}", it)
                                )
                            }
                        }
                ) {
                    Text(
                        text = currentItem.first,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (currentExerciseName != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                    Spacer(Modifier.height(16.dp))

                    if (currentExerciseName != null) {
                        // ** 1. PARANDUS: KASUTAME UUESTI ExerciseImageFromApi FUNKTSIOONI **
                        ExerciseImageFromApi(
                            exerciseName = currentExerciseName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }
                }

                // Middle part: Timer, only shown for items with a duration.
                currentItem.second?.let { duration ->
                    Box(
                        modifier = Modifier.wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Timer(
                                    time = duration,
                                    currentIndex = currentIndex,
                                    onTimerFinished = {
                                        if (currentIndex < items.lastIndex) currentIndex++ else onFinish()
                                    }
                                )
                            }
                        }
                    }
                }

                // ** 2. PARANDUS: LISAME TAGASI HARJUTUSTE LOENDURI **
                Text(
                    text = "${currentIndex + 1} / ${items.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Bottom part: Navigation buttons.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { if (currentIndex > 0) currentIndex-- }, enabled = currentIndex > 0) {
                        Text("Back")
                    }
                    if (currentIndex < items.lastIndex) {
                        Button(onClick = { currentIndex++ }) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Finish workout")
                            Spacer(Modifier.width(8.dp))
                            Text("Finish")
                        }
                    }
                }
            }
        }
    }
}

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

/**
 * A composable that displays a countdown timer.
 * The timer is automatically restarted when the `currentIndex` changes.
 *
 * @param time The initial time in seconds from which to count down.
 * @param currentIndex A key that, when changed, causes the timer to reset.
 * @param onTimerFinished A callback function that is invoked when the timer reaches zero.
 */
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

/**
 * Formats a duration in seconds into a "MM:SS" string format.
 *
 * @param seconds The total number of seconds to format.
 * @return A string representation of the time, e.g., "01:30".
 */
private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

/**
 * A composable that fetches and displays an exercise image from a remote API using its ID.
 * It handles loading and error states internally.
 *
 * @param exerciseId The unique identifier of the exercise to fetch the image for.
 * @param modifier The modifier to be applied to this composable.
 */
@Composable
fun ExerciseImageFromApi(exerciseId: String, modifier: Modifier = Modifier) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    /**
     * Fetches the exercise details when the exerciseId changes. The coroutine is launched
     * only when the key `exerciseId` is updated.
     */
    LaunchedEffect(exerciseId) {
        isLoading = true
        try {
            /** Makes an API call to get the exercise data by its unique ID. */
            val response = RetrofitInstance.api.getExercisesById(exerciseId)
            if (response.isSuccessful) {
                /** Extracts the image URL from the 'data' object of the response body. */
                imageUrl = response.body()?.data?.imageUrl
            }
        } catch (e: Exception) {
            android.util.Log.e("ExerciseImageFromApi", "Failed to load image for ID $exerciseId", e)
        }
        isLoading = false
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image for $exerciseId",
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


/**
 * The main stateful composable for the Search feature.
 *
 * This screen acts as a controller, observing state from the [SearchViewModel]
 * and determining which of its three child views to display:
 * 1. [SearchListView]: The default view for searching and browsing routines.
 * 2. [RoutinePreview]: Shown when a user clicks on a routine to see its details.
 * 3. [WorkoutView]: The active workout session, shown after the user starts a routine.
 *
 * It also handles back-press events to provide intuitive navigation between these states.
 *
 * @param viewModel The [SearchViewModel] that holds the business logic and state.
 * @param navController The [NavHostController] for navigating to other destinations.
 * @param darkMode The current state of the dark mode theme.
 * @param onToggleDarkMode A callback to toggle the dark mode theme.
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    navController: NavHostController,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    /** Collect state from the ViewModel in a lifecycle-aware manner. */
    val query by viewModel.query.collectAsStateWithLifecycle()
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    val selectedRoutine by viewModel.selectedRoutine.collectAsStateWithLifecycle()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsStateWithLifecycle()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsStateWithLifecycle()

    /** Listen for one-time navigation events, e.g., after finishing a workout. */
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            navController.navigate(NavDestination.STATS.route) {
                popUpTo(NavDestination.HOME.route)
            }
        }
    }

    /** This Column orchestrates which view is currently visible based on the ViewModel's state. */
    Column(modifier = Modifier.fillMaxSize()) {
        when {
            /** Case 1: No routine has been selected, so show the default search list. */
            selectedRoutine == null -> {
                SearchListView(
                    query = query,
                    summaries = summaries,
                    onQueryChange = viewModel::onQueryChange,
                    onRoutineClick = viewModel::onRoutineSelect
                )
            }
            /** Case 2: A routine is selected and the workout is active, so show the workout session. */
            isWorkoutActive -> {
                BackHandler { viewModel.stopWorkout() } /** A back press stops the workout and returns to the preview. */
                WorkoutView(
                    routine = selectedRoutine,
                    workoutSteps = selectedRoutine?.routineItems ?: emptyList(),
                    currentExerciseIndex = currentExerciseIndex,
                    onExerciseChange = viewModel::onExerciseChange,
                    onClose = viewModel::stopWorkout,
                    onFinish = viewModel::onRoutineFinish,
                    navController = navController
                )
            }
            /** Case 3: A routine is selected but the workout is not active, so show the preview. */
            else -> {
                BackHandler { viewModel.onClearSelection() } /** A back press clears the selection and returns to the search list. */
                RoutinePreview(
                    routine = selectedRoutine!!, /** Non-null asserted because this case is only reachable if a routine is selected. */
                    onClose = viewModel::onClearSelection,
                    onStartWorkout = viewModel::startWorkout
                )
            }
        }
    }
}

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
        /** The list of routine cards, with padding at the bottom to avoid overlapping the search bar. */
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
                        /** Display the completion count in the top-right corner if it's greater than zero. */
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
        /** The search text field, aligned to the bottom of the screen for easy access. */
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
 * A stateless composable that shows a detailed preview of a selected routine.
 * It displays the routine's description, a list of its exercises, and a prominent
 * "Start Workout" button.
 *
 * @param routine The full [ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine] object to display.
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
                actions = {
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
            /** Display the routine's description if it is not null or blank. */
            if (!routine.description.isNullOrBlank()) {
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            HorizontalDivider()

            /** Display a numbered list of all items (exercises and rests) in the routine. */
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

/**
 * A sealed interface representing a single step within a workout session.
 * This provides a type-safe way to handle different kinds of workout items.
 */
private sealed interface WorkoutStep {
    /** Represents an exercise step with its details. */
    data class Exercise(
        val exerciseId: String,
        val exerciseName: String,
        val details: String,
        val durationSeconds: Long? = null
    ) : WorkoutStep

    /** Represents a rest step with its duration. */
    data class Rest(
        val details: String,
        val durationSeconds: Long
    ) : WorkoutStep
}

/**
 * A stateless composable for the active workout session, acting as the "player" view.
 * It displays the current exercise or rest period, provides a timer if applicable, and includes
 * navigation controls ("Previous", "Next", "Finish"). The sequence of steps is generated
 * dynamically to account for sets and rests.
 *
 * @param routine The full [ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine] object being performed. Can be null.
 * @param onClose Callback to stop the workout and return to the preview.
 * @param onFinish Callback to mark the routine as complete and navigate away.
 * @param navController The [NavHostController] to handle navigation to the exercise detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutView(
    routine: ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine?,
    workoutSteps: List<Any>,
    currentExerciseIndex: Int,
    onExerciseChange: (Int) -> Unit,
    onClose: () -> Unit,
    onFinish: () -> Unit,
    navController: NavHostController
) {
    if (routine == null) return

    val localWorkoutSteps = remember(routine.routineItems) {
        buildList {
            routine.routineItems.forEach { item ->
                when (item) {
                    is SavedExerciseByReps -> {
                        for (i in 1..item.amountOfSets) {
                            add(
                                WorkoutStep.Exercise(
                                    exerciseId = item.exerciseDefinition.id.value,
                                    exerciseName = item.exerciseDefinition.name,
                                    details = "${item.countOfRepetitions} reps"
                                )
                            )
                            if (i < item.amountOfSets && item.recommendedRestDurationBetweenSets.inWholeSeconds > 0) {
                                add(
                                    WorkoutStep.Rest(
                                        details = "Rest for ${item.recommendedRestDurationBetweenSets.inWholeSeconds} seconds",
                                        durationSeconds = item.recommendedRestDurationBetweenSets.inWholeSeconds
                                    )
                                )
                            }
                        }
                    }
                    is SavedExerciseByDuration -> {
                        for (i in 1..item.amountOfSets) {
                            add(
                                WorkoutStep.Exercise(
                                    exerciseId = item.exerciseDefinition.id.value,
                                    exerciseName = item.exerciseDefinition.name,
                                    details = "${item.duration.inWholeSeconds} seconds",
                                    durationSeconds = item.duration.inWholeSeconds
                                )
                            )
                            if (i < item.amountOfSets && item.recommendedRestDurationBetweenSets.inWholeSeconds > 0) {
                                add(
                                    WorkoutStep.Rest(
                                        details = "Rest for ${item.recommendedRestDurationBetweenSets.inWholeSeconds} seconds",
                                        durationSeconds = item.recommendedRestDurationBetweenSets.inWholeSeconds
                                    )
                                )
                            }
                        }
                    }
                    is SavedRestDurationBetweenExercises -> {
                        if (item.restDuration.inWholeSeconds > 0) {
                            add(
                                WorkoutStep.Rest(
                                    details = "Rest for ${item.restDuration.inWholeSeconds} seconds",
                                    durationSeconds = item.restDuration.inWholeSeconds
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    val currentStep = localWorkoutSteps.getOrNull(currentExerciseIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routine.name) },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close,
                            contentDescription = "Close workout")
                    }
                }

            )
        }
    ) { padding ->
        if (currentStep == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Workout finished or routine is empty.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val imageContainerModifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                Box(
                    modifier = imageContainerModifier,
                    contentAlignment = Alignment.Center
                ) {
                    when (currentStep) {
                        is WorkoutStep.Exercise -> {
                            ExerciseImageFromApi(
                                exerciseId = currentStep.exerciseId,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is WorkoutStep.Rest -> {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "Rest",
                                modifier = Modifier.size(200.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                when (currentStep) {
                    is WorkoutStep.Exercise -> {
                        Text(
                            text = currentStep.exerciseName,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.clickable {
                                navController.navigate("exercise_detail/${currentStep.exerciseId}")
                            }
                        )
                        Text(currentStep.details, style = MaterialTheme.typography.titleLarge)

                        if (currentStep.durationSeconds != null) {
                            Timer(
                                time = currentStep.durationSeconds,
                                currentIndex = currentExerciseIndex,
                                onTimerFinished = {
                                    if (currentExerciseIndex < localWorkoutSteps.lastIndex) {
                                        onExerciseChange(currentExerciseIndex + 1)
                                    } else {
                                        onFinish()
                                    }
                                }
                            )
                        }
                    }
                    is WorkoutStep.Rest -> {
                        Text("Rest", style = MaterialTheme.typography.headlineSmall)
                        Timer(
                            time = currentStep.durationSeconds,
                            currentIndex = currentExerciseIndex,
                            onTimerFinished = {
                                if (currentExerciseIndex < localWorkoutSteps.lastIndex) {
                                    onExerciseChange(currentExerciseIndex + 1)
                                } else {
                                    onFinish()
                                }
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onExerciseChange(currentExerciseIndex - 1) },
                    enabled = currentExerciseIndex > 0
                ) {
                    Text("Previous")
                }

                Text("${currentExerciseIndex + 1} / ${localWorkoutSteps.size}")

                Button(
                    onClick = {
                        if (currentExerciseIndex < localWorkoutSteps.lastIndex) {
                            onExerciseChange(currentExerciseIndex + 1)
                        } else {
                            onFinish()
                        }
                    }
                ) {
                    Text(if (currentExerciseIndex < localWorkoutSteps.lastIndex) "Next" else "Finish")
                }
            }
        }
    }
}

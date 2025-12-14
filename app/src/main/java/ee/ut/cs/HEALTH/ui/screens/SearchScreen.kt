package ee.ut.cs.HEALTH.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
import ee.ut.cs.HEALTH.ui.components.RoutineInfoCard
import kotlinx.coroutines.delay





/**
 * A composable that fetches and displays an exercise image from a remote API using its ID.
 * It handles loading and error states internally by showing a progress indicator or a placeholder.
 *
 * @param exerciseId The unique identifier of the exercise to fetch the image for.
 * @param modifier The modifier to be applied to this composable.
 */
@Composable
fun ExerciseImageFromApi(exerciseId: String, modifier: Modifier = Modifier) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(exerciseId) {
        isLoading = true
        try {
            val response = RetrofitInstance.api.getExercisesById(exerciseId)
            if (response.isSuccessful) {
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
    val query by viewModel.query.collectAsStateWithLifecycle()
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    val selectedRoutine by viewModel.selectedRoutine.collectAsStateWithLifecycle()
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsStateWithLifecycle()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsStateWithLifecycle()
    val isFinishing by viewModel.isFinishingWorkout.collectAsStateWithLifecycle()
    val comment by viewModel.workoutComment.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            navController.navigate(NavDestination.STATS.route) {
                popUpTo(NavDestination.HOME.route)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            selectedRoutine == null -> {
                SearchListView(
                    query = query,
                    summaries = summaries,
                    onQueryChange = viewModel::onQueryChange,
                    onRoutineClick = viewModel::onRoutineSelect
                )
            }
            isWorkoutActive -> {
                BackHandler { viewModel.stopWorkout() }
                WorkoutView(
                    routine = selectedRoutine,
                    currentExerciseIndex = currentExerciseIndex,
                    isFinishing = isFinishing,
                    comment = comment,
                    onCommentChange = viewModel::onWorkoutCommentChange,
                    onFinalExerciseFinished = viewModel::onFinalExerciseFinished,
                    onExerciseChange = viewModel::onExerciseChange,
                    onClose = viewModel::stopWorkout,
                    onFinish = viewModel::onRoutineFinish,
                    navController = navController
                )
            }
            else -> {
                BackHandler { viewModel.onClearSelection() }
                RoutinePreview(
                    routine = selectedRoutine!!,
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
            .padding(horizontal = 16.dp),

        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            items(summaries, key = { it.id.value }) { routine ->
                RoutineInfoCard(
                    title = routine.name,
                    description = routine.description,
                    completionCount = routine.completionCount,
                    onClick = { onRoutineClick(routine.id.value) }
                )
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
                onClick = onStartWorkout,
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                text = { Text("Start Workout") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!routine.description.isNullOrBlank()) {
                item {
                    Text(
                        text = routine.description,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            itemsIndexed(routine.routineItems) { index, item ->
                val name = when (item) {
                    is SavedExerciseByDuration -> item.exerciseDefinition.name
                    is SavedExerciseByReps -> item.exerciseDefinition.name
                    is SavedRestDurationBetweenExercises -> "Rest"
                }
                Text(
                    text = "${index + 1}. $name",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


/**
 * Represents a single, flattened step within a workout session, which can be either an
 * exercise or a rest period. This is generated dynamically from the routine structure to
 * correctly handle sets and rests between them.
 */
sealed class WorkoutStep {
    /** A step that involves performing an exercise. */
    data class Exercise(
        val exerciseId: String,
        val exerciseName: String,
        val details: String,
        val durationSeconds: Long? = null
    ) : WorkoutStep()

    /** A step that involves resting. */
    data class Rest(
        val details: String,
        val durationSeconds: Long
    ) : WorkoutStep()
}

/**
 * The main view for an active workout session, also known as the "Workout Player" view.
 * It displays the current exercise or rest period, provides a timer if applicable, and includes
 * navigation controls ("Previous", "Next", "Finish"). The sequence of steps is generated
 * dynamically to account for sets and rests.
 *
 * @param routine The full [ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine] object being performed. Can be null.
 * @param currentExerciseIndex The index of the current step in the workout sequence.
 * @param isFinishing A boolean flag indicating if the workout is in the final "comment" stage.
 * @param comment The current text of the user's workout comment.
 * @param onCommentChange A callback to update the workout comment.
 * @param onExerciseChange A callback to change the current exercise index.
 * @param onClose Callback to stop the workout and return to the preview.
 * @param onFinalExerciseFinished Callback triggered when the last exercise step is completed, to transition to the finishing view.
 * @param onFinish Callback to mark the routine as complete and navigate away.
 * @param navController The [NavHostController] to handle navigation to the exercise detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutView(
    routine: ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine?,
    currentExerciseIndex: Int,
    isFinishing: Boolean,
    comment: String,
    onCommentChange: (String) -> Unit,
    onExerciseChange: (Int) -> Unit,
    onClose: () -> Unit,
    onFinalExerciseFinished: () -> Unit,
    onFinish: () -> Unit,
    navController: NavController
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
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Stop workout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                if (isFinishing) {
                    FinishWorkoutContent(
                        comment = comment,
                        onCommentChange = onCommentChange
                    )
                } else if (currentStep != null) {
                    ExerciseStepContent(
                        step = currentStep,
                        stepNumber = currentExerciseIndex + 1,
                        totalSteps = localWorkoutSteps.size,
                        navController = navController,
                        onTimerFinished = {
                            if (currentExerciseIndex < localWorkoutSteps.lastIndex) {
                                onExerciseChange(currentExerciseIndex + 1)
                            } else {
                                onFinalExerciseFinished()
                            }
                        }
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("Workout is empty or has finished.")
                    }
                }
            }

            WorkoutNavigation(
                isFinishing = isFinishing,
                currentStepIndex = currentExerciseIndex,
                totalSteps = localWorkoutSteps.size,
                onPrevious = { onExerciseChange(currentExerciseIndex - 1) },
                onNext = { onExerciseChange(currentExerciseIndex + 1) },
                onFinalExerciseFinished = onFinalExerciseFinished,
                onFinish = onFinish
            )
        }
    }
}

/**
 * Displays the content for the final workout screen, allowing the user to add a comment
 * about their session.
 *
 * @param comment The current comment text.
 * @param onCommentChange Callback to update the comment text.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinishWorkoutContent(
    comment: String,
    onCommentChange: (String) -> Unit
) {
    val presetComments = listOf("Did great!", "Not so bad", "Didn't finish", "Too easy", "Felt strong", "Could do more")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Great job!", style = MaterialTheme.typography.headlineMedium)
        Text("You've completed the workout. You can add a comment below.")

        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Add a comment (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetComments.forEach { preset ->
                ElevatedAssistChip(
                    onClick = { onCommentChange(preset) },
                    label = { Text(preset) }
                )
            }
        }
    }
}


/**
 * Displays the content for a single workout step, which can be an exercise or a rest period.
 * It shows the relevant details, such as name, reps, duration, and an image or timer.
 *
 * @param step The [WorkoutStep] to display.
 * @param stepNumber The human-readable number of the current step (e.g., 1, 2, 3).
 * @param totalSteps The total number of steps in the workout.
 * @param navController The [NavController] to handle navigation to the exercise detail screen.
 * @param onTimerFinished Callback invoked when a timer for an exercise or rest period finishes.
 */
@Composable
private fun ExerciseStepContent(
    step: WorkoutStep,
    stepNumber: Int,
    totalSteps: Int,
    navController: NavController,
    onTimerFinished: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
            when (step) {
                is WorkoutStep.Exercise -> {
                    ExerciseImageFromApi(
                        exerciseId = step.exerciseId,
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

        when (step) {
            is WorkoutStep.Exercise -> {
                Text(
                    text = step.exerciseName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.clickable {
                        navController.navigate("exercise_detail/${step.exerciseId}")
                    },
                    color = MaterialTheme.colorScheme.primary
                )
                Text(step.details, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)

                step.durationSeconds?.let {
                    Timer(
                        time = it,
                        currentIndex = stepNumber - 1,
                        onTimerFinished = onTimerFinished
                    )
                }
            }
            is WorkoutStep.Rest -> {
                Text("Rest", style = MaterialTheme.typography.headlineSmall)
                Timer(
                    time = step.durationSeconds,
                    currentIndex = stepNumber - 1,
                    onTimerFinished = onTimerFinished
                )
            }
        }
    }
}


/**
 * Renders the bottom navigation controls for the workout view.
 * It includes "Previous", "Next", and "Finish" buttons, and a step counter,
 * adapting its state based on the user's progress through the workout.
 *
 * @param isFinishing True if the workout is in the final commenting stage.
 * @param currentStepIndex The zero-based index of the current step.
 * @param totalSteps The total number of steps in the workout.
 * @param onPrevious Callback for the "Previous" or "Back" button.
 * @param onNext Callback for the "Next" button.
 * @param onFinalExerciseFinished Callback for when the user presses the button on the last step.
 * @param onFinish Callback for the final "Finish Workout" button.
 */
@Composable
private fun WorkoutNavigation(
    isFinishing: Boolean,
    currentStepIndex: Int,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinalExerciseFinished: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = currentStepIndex > 0 || isFinishing,
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isFinishing) "Back" else "Previous")
        }

        if (!isFinishing && totalSteps > 0) {
            Text(
                text = "${currentStepIndex + 1} / $totalSteps",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.padding(horizontal = 16.dp))
        }

        Button(
            onClick = {
                when {
                    isFinishing -> onFinish()
                    currentStepIndex < totalSteps - 1 -> onNext()
                    else -> onFinalExerciseFinished()
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isFinishing || (totalSteps > 0 && currentStepIndex == totalSteps - 1)) "Finish" else "Next")
        }
    }
}
/**
 * A composable that displays a countdown timer.
 * The timer is automatically restarted when the `currentIndex` changes, ensuring it resets
 * for each new exercise or rest period.
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

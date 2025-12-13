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
fun ExerciseImageFromApi(exerciseId: String, modifier: Modifier = Modifier) { // <-- VÕTAB VASTU ID!
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(exerciseId) { // <-- Reageerib ID muutusele
        isLoading = true
        try {
            // Kasutab getExerciseById API kõnet!
            val response = RetrofitInstance.api.getExercisesById(exerciseId)
            if (response.isSuccessful) {
                // Loeme andmed .data seest
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
// Defineerime WorkoutView'st väljaspool uue andmeklassi, et hoida "esitusloendi" sammu infot.
// See on palju selgem kui vana Triple<String, Long?, String?>
private sealed interface WorkoutStep {
    data class Exercise(
        val exerciseId: String,
        val exerciseName: String,
        val details: String,
        val durationSeconds: Long? = null
    ) : WorkoutStep

    data class Rest(
        val details: String, 
        val durationSeconds: Long
    ) : WorkoutStep
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutView(
    routine: ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine?,
    onClose: () -> Unit,
    onFinish: () -> Unit,
    navController: NavHostController
) {
    // 1. EHITAME KORREKTSE "ESITUSLOENDI", MIS ARVESTAB SEERIATEGA
    val workoutSteps = remember(routine) {
        if (routine == null) return@remember emptyList<WorkoutStep>()

        buildList {
            routine.routineItems.forEach { item ->
                when (item) {
                    is SavedExerciseByReps -> {
                        for (i in 1..item.amountOfSets) {
                            // Lisa harjutuse samm
                            add(
                                WorkoutStep.Exercise(
                                    exerciseId = item.exerciseDefinition.id.value,
                                    exerciseName = item.exerciseDefinition.name,
                                    details = "${item.countOfRepetitions} reps"
                                )
                            )
                            // Lisa seeriate vaheline puhkus, kui see on olemas ja pole viimane seeria
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
                            // Lisa harjutuse samm
                            add(
                                WorkoutStep.Exercise(
                                    exerciseId = item.exerciseDefinition.id.value,
                                    exerciseName = item.exerciseDefinition.name,
                                    details = "${item.duration.inWholeSeconds} seconds",
                                    durationSeconds = item.duration.inWholeSeconds
                                )
                            )
                            // Lisa seeriate vaheline puhkus, kui see on olemas ja pole viimane seeria
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
                        // Lisa harjutuste vaheline pikem puhkus
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

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentStep = workoutSteps.getOrNull(currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(routine?.name ?: "Workout") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close workout")
                    }
                }
            )
        }
    ) { padding ->
        if (currentStep == null) {
            // ... (tühja oleku kuvamine jääb samaks)
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
            // --- Ülemine osa: Pilt ja Nimi ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 2. KUVAME INFOT 'currentStep' PÕHJAL
                when (currentStep) {
                    is WorkoutStep.Exercise -> {
                        ExerciseImageFromApi(
                            exerciseId = currentStep.exerciseId,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                        Text(
                            text = currentStep.exerciseName,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.clickable {
                                navController.navigate("exercise_detail/${currentStep.exerciseId}")
                            }
                        )
                        Text(currentStep.details, style = MaterialTheme.typography.titleLarge)

                        // Kui harjutus on ajapõhine, näita taimerit
                        if (currentStep.durationSeconds != null) {
                            Timer(
                                time = currentStep.durationSeconds,
                                currentIndex = currentIndex,
                                onTimerFinished = {
                                    if (currentIndex < workoutSteps.lastIndex) {
                                        currentIndex++
                                    } else {
                                        onFinish()
                                    }
                                }
                            )
                        }
                    }
                    is WorkoutStep.Rest -> {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Rest",
                            modifier = Modifier.size(200.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Rest", style = MaterialTheme.typography.headlineSmall)
                        Timer(
                            time = currentStep.durationSeconds,
                            currentIndex = currentIndex,
                            onTimerFinished = {
                                if (currentIndex < workoutSteps.lastIndex) {
                                    currentIndex++
                                } else {
                                    onFinish()
                                }
                            }
                        )
                    }
                }
            }

            // --- Alumised nupud ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0
                ) {
                    Text("Previous")
                }

                Text("${currentIndex + 1} / ${workoutSteps.size}")

                Button(
                    onClick = {
                        if (currentIndex < workoutSteps.lastIndex) {
                            currentIndex++
                        } else {
                            onFinish()
                        }
                    }
                ) {
                    Text(if (currentIndex < workoutSteps.lastIndex) "Next" else "Finish")
                }
            }
        }
    }
}


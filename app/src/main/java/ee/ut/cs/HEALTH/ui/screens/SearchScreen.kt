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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import ee.ut.cs.HEALTH.domain.model.remote.RetrofitInstance
import ee.ut.cs.HEALTH.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import ee.ut.cs.HEALTH.ui.navigation.NavDestination


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
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            navController.navigate(NavDestination.STATS.route) {
                popUpTo(NavDestination.SEARCH.route) { inclusive = true }
            }
        }
    }
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
            onFinish = viewModel::onRoutineFinish,
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
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }

        // Show routine details if available, otherwise show a loading indicator.
        routine?.let { r ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 72.dp)
            ) {
                Text(r.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                Text(r.description.orEmpty(), fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 16.dp))

                val items = mutableListOf<Triple<String, Long?, String?>>()

                for (item in r.routineItems) {
                    when (item) {
                        is SavedExerciseByReps -> {
                            for (i in 1..item.amountOfSets) {
                                items.add(Triple("${item.exerciseDefinition.name} ${item.countOfRepetitions} times", null, item.exerciseDefinition.name))
                                items.add(Triple("Rest for ${item.recommendedRestDurationBetweenSets.inWholeSeconds} seconds", item.recommendedRestDurationBetweenSets.inWholeSeconds, null))
                            }

                        }
                        is SavedExerciseByDuration -> {
                            for (i in 1..item.amountOfSets) {
                                items.add(Triple("${item.exerciseDefinition.name} for ${item.duration.inWholeSeconds} seconds", item.duration.inWholeSeconds,item.exerciseDefinition.name))
                                items.add(Triple("Rest for ${item.recommendedRestDurationBetweenSets.inWholeSeconds} seconds", item.recommendedRestDurationBetweenSets.inWholeSeconds,null))

                            }
                        }
                        is SavedRestDurationBetweenExercises -> {
                            items.add(Triple("Rest for ${item.restDuration.inWholeSeconds}s", item.restDuration.inWholeSeconds, null))
                        }
                    }
                }
                if (items.isEmpty()) {
                    Text("This routine is empty.")
                    return
                }

                var currentIndex by remember { mutableIntStateOf(0) }
                val currentItem = items[currentIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Exercise title
                    Text(
                        text = currentItem.first,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                        maxLines = 2
                    )
                    val currentExerciseName = (items[currentIndex].third as? String)
                    if (currentExerciseName != null) {
                        ExerciseImageFromApi(
                            exerciseName = currentExerciseName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(bottom = 16.dp)
                        )
                    }

                    // Timer (if present) — only take as much space as needed
                    currentItem.second?.let { duration ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp),
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
                                            if (currentIndex < items.lastIndex) currentIndex++ else onClose()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bottom row: progress + buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Progress text
                        Text(
                            text = "Exercise ${currentIndex + 1} of ${items.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Navigation buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { if (currentIndex > 0) currentIndex-- },
                                enabled = currentIndex > 0
                            ) {
                                Text("Previous")
                            }
                            val isLastItem = currentIndex == items.lastIndex
                            Button(
                                onClick = {
                                    if (isLastItem) {
                                        onFinish()
                                    } else {
                                        currentIndex++
                                    }
                                }
                            ) {
                                Text(if (isLastItem) "Finish" else "Next")
                            }
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
}

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
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.default_profile_pic),
                error = androidx.compose.ui.res.painterResource(id = R.drawable.default_profile_pic)
            )
        }
    }
}
@Composable
fun Timer(
    time: Long,
    currentIndex: Int,
    onTimerFinished: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(time) }

    LaunchedEffect(currentIndex) {
        timeLeft = time
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        // 👇 When loop ends, trigger callback
        onTimerFinished()
    }

    Text("Time left: $timeLeft")
}
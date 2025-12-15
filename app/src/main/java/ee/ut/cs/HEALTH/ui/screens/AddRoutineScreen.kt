package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ee.ut.cs.HEALTH.ui.components.AddRoutineScreen.AddItemButton
import ee.ut.cs.HEALTH.viewmodel.AddRoutineViewModel
import ee.ut.cs.HEALTH.viewmodel.RoutineEvent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.NewRestDurationBetweenExercises

/**
 * A screen for creating and editing a workout routine.
 *
 * This composable function provides the user interface for building a new routine.
 * It allows the user to set a name and description, add exercises or rest periods,
 * reorder or remove items, and finally save the routine. The screen observes state
 * from [AddRoutineViewModel] and sends user actions back as [RoutineEvent]s.
 *
 * It also handles side effects such as showing a Toast message on success and
 * navigating away once the routine is saved.
 *
 * @param viewModel The [AddRoutineViewModel] that manages the state and logic for this screen.
 * @param navController The [NavController] used for navigation, e.g., after saving.
 * @param darkMode A boolean indicating if dark mode is currently enabled.
 * @param onToggleDarkMode A lambda function to toggle the dark mode setting.
 */
@Composable
fun AddRoutineScreen(
    viewModel: AddRoutineViewModel,
    navController: NavController, darkMode: Boolean, onToggleDarkMode: (Boolean) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val exerciseDefinitions by viewModel.exerciseDefinitions.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onToastShown()
        }
    }

    val scrollState = rememberScrollState()


    if (state.saveSuccess) {
        LaunchedEffect(true) {
            navController.navigate("search?routineId={routineId}") {
                popUpTo("home")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add New Routine", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.routine.name,
            onValueChange = { viewModel.onEvent(RoutineEvent.SetRoutineName(it)) },
            label = { Text("Routine name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.routine.description ?: "",
            onValueChange = { viewModel.onEvent(RoutineEvent.SetRoutineDescription(it)) },
            label = { Text("Routine description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        HorizontalDivider()

        if (state.routine.routineItems.isNotEmpty()) {
            Text("Exercises in Routine", style = MaterialTheme.typography.titleMedium)
        }
        BoxWithConstraints(modifier = Modifier.weight(1f, fill = false)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = this.maxHeight)
            ) {
                itemsIndexed(state.routine.routineItems, key = { _, item -> item.hashCode() }) { index, item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            when (item) {
                                is NewExerciseByReps -> {
                                    Text(
                                        "🔁 ${item.exerciseDefinition.name} – ${item.countOfRepetitions} reps, ${item.amountOfSets} sets"
                                    )
                                    item.weight?.let {
                                        Text(
                                            "Weight: ${it.inKg}kg",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        "Rest between sets: ${item.recommendedRestDurationBetweenSets.inWholeSeconds}s",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                is NewExerciseByDuration -> {
                                    Text(
                                        "⏱ ${item.exerciseDefinition.name} – ${item.duration.inWholeSeconds}s, ${item.amountOfSets} sets"
                                    )
                                    item.weight?.let {
                                        Text(
                                            "Weight: ${it.inKg}kg",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        "Rest between sets: ${item.recommendedRestDurationBetweenSets.inWholeSeconds}s",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                is NewRestDurationBetweenExercises -> {
                                    Text("Rest between exercises: ${item.restDuration.inWholeSeconds}s")
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        if (index > 0) {
                                            viewModel.onEvent(
                                                RoutineEvent.MoveRoutineItem(
                                                    from = index,
                                                    to = index - 1
                                                )
                                            )
                                        }
                                    },
                                    enabled = index > 0
                                ) { Text("Move ↑") }

                                OutlinedButton(
                                    onClick = {
                                        if (index < state.routine.routineItems.lastIndex) {
                                            viewModel.onEvent(
                                                RoutineEvent.MoveRoutineItem(
                                                    from = index,
                                                    to = index + 1
                                                )
                                            )
                                        }
                                    },
                                    enabled = index < state.routine.routineItems.lastIndex
                                ) { Text("Move ↓") }

                                Spacer(Modifier.weight(1f))

                                TextButton(
                                    onClick = {
                                        viewModel.onEvent(RoutineEvent.RemoveRoutineItemAt(index))
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) { Text("Remove") }
                            }
                        }
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddItemButton(
                viewModel = viewModel,
                exerciseDefinitions = exerciseDefinitions,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            val canSave = state.routine.name.isNotBlank() &&
                    state.routine.routineItems.isNotEmpty()

            Button(
                onClick = { viewModel.onEvent(RoutineEvent.Save) },
                enabled = canSave && !state.isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save routine")
            }
        }
    }
}

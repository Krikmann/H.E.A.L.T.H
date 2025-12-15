package ee.ut.cs.HEALTH.ui.components.AddRoutineScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.NewRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutineItem
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseDefinition
import ee.ut.cs.HEALTH.domain.model.routine.Weight
import ee.ut.cs.HEALTH.viewmodel.AddRoutineViewModel
import ee.ut.cs.HEALTH.viewmodel.RoutineEvent
import ee.ut.cs.HEALTH.viewmodel.SearchResult
import kotlin.time.Duration.Companion.seconds

private enum class ItemKind { EXERCISE, REST }
private enum class ExerciseMode { REPS, DURATION }

/**
 * A button that opens a dialog for adding a new item (exercise or rest) to a routine.
 *
 * This composable acts as the entry point for the user to add new components to their
 * workout routine. Clicking it displays the [AddItemDialog].
 *
 * @param viewModel The [AddRoutineViewModel] instance to which the new item event will be sent.
 * @param exerciseDefinitions A list of predefined exercises available for selection.
 * @param modifier The modifier to be applied to the button.
 */
@Composable
fun AddItemButton(
    viewModel: AddRoutineViewModel,
    exerciseDefinitions: List<SavedExerciseDefinition>,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }

    Button(
        onClick = { show = true },
        modifier = modifier.height(48.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add Item")
    }

    if (show) {
        AddItemDialog(
            viewModel = viewModel,
            exerciseDefinitions = exerciseDefinitions,
            onDismiss = { show = false },
            onAdd = { item ->
                viewModel.onEvent(RoutineEvent.AddRoutineItem(item))
                show = false
            }
        )
    }
}

/**
 * A comprehensive dialog for configuring and adding a new routine item.
 *
 * This dialog allows the user to choose between adding an exercise or a rest period.
 * For exercises, it provides fields for searching, setting reps/duration, sets, weight,
 * and rest between sets. It handles API calls for searching exercises and manages
 * loading, error, and result states.
 *
 * @param viewModel The [AddRoutineViewModel] used for searching exercises.
 * @param exerciseDefinitions A list of locally saved exercises for searching.
 * @param onDismiss A lambda to be invoked when the user dismisses the dialog.
 * @param onAdd A lambda to be invoked when the user confirms adding a new item.
 */
@Composable
private fun AddItemDialog(
    viewModel: AddRoutineViewModel,
    exerciseDefinitions: List<SavedExerciseDefinition>,
    onDismiss: () -> Unit,
    onAdd: (NewRoutineItem) -> Unit
) {
    var itemKind by remember { mutableStateOf(ItemKind.EXERCISE) }
    var exerciseMode by remember { mutableStateOf(ExerciseMode.REPS) }
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SavedExerciseDefinition>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<SavedExerciseDefinition?>(null) }
    var searchTriggered by remember { mutableStateOf(false) }

    var restSeconds by remember { mutableStateOf("60") }
    var sets by remember { mutableStateOf("3") }
    var weightKg by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("10") }
    var durationSeconds by remember { mutableStateOf("30") }
    var restBetweenSetsSeconds by remember { mutableStateOf("0") }

    var showNoInternetDialog by remember { mutableStateOf(false) }
    var apiErrorDetails by remember { mutableStateOf<Pair<Int, String>?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(text = "Add routine item", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                val isBoxVisible = isSearching || searchResults.isNotEmpty() || (searchTriggered && !isSearching && searchResults.isEmpty())
                val verticalSpacing = if (isBoxVisible) 8.dp else 16.dp

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing)
                ) {
                    SegmentedTwoWay(
                        left = "Exercise",
                        right = "Rest time",
                        selectedLeft = itemKind == ItemKind.EXERCISE,
                        onSelect = { itemKind = if (it) ItemKind.EXERCISE else ItemKind.REST }
                    )

                    when (itemKind) {
                        ItemKind.REST -> {
                            DurationSecondsField(
                                label = "Rest duration (seconds)",
                                value = restSeconds,
                                onChange = { restSeconds = it }
                            )
                        }

                        ItemKind.EXERCISE -> {
                            OutlinedTextField(
                                value = query,
                                onValueChange = {
                                    query = it
                                    if(searchTriggered) searchTriggered = false
                                },
                                label = { Text("Search exercise") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("exerciseSearchField"),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (query.length >= 3) {
                                                isSearching = true
                                                searchTriggered = true
                                                viewModel.searchExercises(query) { result ->
                                                    when (result) {
                                                        is SearchResult.Success -> searchResults = result.exercises
                                                        is SearchResult.NoInternet -> {
                                                            showNoInternetDialog = true
                                                            searchResults = emptyList()
                                                        }
                                                        is SearchResult.ApiError -> {
                                                            apiErrorDetails = Pair(result.code, result.message)
                                                            searchResults = emptyList()
                                                        }
                                                    }
                                                    isSearching = false
                                                }
                                            }
                                        },
                                        enabled = !isSearching
                                    ) {
                                        Icon(Icons.Default.Search, "Search")
                                    }
                                }
                            )

                            if (isBoxVisible) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    if (isSearching) {
                                        CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                                    } else if (searchTriggered && searchResults.isEmpty()) {
                                        Text(
                                            text = "Did not find any exercise",
                                            modifier = Modifier.padding(top = 16.dp)
                                        )
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.testTag("exerciseResultsList")
                                        ) {
                                            items(searchResults) { exercise ->
                                                Text(
                                                    text = exercise.name,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("exerciseResultItem")
                                                        .clickable {
                                                            selectedExercise = exercise
                                                            query = exercise.name
                                                            searchResults = emptyList()
                                                            searchTriggered = false
                                                        }
                                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                NumberField(
                                    label = "Sets",
                                    value = sets,
                                    onChange = { sets = it },
                                    modifier = Modifier.weight(1f)
                                )
                                NumberField(
                                    label = "Weight (kg)",
                                    value = weightKg,
                                    onChange = { weightKg = it },
                                    modifier = Modifier.weight(1f),
                                    allowDecimals = true
                                )
                            }

                            DurationSecondsField(
                                label = "Rest between sets (seconds)",
                                value = restBetweenSetsSeconds,
                                onChange = { restBetweenSetsSeconds = it }
                            )

                            SegmentedTwoWay(
                                left = "Reps",
                                right = "Duration",
                                selectedLeft = exerciseMode == ExerciseMode.REPS,
                                onSelect = { exerciseMode = if (it) ExerciseMode.REPS else ExerciseMode.DURATION }
                            )

                            when (exerciseMode) {
                                ExerciseMode.REPS -> NumberField("Reps", reps, { reps = it })
                                ExerciseMode.DURATION -> DurationSecondsField("Duration (seconds)", durationSeconds, { durationSeconds = it })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val restBetweenSets = restBetweenSetsSeconds.toIntOrNull()?.coerceAtLeast(0)?.seconds ?: 0.seconds
                            val item = when (itemKind) {
                                ItemKind.REST -> NewRestDurationBetweenExercises((restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: 0).seconds)
                                ItemKind.EXERCISE -> {
                                    val def = selectedExercise ?: return@Button
                                    val amtSets = sets.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                    val wt = weightKg.toDoubleOrNull()?.let { Weight.fromKg(it) }
                                    when (exerciseMode) {
                                        ExerciseMode.REPS -> NewExerciseByReps(def, restBetweenSets, amtSets, wt, reps.toIntOrNull()?.coerceAtLeast(1) ?: 1)
                                        ExerciseMode.DURATION -> NewExerciseByDuration(def, restBetweenSets, amtSets, wt, (durationSeconds.toIntOrNull()?.coerceAtLeast(1) ?: 1).seconds)
                                    }
                                }
                            }
                            onAdd(item)
                        },
                        enabled = itemKind == ItemKind.REST || (itemKind == ItemKind.EXERCISE && selectedExercise != null)
                    ) { Text("Add item") }
                }
            }
        }
    }

    if (showNoInternetDialog) {
        NoInternetDialog(onDismiss = { showNoInternetDialog = false })
    }
}

/**
 * A two-option segmented button control.
 *
 * This composable creates two buttons side-by-side, allowing the user to select one of two
 * options. The selected option is highlighted.
 *
 * @param left The text for the left button.
 * @param right The text for the right button.
 * @param selectedLeft A boolean indicating whether the left button is currently selected.
 * @param onSelect A callback invoked with `true` if the left button is selected, `false` otherwise.
 */
@Composable
private fun SegmentedTwoWay(
    left: String,
    right: String,
    selectedLeft: Boolean,
    onSelect: (leftSelected: Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val leftColors = if (selectedLeft) ButtonDefaults.filledTonalButtonColors()
        else ButtonDefaults.outlinedButtonColors()
        val rightColors = if (!selectedLeft) ButtonDefaults.filledTonalButtonColors()
        else ButtonDefaults.outlinedButtonColors()

        Button(
            onClick = { onSelect(true) },
            colors = leftColors,
            modifier = Modifier.weight(1f)
        ) { Text(left) }

        Button(
            onClick = { onSelect(false) },
            colors = rightColors,
            modifier = Modifier.weight(1f)
        ) { Text(right) }
    }
}

/**
 * A styled [OutlinedTextField] specifically for numeric input.
 *
 * This field filters user input to only allow digits and, optionally, a single decimal point.
 *
 * @param label The label text for the text field.
 * @param value The current string value of the text field.
 * @param onChange A callback invoked when the value changes.
 * @param modifier The modifier to be applied to the text field.
 * @param allowDecimals If true, allows a single decimal point in the input.
 */
@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowDecimals: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { s ->
            val filtered =
                s.filter { it.isDigit() || (allowDecimals && it == '.' && '.' !in value) }
            onChange(filtered)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimals) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * A specialized [NumberField] for inputting duration in seconds.
 *
 * This is a convenience composable that wraps [NumberField] with a specific label and behavior
 * for entering time in seconds.
 *
 * @param label The label text for the text field.
 * @param value The current string value (duration in seconds).
 * @param onChange A callback invoked when the value changes.
 */
@Composable
private fun DurationSecondsField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    NumberField(label = label, value = value, onChange = onChange)
}

/**
 * A simple alert dialog to inform the user about a lack of internet connection.
 *
 * @param onDismiss A lambda to be invoked when the user dismisses the dialog.
 */
@Composable
fun NoInternetDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No Internet") },
        text = { Text("You need an internet connection to search exercises.") },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

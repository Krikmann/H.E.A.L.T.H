package ee.ut.cs.HEALTH.ui.components.AddRoutineScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private enum class ItemKind { EXERCISE, REST }
private enum class ExerciseMode { REPS, DURATION }

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
    var searchTriggered by remember { mutableStateOf(false) } // Uus muutuja

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
                                    } else if (searchResults.isEmpty()) {
                                        // "Ei leidnud" teade kuvatakse ainult siis, kui otsing on käivitatud ja tulemused on tühjad
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
                                                            searchTriggered = false // Peida kast pärast valikut
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

@Composable
private fun DurationSecondsField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    NumberField(label = label, value = value, onChange = onChange)
}

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


package ee.ut.cs.HEALTH.ui.components.AddRoutineScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.NewRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutineItem
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseDefinition
import ee.ut.cs.HEALTH.domain.model.routine.Weight
import ee.ut.cs.HEALTH.viewmodel.AddRoutineViewModel
import ee.ut.cs.HEALTH.viewmodel.RoutineEvent
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
    exerciseDefinitions: List<SavedExerciseDefinition>,
    onDismiss: () -> Unit,
    onAdd: (NewRoutineItem) -> Unit
) {
    var itemKind by remember { mutableStateOf(ItemKind.EXERCISE) }
    var exerciseMode by remember { mutableStateOf(ExerciseMode.REPS) }

    var restSeconds by remember { mutableStateOf("60") }

    var selectedDefIndex by remember { mutableIntStateOf(0) } // default to first
    var sets by remember { mutableStateOf("3") }
    var weightKg by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("10") }
    var durationSeconds by remember { mutableStateOf("30") }
    var restBetweenSetsSeconds by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val restBetweenSets = restBetweenSetsSeconds
                        .toIntOrNull()
                        ?.coerceAtLeast(0)
                        ?.seconds
                        ?: 0.seconds

                    val item = when (itemKind) {
                        ItemKind.REST -> {
                            val sec = restSeconds.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            NewRestDurationBetweenExercises(restDuration = sec.seconds)
                        }
                        ItemKind.EXERCISE -> {
                            val def = exerciseDefinitions.getOrNull(selectedDefIndex)
                                ?: return@TextButton
                            val amtSets = sets.toIntOrNull()?.coerceAtLeast(1) ?: 1
                            val wt = weightKg.toDoubleOrNull()?.let { Weight.fromKg(it) }

                            when (exerciseMode) {
                                ExerciseMode.REPS -> {
                                    val r = reps.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                    NewExerciseByReps(
                                        exerciseDefinition = def,
                                        recommendedRestDurationBetweenSets = restBetweenSets,
                                        amountOfSets = amtSets,
                                        weight = wt,
                                        countOfRepetitions = r
                                    )
                                }
                                ExerciseMode.DURATION -> {
                                    val sec = durationSeconds.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                    NewExerciseByDuration(
                                        exerciseDefinition = def,
                                        recommendedRestDurationBetweenSets = restBetweenSets,
                                        amountOfSets = amtSets,
                                        weight = wt,
                                        duration = sec.seconds
                                    )
                                }
                            }
                        }
                    }
                    onAdd(item)
                }
            ) { Text("Add item") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Add routine item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

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
                        // Exercise Definition dropdown
                        ExerciseDefinitionDropdown(
                            definitions = exerciseDefinitions,
                            selectedIndex = selectedDefIndex,
                            onSelected = { selectedDefIndex = it }
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

                        Spacer(Modifier.height(12.dp))

                        DurationSecondsField(
                            label = "Rest between sets (seconds)",
                            value = restBetweenSetsSeconds,
                            onChange = { restBetweenSetsSeconds = it }
                        )

                        Spacer(Modifier.height(12.dp))

                        // Reps vs Duration toggle
                        SegmentedTwoWay(
                            left = "Reps",
                            right = "Duration",
                            selectedLeft = exerciseMode == ExerciseMode.REPS,
                            onSelect = { exerciseMode = if (it) ExerciseMode.REPS else ExerciseMode.DURATION }
                        )

                        Spacer(Modifier.height(8.dp))

                        when (exerciseMode) {
                            ExerciseMode.REPS -> {
                                NumberField(
                                    label = "Reps",
                                    value = reps,
                                    onChange = { reps = it }
                                )
                            }
                            ExerciseMode.DURATION -> {
                                DurationSecondsField(
                                    label = "Duration (seconds)",
                                    value = durationSeconds,
                                    onChange = { durationSeconds = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
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
            val filtered = s.filter { it.isDigit() || (allowDecimals && it == '.' && '.' !in value) }
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
private fun ExerciseDefinitionDropdown(
    definitions: List<SavedExerciseDefinition>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = definitions.getOrNull(selectedIndex)?.name ?: "Select exercise"

    Column {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            label = { Text("Exercise definition") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            definitions.forEachIndexed { i, def ->
                DropdownMenuItem(
                    text = { Text(def.name) },
                    onClick = {
                        onSelected(i)
                        expanded = false
                    }
                )
            }
        }
    }
}
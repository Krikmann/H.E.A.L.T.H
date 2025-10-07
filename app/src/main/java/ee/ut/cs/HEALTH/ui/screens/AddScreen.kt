package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.ui.components.AutoCompleteTextField
import ee.ut.cs.HEALTH.data.local.database.exerciseList
import ee.ut.cs.HEALTH.viewmodel.AddRoutineViewModel



sealed class UserExercise {
    data class ByReps(val name: String, val reps: Int, val sets: Int, val weight: Double?) :
        UserExercise()

    data class ByDuration(
        val name: String,
        val durationSeconds: Int,
        val sets: Int,
        val weight: Double?
    ) : UserExercise()
}

@Composable
fun AddScreen(dao: RoutineDao)  {
    val viewModel = viewModel { AddRoutineViewModel(dao) }
    val routineName = viewModel.routineName
    val routineDescription = viewModel.routineDescription
    val exerciseName = viewModel.exerciseName
    val reps = viewModel.reps
    val sets = viewModel.sets
    val duration = viewModel.duration
    val weight = viewModel.weight
    val restDuration = viewModel.restDuration
    val inputMode by remember { derivedStateOf { viewModel.inputMode } }

    val suggestions = exerciseList
    val scrollState = rememberScrollState()

    val exercises = viewModel.exercises


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add New Routine", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = routineName,
            onValueChange = { viewModel.routineName = it },
            label = { Text("Routine name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = routineDescription,
            onValueChange = { viewModel.routineDescription = it },
            label = { Text("Routine description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)





        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = restDuration,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) viewModel.restDuration = it },
                label = { Text("Rest(sec)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            listOf("Reps", "Duration").forEach { mode ->
                Button(
                    onClick = { viewModel.inputMode = mode },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (inputMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(mode)
                }
            }

        }
        AutoCompleteTextField(
            value = exerciseName,
            onValueChange = { viewModel.exerciseName = it },
            label = "Exercise name",
            suggestions = suggestions,
            onSuggestionSelected = { selected -> viewModel.exerciseName = selected }
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            if (inputMode == "Reps") {
                OutlinedTextField(
                    value = reps,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) viewModel.reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )


            }

            if (inputMode == "Duration") {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) viewModel.duration = it },
                    label = { Text("Duration") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
            }
            OutlinedTextField(
                value = sets,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) viewModel.sets = it },
                label = { Text("Sets") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) viewModel.weight = it },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.addExercise() },

                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = "add exercise")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add exercise")
            }


        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        Text("Exercises in Routine", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
            .heightIn(max = 300.dp)
        ) {
            items(exercises) { exercise ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (exercise) {
                            is UserExercise.ByReps -> Text("🔁 ${exercise.name} – ${exercise.reps} reps, ${exercise.sets} sets, ${exercise.weight ?: 0.0} kg")
                            is UserExercise.ByDuration -> Text("⏱ ${exercise.name} – ${exercise.durationSeconds}s, ${exercise.sets} sets, ${exercise.weight ?: 0.0} kg")
                        }
                    }
                }
            }

        }

        Button(
            onClick = {
                viewModel.saveRoutine()
                println("Routine: $routineName ($routineDescription)")
                println("Exercises: $exercises")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Routine")
        }
    }
}

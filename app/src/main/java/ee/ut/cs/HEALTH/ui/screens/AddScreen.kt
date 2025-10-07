package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.ui.components.AutoCompleteTextField
import ee.ut.cs.HEALTH.data.local.database.exerciseList

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
fun AddScreen(dao: RoutineDao) {
    var routineName by remember { mutableStateOf("") }
    var routineDescription by remember { mutableStateOf("") }

    var suggestions by remember { mutableStateOf(exerciseList.toMutableList()) }

    var exerciseName by remember { mutableStateOf("") }
    var restDuration by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val exercises = remember { mutableStateListOf<UserExercise>() }

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
            onValueChange = { routineName = it },
            label = { Text("Routine name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = routineDescription,
            onValueChange = { routineDescription = it },
            label = { Text("Routine description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)



        var inputMode by remember { mutableStateOf("Reps") }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = restDuration,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) restDuration = it },
                label = { Text("Rest(sec)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            listOf("Reps", "Duration").forEach { mode ->
                Button(
                    onClick = { inputMode = mode },
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
            onValueChange = { exerciseName = it },
            label = "Exercise name",
            suggestions = suggestions,
            onSuggestionSelected = { selected -> exerciseName = selected }
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
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) reps = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                OutlinedTextField(
                    value = sets,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) sets = it },
                    label = { Text("Sets") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
            }

            if (inputMode == "Duration") {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) duration = it },
                    label = { Text("Duration (seconds)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
            }

            OutlinedTextField(
                value = weight,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*\$"))) weight = it },
                label = { Text("Weight(kg)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
        }


        /* Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             OutlinedTextField(
                 value = reps,
                 onValueChange = {  if (it.all { char -> char.isDigit() }) reps = it  },
                 label = { Text("Reps") },
                 modifier = Modifier.weight(1f),
                 keyboardOptions = KeyboardOptions.Default.copy(
                     keyboardType = KeyboardType.Number
                 )
             )
             OutlinedTextField(
                 value = duration,
                 onValueChange = { if (it.all { char -> char.isDigit() }) duration = it },
                 label = { Text("Duration (sec)") },
                 modifier = Modifier.weight(1f),
                 keyboardOptions = KeyboardOptions.Default.copy(
                     keyboardType = KeyboardType.Number
                 )
             )
         }

         Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             OutlinedTextField(
                 value = sets,
                 onValueChange = { sets = it },
                 label = { Text("Sets") },
                 modifier = Modifier.weight(1f)
             )
             OutlinedTextField(
                 value = weight,
                 onValueChange = { weight = it },
                 label = { Text("Weight (kg)") },
                 modifier = Modifier.weight(1f)
             )
         }*/

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    val repsInt = reps.toIntOrNull()
                    val setsInt = sets.toIntOrNull()
                    val weightDouble = weight.toDoubleOrNull()
                    val durationInt = duration.toIntOrNull()


                    if (exerciseName.isNotBlank() && repsInt != null && setsInt != null) {
                        exercises.add(
                            UserExercise.ByReps(
                                exerciseName,
                                repsInt,
                                setsInt,
                                weightDouble
                            )
                        )
                        if (!suggestions.contains(exerciseName)) suggestions.add(exerciseName)

                    } else if (exerciseName.isNotBlank() && durationInt != null && setsInt != null) {
                        exercises.add(
                            UserExercise.ByDuration(
                                exerciseName,
                                durationInt,
                                setsInt,
                                weightDouble
                            )
                        )
                        if (!suggestions.contains(exerciseName)) suggestions.add(exerciseName)

                    }
                    exerciseName = ""
                    reps = ""
                    sets = ""
                    duration = ""
                    weight = ""
                },
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
                println("Routine: $routineName ($routineDescription)")
                println("Exercises: $exercises")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlaylistAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Routine")
        }
    }
}

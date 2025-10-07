package ee.ut.cs.HEALTH.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.ui.screens.UserExercise

class AddRoutineViewModel(
    private val dao: RoutineDao
) : ViewModel() {

    var routineName by mutableStateOf("")
    var routineDescription by mutableStateOf("")
    var exerciseName by mutableStateOf("")
    var reps by mutableStateOf("")
    var sets by mutableStateOf("")
    var duration by mutableStateOf("")
    var weight by mutableStateOf("")
    var restDuration by mutableStateOf("")
    var inputMode by mutableStateOf("Reps")

    var suggestions = mutableStateListOf<String>()
    var exercises = mutableStateListOf<UserExercise>()

    fun addExercise() {
        val repsInt = reps.toIntOrNull()
        val setsInt = sets.toIntOrNull()
        val weightDouble = weight.toDoubleOrNull()
        val durationInt = duration.toIntOrNull()

        if (inputMode == "Reps" && exerciseName.isNotBlank() && repsInt != null && setsInt != null) {
            exercises.add(UserExercise.ByReps(exerciseName, repsInt, setsInt, weightDouble))
            if (!suggestions.contains(exerciseName)) suggestions.add(exerciseName)
        } else if (inputMode == "Duration" && exerciseName.isNotBlank() && durationInt != null && setsInt != null) {
            exercises.add(UserExercise.ByDuration(exerciseName, durationInt, setsInt, weightDouble))
            if (!suggestions.contains(exerciseName)) suggestions.add(exerciseName)
        }

        exerciseName = ""
        reps = ""
        sets = ""
        duration = ""
        weight = ""
    }

    fun saveRoutine() {
        // TODO: implement saving logic with dao
    }
}

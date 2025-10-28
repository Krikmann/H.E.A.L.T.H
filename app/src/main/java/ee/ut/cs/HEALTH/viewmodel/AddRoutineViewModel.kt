package ee.ut.cs.HEALTH.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.NewRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutine
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutineItem
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseDefinition
import ee.ut.cs.HEALTH.domain.model.routine.Weight
import ee.ut.cs.HEALTH.domain.model.routine.add
import ee.ut.cs.HEALTH.domain.model.routine.insertAt
import ee.ut.cs.HEALTH.domain.model.routine.move
import ee.ut.cs.HEALTH.domain.model.routine.removeAt
import ee.ut.cs.HEALTH.domain.model.routine.replaceAt
import ee.ut.cs.HEALTH.domain.model.routine.withAmountOfSets
import ee.ut.cs.HEALTH.domain.model.routine.withCountOfRepetitions
import ee.ut.cs.HEALTH.domain.model.routine.withDescription
import ee.ut.cs.HEALTH.domain.model.routine.withDuration
import ee.ut.cs.HEALTH.domain.model.routine.withExerciseDefinition
import ee.ut.cs.HEALTH.domain.model.routine.withName
import ee.ut.cs.HEALTH.domain.model.routine.withWeight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import ee.ut.cs.HEALTH.domain.model.remote.RetrofitInstance
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseDefinitionId


data class RoutineUiState(
    val routine: NewRoutine,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface RoutineEvent {
    data class AddRoutineItem(val item: NewRoutineItem) : RoutineEvent
    data class InsertRoutineItem(val index: Int, val item: NewRoutineItem) : RoutineEvent
    data class RemoveRoutineItemAt(val index: Int) : RoutineEvent
    data class MoveRoutineItem(val from: Int, val to: Int) : RoutineEvent
    data class ReplaceRoutineItemAt(val index: Int, val item: NewRoutineItem) : RoutineEvent

    data class SetRoutineName(val name: String) : RoutineEvent
    data class SetRoutineDescription(val description: String?) : RoutineEvent

    data class SetExerciseDefinition(
        val index: Int,
        val exerciseDefinition: SavedExerciseDefinition
    ) : RoutineEvent

    data class SetExerciseNrOfSets(val index: Int, val amountOfSets: Int) : RoutineEvent
    data class SetExerciseWeight(val index: Int, val weight: Weight?) : RoutineEvent
    data class SetExerciseReps(val index: Int, val amountOfReps: Int) : RoutineEvent
    data class SetExerciseDuration(val index: Int, val duration: Duration) : RoutineEvent

    data class SetRestDurationBetweenSets(val index: Int, val duration: Duration) : RoutineEvent

    data object Save : RoutineEvent
}

class AddRoutineViewModelFactory(
    private val repository: RoutineRepository,
    private val initial: NewRoutine
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AddRoutineViewModel::class.java))
        return AddRoutineViewModel(repository, initial) as T
    }
}

class AddRoutineViewModel(
    private val repository: RoutineRepository,
    initial: NewRoutine
) : ViewModel() {
    private val _state = MutableStateFlow(RoutineUiState(initial))
    val state: StateFlow<RoutineUiState> = _state

    val exerciseDefinitions: StateFlow<List<SavedExerciseDefinition>> =
        repository.getAllExerciseDefinitions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onEvent(event: RoutineEvent) = when (event) {
        is RoutineEvent.AddRoutineItem ->
            reduce { copy(routine = routine.add(event.item)) }

        is RoutineEvent.InsertRoutineItem ->
            reduce { copy(routine = routine.insertAt(event.index, event.item)) }

        is RoutineEvent.RemoveRoutineItemAt ->
            reduce { copy(routine = routine.removeAt(event.index)) }

        is RoutineEvent.MoveRoutineItem ->
            reduce { copy(routine = routine.move(event.from, event.to)) }

        is RoutineEvent.ReplaceRoutineItemAt ->
            reduce { copy(routine = routine.replaceAt(event.index, event.item)) }

        is RoutineEvent.SetRoutineName ->
            reduce { copy(routine = routine.withName(event.name)) }

        is RoutineEvent.SetRoutineDescription ->
            reduce { copy(routine = routine.withDescription(event.description)) }

        is RoutineEvent.SetExerciseDefinition ->
            transformRoutineItemAt(event.index) {
                when (it) {
                    is NewExerciseByReps -> it.withExerciseDefinition(event.exerciseDefinition)
                    is NewExerciseByDuration -> it.withExerciseDefinition(event.exerciseDefinition)
                    else -> it
                }
            }

        is RoutineEvent.SetExerciseNrOfSets ->
            transformRoutineItemAt(event.index) {
                when (it) {
                    is NewExerciseByReps -> it.withAmountOfSets(event.amountOfSets)
                    is NewExerciseByDuration -> it.withAmountOfSets(event.amountOfSets)
                    else -> it
                }
            }

        is RoutineEvent.SetExerciseWeight ->
            transformRoutineItemAt(event.index) {
                when (it) {
                    is NewExerciseByReps -> it.withWeight(event.weight)
                    is NewExerciseByDuration -> it.withWeight(event.weight)
                    else -> it
                }
            }

        is RoutineEvent.SetExerciseReps ->
            transformRoutineItemAt(event.index) {
                when (it) {
                    is NewExerciseByReps -> it.withCountOfRepetitions(event.amountOfReps)
                    else -> it
                }
            }

        is RoutineEvent.SetExerciseDuration ->
            transformRoutineItemAt(event.index) {
                when (it) {
                    is NewExerciseByDuration -> it.withDuration(event.duration)
                    else -> it
                }
            }

        is RoutineEvent.SetRestDurationBetweenSets ->
            transformRoutineItemAt(event.index) {
                when (it) {
                    is NewRestDurationBetweenExercises ->
                        NewRestDurationBetweenExercises(event.duration)

                    else -> it
                }
            }

        is RoutineEvent.Save -> saveRoutine()
    }

    private inline fun reduce(block: RoutineUiState.() -> RoutineUiState) {
        _state.update(block)
    }

    fun saveRoutine() {
        viewModelScope.launch {
            try {
                reduce { copy(isSaving = true, error = null) }
                repository.insert(state.value.routine)
                reduce { copy(isSaving = false) }
            } catch (t: Throwable) {
                reduce { copy(isSaving = false, error = t.message) }
            }
        }
    }

    private inline fun transformRoutineItemAt(
        index: Int,
        crossinline transform: (NewRoutineItem) -> NewRoutineItem
    ) {
        reduce {
            val current = routine.routineItems.getOrNull(index) ?: return@reduce this
            copy(routine = routine.replaceAt(index, transform(current)))
        }
    }

    fun searchExercises(
        query: String,
        onResult: (List<SavedExerciseDefinition>) -> Unit
    ) {
        if (query.isBlank()) {
            onResult(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                Log.d("AddRoutineViewModel", "Searching for exercises with query: $query")
                val response = RetrofitInstance.api.searchExercisesByName(query)
                Log.d("AddRoutineViewModel", "HTTP response code: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    val dtoList = body?.exercises ?: emptyList()
                    Log.d("AddRoutineViewModel", "Parsed DTO list: $dtoList")

                    val exercises = dtoList.take(25).map { dto ->
                        val id = dto.exerciseId
                        val name = dto.name
                        SavedExerciseDefinition(
                            id = ExerciseDefinitionId(id),
                            name = name
                        )
                    }

                    Log.d("AddRoutineViewModel", "Mapped ${exercises.size} exercises")
                    onResult(exercises)
                } else {
                    Log.e("AddRoutineViewModel", "Request failed: ${response.errorBody()?.string()}")
                    onResult(emptyList())
                }
            } catch (e: Exception) {
                Log.e("AddRoutineViewModel", "Error searching exercises by name: $query", e)
                onResult(emptyList())
            }
        }
    }

}

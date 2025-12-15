package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Represents the UI state for the Add/Edit Routine screen.
 *
 * @property routine The current [NewRoutine] object being built or edited.
 * @property isSaving A boolean flag indicating if a save operation is in progress.
 * @property saveSuccess A boolean flag that becomes true upon successful save, used to trigger navigation.
 * @property error An optional string containing an error message if an operation fails.
 */
data class RoutineUiState(
    val routine: NewRoutine,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

/**
 * Defines all possible user actions (events) that can occur on the Add/Edit Routine screen.
 * This sealed interface is used to pass events from the UI to the [AddRoutineViewModel].
 */
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

/**
 * Factory for creating [AddRoutineViewModel] instances.
 * This is required because the ViewModel has constructor dependencies.
 *
 * @property repository The [RoutineRepository] to be passed to the ViewModel.
 * @property initial The initial [NewRoutine] state for the ViewModel.
 */
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

/**
 * ViewModel for the "Add Routine" screen.
 *
 * This class manages the state of a new routine being created. It handles all user interactions,
 * such as adding/removing exercises, modifying routine details, and saving the final routine
 * to the [RoutineRepository]. It also provides a search functionality for exercises.
 *
 * @param repository The repository for data operations.
 * @param initial The starting state of the [NewRoutine] being created.
 */
class AddRoutineViewModel(
    private val repository: RoutineRepository,
    initial: NewRoutine
) : ViewModel() {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val _state = MutableStateFlow(RoutineUiState(initial))
    val state: StateFlow<RoutineUiState> = _state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage
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

    /**
     * Saves the current routine to the repository.
     * Updates the UI state to reflect loading, success, or error states.
     * Posts a toast message on success.
     */
    fun saveRoutine() {
        viewModelScope.launch {
            try {
                reduce { copy(isSaving = true, error = null) }
                repository.insert(state.value.routine)
                val routineName = state.value.routine.name
                _toastMessage.value = "'$routineName' saved!"

                reduce { copy(isSaving = false, saveSuccess = true) }
            } catch (t: Throwable) {
                reduce { copy(isSaving = false, error = t.message) }
            }
        }
    }

    /**
     * Resets the toast message state after it has been shown.
     */
    fun onToastShown() {
        _toastMessage.value = null
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

    /**
     * Searches for exercises via the remote API based on a query string.
     *
     * This function runs on a background thread. It handles empty queries, network errors,
     * and API errors, delivering the result through the [onResult] callback.
     *
     * @param query The search term for exercises.
     * @param onResult A callback function to deliver the [SearchResult].
     */
    fun searchExercises(query: String, onResult: (SearchResult) -> Unit) {
        viewModelScope.launch {
            if (query.isBlank()) {
                onResult(SearchResult.Success(emptyList()))
                return@launch
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.searchExercisesByName(query)
                }

                if (response.isSuccessful) {
                    val exercises = response.body()?.data?.take(25)?.map {
                        SavedExerciseDefinition(ExerciseDefinitionId(it.exerciseId), it.name)
                    } ?: emptyList()
                    onResult(SearchResult.Success(exercises))
                } else {
                    onResult(SearchResult.ApiError(response.code(), response.message()))
                }
            } catch (e: IOException) {
                onResult(SearchResult.NoInternet)
            } catch (e: Exception) {
                onResult(SearchResult.ApiError(500, e.message ?: "Unknown error"))
            }
        }
    }
}

/**
 * Represents the possible outcomes of an exercise search operation.
 */
sealed class SearchResult {
    /**
     * Represents a successful search.
     * @property exercises A list of found exercises, which may be empty.
     */
    data class Success(val exercises: List<SavedExerciseDefinition>) : SearchResult()

    /**
     * Represents a failure due to a lack of internet connectivity.
     */
    object NoInternet : SearchResult()

    /**
     * Represents a failure from the API, such as a server error.
     * @property code The HTTP status code of the error.
     * @property message A descriptive error message.
     */
    data class ApiError(val code: Int, val message: String) : SearchResult()
}

package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.domain.model.routine.RoutineId
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine
import ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * ViewModel for the Search screen.
 *
 * This class manages the state for searching routines, selecting a routine for preview,
 * and handling the active workout session. It receives an optional `routineIdToOpen`
 * to directly open a specific routine.
 *
 * @property repository The repository for accessing routine data.
 * @property routineIdToOpen An optional ID of a routine to open immediately upon initialization.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repository: RoutineRepository,
    private val routineIdToOpen: Long?
) : ViewModel() {

    /**
     * A state flow representing the current search query entered by the user.
     */
    val query = MutableStateFlow("")

    private val _navigationEvent = Channel<Unit>()

    /**
     * A flow that emits a one-time event to trigger navigation, for instance,
     * after a workout is successfully completed.
     */
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var isSaving = false

    /**
     * A flow of routine summaries that updates automatically based on the [query].
     * The results are reversed to show the most recently added routines first.
     */
    val summaries: StateFlow<List<RoutineSummary>> = query
        .flatMapLatest { repository.searchRoutineSummaries(it.trim()) }
        .map { it.reversed() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * A state flow holding the ID of the currently selected routine for preview.
     * Null if no routine is selected.
     */
    val selectedId = MutableStateFlow<Long?>(null)

    /**
     * A state flow holding the full [SavedRoutine] object for the selected routine.
     * Null if no routine is selected or if it's still loading.
     */
    val selectedRoutine = MutableStateFlow<SavedRoutine?>(null)

    private val _isWorkoutActive = MutableStateFlow(false)

    /**
     * A state flow indicating whether a workout session is currently active.
     */
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive

    private val _currentExerciseIndex = MutableStateFlow(0)

    /**
     * A state flow representing the index of the current exercise or rest step in the active workout.
     */
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex

    init {
        routineIdToOpen?.let { onRoutineSelect(it) }
    }

    private val _isFinishingWorkout = MutableStateFlow(false)

    /**
     * A state flow that becomes true when the user has completed the final exercise
     * and is now in the "finish workout" confirmation view.
     */
    val isFinishingWorkout: StateFlow<Boolean> = _isFinishingWorkout

    private val _workoutComment = MutableStateFlow("")

    /**
     * A state flow holding the comment or note the user can add upon completing a workout.
     */
    val workoutComment: StateFlow<String> = _workoutComment

    /**
     * Updates the search query.
     *
     * @param newQuery The new search string from the UI.
     */
    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /**
     * Handles the selection of a routine. Fetches the full routine details from the repository
     * and updates the [selectedRoutine] state.
     *
     * @param id The ID of the selected routine.
     */
    fun onRoutineSelect(id: Long) {
        selectedId.value = id
        _isWorkoutActive.value = false
        _currentExerciseIndex.value = 0
        viewModelScope.launch {
            repository.getRoutine(RoutineId(id)).collect { routine ->
                selectedRoutine.value = routine
            }
        }
    }

    /**
     * Clears the current routine selection and resets all related workout states.
     * This is used to return to the search list view.
     */
    fun onClearSelection() {
        selectedId.value = null
        selectedRoutine.value = null
        _isWorkoutActive.value = false
        _currentExerciseIndex.value = 0
        _isFinishingWorkout.value = false
        _workoutComment.value = ""
    }

    /**
     * Starts the active workout session for the currently selected routine.
     */
    fun startWorkout() {
        _isWorkoutActive.value = true
    }

    /**
     * Stops the active workout session and returns to the routine preview screen.
     */
    fun stopWorkout() {
        _isWorkoutActive.value = false
    }

    /**
     * Updates the workout completion comment as the user types.
     *
     * @param comment The new comment text.
     */
    fun onWorkoutCommentChange(comment: String) {
        _workoutComment.value = comment
    }

    /**
     * Sets the state to indicate that the user has reached the final confirmation step of the workout.
     */
    fun onFinalExerciseFinished() {
        _isFinishingWorkout.value = true
    }

    /**
     * Updates the current exercise index and resets the finishing state if necessary.
     * This is called when the user navigates between exercises in the workout.
     *
     * @param newIndex The index of the new exercise or rest step.
     */
    fun onExerciseChange(newIndex: Int) {
        if (_isFinishingWorkout.value) {
            _isFinishingWorkout.value = false
        }
        _currentExerciseIndex.value = newIndex
    }

    /**
     * Finalizes the workout. Marks the routine as completed in the repository, sends a
     * navigation event, and clears the selection.
     */
    fun onRoutineFinish() {
        if (isSaving) return
        viewModelScope.launch {
            try {
                isSaving = true
                selectedRoutine.value?.let {
                    val noteToSave = _workoutComment.value.trim().ifEmpty { null }
                    repository.markRoutineAsCompleted(it.id, noteToSave)
                    _navigationEvent.send(Unit)
                    onClearSelection()
                }
            } finally {
                isSaving = false
            }
        }
    }
}

/**
 * Factory for creating a [SearchViewModel] with a [RoutineRepository] dependency and an
 * optional routine ID to open. This is necessary because the ViewModel has a non-empty constructor.
 *
 * @property repository The instance of [RoutineRepository] to be provided to the ViewModel.
 * @property routineIdToOpen The optional ID of a routine to pre-load.
 */
class SearchViewModelFactory(
    private val repository: RoutineRepository,
    private val routineIdToOpen: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository, routineIdToOpen) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

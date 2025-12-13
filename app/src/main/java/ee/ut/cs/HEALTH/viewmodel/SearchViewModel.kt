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

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(private val repository: RoutineRepository, private val routineIdToOpen: Long?) : ViewModel() {

    val query = MutableStateFlow("")
    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    val summaries: StateFlow<List<RoutineSummary>> = query
        .flatMapLatest { repository.searchRoutineSummaries(it.trim()) }
        .map { it.reversed() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedId = MutableStateFlow<Long?>(null)
    val selectedRoutine = MutableStateFlow<SavedRoutine?>(null)

    // --- UUS OLEK ---
    // See hoiab meeles, kas kasutaja on "Start Workout" nuppu vajutanud
    private val _isWorkoutActive = MutableStateFlow(false)

    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive
    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex
    init {
        routineIdToOpen?.let { onRoutineSelect(it) }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

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

    fun onClearSelection() {
        selectedId.value = null
        selectedRoutine.value = null
        _isWorkoutActive.value = false
        _currentExerciseIndex.value = 0
    }

    fun startWorkout() {
        _isWorkoutActive.value = true
    }

    fun stopWorkout() {
        _isWorkoutActive.value = false
    }
    fun onExerciseChange(newIndex: Int) {
        _currentExerciseIndex.value = newIndex
    }

    fun onRoutineFinish() {
        viewModelScope.launch {
            selectedRoutine.value?.let {
                repository.markRoutineAsCompleted(it.id)
                _navigationEvent.send(Unit)
                onClearSelection()
            }
        }
    }
}

/**
 * Factory for creating a `SearchViewModel` with a `RoutineRepository` dependency.
 * This is necessary because the ViewModel has a non-empty constructor.
 */
class SearchViewModelFactory(
    private val repository: RoutineRepository,
    private val routineIdToOpen: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is `SearchViewModel`.
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            // If it is, create an instance and pass the repository.
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository,routineIdToOpen) as T
        }
        // If it's any other class, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

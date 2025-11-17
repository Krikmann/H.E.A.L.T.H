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
class SearchViewModel(private val repository: RoutineRepository) : ViewModel() {

    //  Holds the user's search input string.
    val query = MutableStateFlow("")
    private val _navigationEvent = Channel<Unit>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    //  A flow of search results that automatically updates when the `query` flow changes.
    //    `flatMapLatest` cancels the previous database query and starts a new one
    //    as the user types, which is highly efficient.
    val summaries: StateFlow<List<RoutineSummary>> = query
        .flatMapLatest { searchQuery ->
            repository.searchRoutineSummaries(searchQuery.trim()) // Uses the new repository method
        }
        .map{searchResultList ->searchResultList.reversed()}
        .stateIn(
            scope = viewModelScope,
            // `WhileSubscribed` makes the flow active only when the UI is visible,
            // saving resources. The 5000ms timeout keeps it active during brief
            // configuration changes (like screen rotation).
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // The initial state before the first query completes.
        )

    // Holds the state for the currently selected routine.
    //    `selectedId` tracks which routine is being viewed.
    //    `selectedRoutine` holds the detailed data for that routine.
    val selectedId = MutableStateFlow<Long?>(null)
    val selectedRoutine = MutableStateFlow<SavedRoutine?>(null)

    // --- UI Events ---
    // These functions are called by the UI to signal user actions.

    /**
     * Updates the search query. Called every time the user types in the search field.
     */
    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /**
     * Fetches the full details of a routine when the user selects it from the list.
     *
     * @param id The ID of the selected routine.
     */
    fun onRoutineSelect(id: Long) {
        selectedId.value = id
        viewModelScope.launch {
            // Fetch the detailed routine from the repository and update the state.
            repository.getRoutine(RoutineId(id)).collect { routine ->
                selectedRoutine.value = routine
            }
        }
    }

    /**
     * Clears the current selection, returning the user to the search list view.
     */
    fun onClearSelection() {
        selectedId.value = null
        selectedRoutine.value = null
    }

    /**
     * Handles the logic when a user finishes a routine.
     * It marks the routine as completed in the repository and then clears the selection.
     */
    fun onRoutineFinish() {
        viewModelScope.launch {
            val routineToFinish = selectedRoutine.value
            if (routineToFinish != null) {
                repository.markRoutineAsCompleted(routineToFinish.id)
                _navigationEvent.send(Unit)
            }
        }
    }

}

/**
 * Factory for creating a `SearchViewModel` with a `RoutineRepository` dependency.
 * This is necessary because the ViewModel has a non-empty constructor.
 */
class SearchViewModelFactory(
    private val repository: RoutineRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is `SearchViewModel`.
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            // If it is, create an instance and pass the repository.
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        // If it's any other class, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

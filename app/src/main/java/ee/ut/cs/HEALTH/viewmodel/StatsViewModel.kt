package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineHistoryItem
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for the Stats screen.
 *
 * This class is responsible for fetching the user's entire workout history from the
 * [RoutineRepository] and transforming it into a format suitable for the UI.
 * It groups the completed routines by date to be displayed in a list.
 *
 * @param repository The repository for accessing workout history data.
 */
class StatsViewModel(repository: RoutineRepository) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    /**
     * A state flow that emits a map of completed routines, grouped by date.
     * The key of the map is a formatted date string (e.g., "December 15, 2025"),
     * and the value is a list of all routines completed on that day.
     */
    val completedRoutinesByDate: StateFlow<Map<String, List<CompletedRoutineHistoryItem>>> =
        repository.getCompletionHistory()
            .map { historyList ->
                historyList.groupBy { item ->
                    dateFormatter.format(item.completionDate)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )
}

/**
 * Factory for creating [StatsViewModel] instances.
 *
 * This factory is necessary because [StatsViewModel] requires a [RoutineRepository]
 * dependency in its constructor.
 *
 * @param repository The repository instance to be provided to the [StatsViewModel].
 */
class StatsViewModelFactory(
    private val repository: RoutineRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

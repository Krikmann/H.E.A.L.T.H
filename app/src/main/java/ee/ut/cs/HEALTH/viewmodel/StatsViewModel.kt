// In file: ee/ut/cs/HEALTH/viewmodel/StatsViewModel.kt
package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineHistoryItem
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map // Make sure this is imported
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Updated ViewModel for the Stats screen.
 * It fetches the data and groups it by date.
 */
class StatsViewModel(repository: RoutineRepository) : ViewModel() {

    // Formatter for displaying dates nicely, e.g., "November 17, 2025"
    private val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    /**
     * This is now a Map where the key is the date (String) and the value is
     * a list of routines completed on that date.
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

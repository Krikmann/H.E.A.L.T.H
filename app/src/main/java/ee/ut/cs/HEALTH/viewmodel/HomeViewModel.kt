package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineHistoryItem
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ee.ut.cs.HEALTH.data.local.dao.DailyRoutineCount

class HomeViewModel(repository: RoutineRepository) : ViewModel() {

    // A flow that gets the most recently completed routine.
    val recentActivity: StateFlow<CompletedRoutineHistoryItem?> =
        repository.getLatestCompletedRoutine()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // A flow that gets the most recently added routine.
    val newestRoutine: StateFlow<RoutineSummary?> =
        repository.getNewestRoutineSummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // A flow that gets the most popular routine based on the completion counter.
    val mostPopularRoutine: StateFlow<RoutineSummary?> =
        repository.getMostPopularRoutineSummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weeklyActivity: StateFlow<List<DailyRoutineCount>> =
        repository.getWeeklyActivity()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class HomeViewModelFactory(
    private val repository: RoutineRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
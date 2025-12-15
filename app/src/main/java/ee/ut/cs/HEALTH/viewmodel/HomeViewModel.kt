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
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Defines the time spans available for the activity chart.
 */
enum class ChartTimeSpan {
    WEEK, MONTH
}

/**
 * ViewModel for the Home screen.
 *
 * This class is responsible for exposing data streams from the [RoutineRepository]
 * to the Home screen UI, handling user interactions like changing the chart's time span,
 * and managing the state related to user activity and profile information.
 *
 * @param repository The repository for accessing routine and profile data.
 */
class HomeViewModel(repository: RoutineRepository) : ViewModel() {

    /**
     * The model producer for the activity chart, used by the Vico charting library.
     */
    val chartModelProducer = ChartEntryModelProducer()

    private val _chartTimeSpan = MutableStateFlow(ChartTimeSpan.WEEK)
    /**
     * A state flow representing the currently selected time span for the activity chart.
     */
    val chartTimeSpan: StateFlow<ChartTimeSpan> = _chartTimeSpan

    /**
     * Updates the time span for the activity chart based on user selection.
     *
     * @param timeSpan The new time span to display ([ChartTimeSpan.WEEK] or [ChartTimeSpan.MONTH]).
     */
    fun onTimeSpanSelected(timeSpan: ChartTimeSpan) {
        _chartTimeSpan.value = timeSpan
    }

    /**
     * A dynamic data flow that provides daily routine counts based on the selected [chartTimeSpan].
     * It automatically switches between fetching weekly or monthly data.
     */
    val activityData: StateFlow<List<DailyRoutineCount>> = _chartTimeSpan
        .flatMapLatest { timeSpan ->
            when (timeSpan) {
                ChartTimeSpan.WEEK -> repository.getWeeklyActivity()
                ChartTimeSpan.MONTH -> repository.getMonthlyActivity()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * A flow that emits the most recently completed routine history item.
     * This is displayed on the home screen as "Your Recent Activity".
     */
    val recentActivity: StateFlow<CompletedRoutineHistoryItem?> =
        repository.getLatestCompletedRoutine()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A flow that emits the summary of the most recently added routine.
     */
    val newestRoutine: StateFlow<RoutineSummary?> =
        repository.getNewestRoutineSummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A flow that emits the summary of the most popular routine, based on completion count.
     */
    val mostPopularRoutine: StateFlow<RoutineSummary?> =
        repository.getMostPopularRoutineSummary()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A flow that emits a list of daily routine counts for the last 7 days.
     */
    val weeklyActivity: StateFlow<List<DailyRoutineCount>> =
        repository.getWeeklyActivity()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * A flow that provides the user's profile information.
     */
    val profile: StateFlow<ProfileEntity?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A flow that emits the total number of routines completed in the last 7 days.
     */
    val weeklyProgress: StateFlow<Int> = repository.getCompletedCountSince(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * A flow that emits the total number of routines completed in the last 30 days.
     */
    val monthlyProgress: StateFlow<Int> = repository.getCompletedCountSince(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

/**
 * Factory for creating [HomeViewModel] instances with a required [RoutineRepository] dependency.
 */
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

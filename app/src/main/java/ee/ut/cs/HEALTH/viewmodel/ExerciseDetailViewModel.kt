package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.remote.ExerciseDetailDto
import ee.ut.cs.HEALTH.domain.model.remote.ExerciseApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the state of the Exercise Detail screen.
 *
 * @property isLoading True if the data is currently being fetched from the API.
 * @property data The [ExerciseDetailDto] containing the exercise details, null if not loaded.
 * @property error A string describing an error that occurred, null if there was no error.
 */
data class ExerciseDetailState(
    val isLoading: Boolean = true,
    val data: ExerciseDetailDto? = null,
    val error: String? = null
)

/**
 * ViewModel for the Exercise Detail screen.
 *
 * This class is responsible for fetching detailed information about a specific exercise
 * from the remote [ExerciseApi] using its ID. It exposes the loading, data, and error states
 * to the UI via a [StateFlow].
 *
 * @param exerciseId The unique identifier of the exercise to fetch details for.
 * @param exerciseApi The Retrofit API service for making network requests.
 */
class ExerciseDetailViewModel(
    private val exerciseId: String,
    private val exerciseApi: ExerciseApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseDetailState())
    /**
     * The public state flow that the UI observes to get updates on exercise details.
     */
    val uiState: StateFlow<ExerciseDetailState> = _uiState

    init {
        fetchExerciseDetails()
    }

    /**
     * Fetches the exercise details from the API.
     *
     * This function launches a coroutine to perform the network request. It updates
     * the [uiState] to reflect the loading state, the successfully fetched data,
     * or any errors that occur during the process.
     */
    private fun fetchExerciseDetails() {
        viewModelScope.launch {
            _uiState.value = ExerciseDetailState(isLoading = true)
            try {
                val response = exerciseApi.getExercisesById(exerciseId)

                if (response.isSuccessful) {
                    val exerciseData = response.body()?.data
                    _uiState.value = ExerciseDetailState(isLoading = false, data = exerciseData)
                } else {
                    _uiState.value = ExerciseDetailState(
                        isLoading = false,
                        error = "API Error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ExerciseDetailState(
                    isLoading = false,
                    error = "Network Error: ${e.message}"
                )
                e.printStackTrace()
            }
        }
    }
}


/**
 * Factory for creating [ExerciseDetailViewModel] instances.
 *
 * This factory is necessary because the ViewModel has constructor parameters ([exerciseId], [exerciseApi])
 * that need to be passed during its creation.
 *
 * @param exerciseId The ID of the exercise to be passed to the ViewModel.
 * @param exerciseApi The API service instance to be passed to the ViewModel.
 */
class ExerciseDetailViewModelFactory(
    private val exerciseId: String,
    private val exerciseApi: ExerciseApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseDetailViewModel(exerciseId, exerciseApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

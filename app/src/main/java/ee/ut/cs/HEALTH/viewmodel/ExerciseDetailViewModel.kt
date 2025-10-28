package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.dto.ExerciseDetailDto
import ee.ut.cs.HEALTH.domain.model.remote.ExerciseApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExerciseDetailState(
    val isLoading: Boolean = true,
    val data: ExerciseDetailDto? = null,
    val error: String? = null
)

class ExerciseDetailViewModel(
    private val exerciseName: String,
    private val exerciseApi: ExerciseApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseDetailState())
    val uiState: StateFlow<ExerciseDetailState> = _uiState

    init {
        fetchExerciseDetails()
    }

    private fun fetchExerciseDetails() {
        viewModelScope.launch {
            _uiState.value = ExerciseDetailState(isLoading = true)
            /**
             * Fetches exercise details from the API.
             *  Directly calls the `suspend` function `searchExercises`.
             * Checks if the response was successful (HTTP 200-299).
             * If successful, it extracts the body, takes the first result from the list,
             *     and updates the UI state with the data.
             * If the response fails or a network error occurs, it updates the UI state
             *     with an appropriate error message.
             */
            try {
                val response = exerciseApi.searchExercisesByName(exerciseName)

                if (response.isSuccessful) {
                    val exercise = response.body()
                    _uiState.value = ExerciseDetailState(isLoading = false, data = exercise?.firstOrNull())
                } else {
                    _uiState.value = ExerciseDetailState(isLoading = false, error = "API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ExerciseDetailState(isLoading = false, error = "Network Error: ${e.message}")
                e.printStackTrace()
            }


        }
    }
}

class ExerciseDetailViewModelFactory(
    private val exerciseName: String,
    private val exerciseApi: ExerciseApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseDetailViewModel(exerciseName, exerciseApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
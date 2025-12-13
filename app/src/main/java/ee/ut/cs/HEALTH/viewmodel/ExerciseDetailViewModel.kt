package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.remote.ExerciseDetailDto
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
    private val exerciseId: String,
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
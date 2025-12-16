package ee.ut.cs.HEALTH.domain.model.remote

import ee.ut.cs.HEALTH.data.remote.ExerciseDetailDto

/**
 * A wrapper class for API responses that return a LIST of exercises.
 * It directly corresponds to the JSON structure: `{ "success": Boolean, "data": [ ... ] }`.
 *
 * @property success Indicates whether the API call was successful.
 * @property data A list of [ExerciseDetailDto] objects containing the exercise details.
 */
data class ExerciseListResponse(
    val success: Boolean,
    val data: List<ExerciseDetailDto>
)

/**
 * A wrapper class for API responses that return a SINGLE exercise.
 * It directly corresponds to the JSON structure: `{ "success": Boolean, "data": { ... } }`.
 *
 * @property success Indicates whether the API call was successful.
 * @property data A single [ExerciseDetailDto] object containing the details of one exercise.
 */
data class SingleExerciseResponse(
    val success: Boolean,
    val data: ExerciseDetailDto
)

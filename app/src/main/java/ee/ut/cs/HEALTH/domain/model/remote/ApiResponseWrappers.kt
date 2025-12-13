package ee.ut.cs.HEALTH.domain.model.remote

import ee.ut.cs.HEALTH.data.remote.ExerciseDetailDto

/**
 * See klass on mõeldud API vastustele, mis tagastavad NIMEKIRJA harjutusi.
 * See vastab täpselt JSON-struktuurile: { "success": true, "data": [ ... ] }
 */
data class ExerciseListResponse(
    val success: Boolean,
    val data: List<ExerciseDetailDto>
)

/**
 * See klass on mõeldud API vastustele, mis tagastavad ÜHE harjutuse.
 * See vastab täpselt JSON-struktuurile: { "success": true, "data": { ... } }
 */
data class SingleExerciseResponse(
    val success: Boolean,
    val data: ExerciseDetailDto
)
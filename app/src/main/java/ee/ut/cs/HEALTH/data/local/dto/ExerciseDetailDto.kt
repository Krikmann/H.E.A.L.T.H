package ee.ut.cs.HEALTH.data.local.dto

/**
 * Data Transfer Object (DTO) for exercise details fetched from a remote API using Retrofit.
 * The structure of this class is designed to match the API's JSON response.
 * Nullable types (?) are used for fields that might be missing from the response,
 * which makes the application more robust and prevents crashes.
 *
 */
data class ExerciseDetailDto(
    val exerciseId: String,
    val name: String,
    val imageUrl: String?,
    val equipments: List<String>,
    val bodyParts: List<String>,
    val exerciseType: String?,
    val targetMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val videoUrl: String?,
    val keywords: List<String>,
    val overview: String?,
    val instructions: List<String>,
    val exerciseTips: List<String>?,
    val variations: List<String>?,
    val relatedExerciseIds: List<String>?
)

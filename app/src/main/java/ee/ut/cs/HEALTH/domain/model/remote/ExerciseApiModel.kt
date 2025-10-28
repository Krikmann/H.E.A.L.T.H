package ee.ut.cs.HEALTH.domain.model.remote

data class ExerciseApiModel(
    val exerciseId: String,
    val name: String,
    val imageUrl: String?,
    val overview: String?,
)

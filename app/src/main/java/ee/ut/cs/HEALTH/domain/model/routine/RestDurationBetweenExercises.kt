package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

@JvmInline value class RestDurationBetweenExercisesId(val id: Int)

data class SavedRestDurationBetweenExercises(
    val id: RestDurationBetweenExercisesId,
    val restDuration: Duration,
): SavedRoutineItem

data class UpdatedRestDurationBetweenExercises(
    val id: RestDurationBetweenExercisesId,
    val restDuration: Duration,
): UpdatedRoutineItem

data class NewRestDurationBetweenExercises(
    val restDuration: Duration,
): NewRoutineItem

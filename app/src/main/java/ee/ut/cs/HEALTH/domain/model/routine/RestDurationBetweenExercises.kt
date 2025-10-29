package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

@JvmInline value class RestDurationBetweenExercisesId(val value: Long)

data class SavedRestDurationBetweenExercises(
    val id: RestDurationBetweenExercisesId,
    val restDuration: Duration,
): SavedRoutineItem {
    override fun toUpdated(): UpdatedRestDurationBetweenExercises =
        UpdatedRestDurationBetweenExercises(
            id = id,
            restDuration = restDuration
        )
}

data class UpdatedRestDurationBetweenExercises(
    val id: RestDurationBetweenExercisesId,
    val restDuration: Duration,
): UpdatedRoutineItem

data class NewRestDurationBetweenExercises(
    val restDuration: Duration,
): NewRoutineItem

fun NewRestDurationBetweenExercises.withRestDuration(d: Duration) =
    copy(restDuration = d)

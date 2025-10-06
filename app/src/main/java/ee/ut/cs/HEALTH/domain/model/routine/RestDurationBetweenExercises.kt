package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

data class RestDurationBetweenExercises(
    val id: RestDurationBetweenExercisesId,
    val restDuration: Duration,
): RoutineItem

@JvmInline value class RestDurationBetweenExercisesId(val id: Int)
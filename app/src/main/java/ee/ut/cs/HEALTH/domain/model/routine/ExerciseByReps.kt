package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

data class ExerciseByReps(
    val id: ExerciseByRepsId,
    override val exerciseDefinition: ExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val countOfRepetitions: Int,
): Exercise

@JvmInline value class ExerciseByRepsId(val id: Int)
package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

data class ExerciseByDuration(
    val id: ExerciseByDurationId,
    override val exerciseDefinition: ExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val duration: Duration,
): Exercise

@JvmInline value class ExerciseByDurationId(val id: Int)
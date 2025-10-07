package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

@JvmInline value class ExerciseByDurationId(val value: Long)

data class SavedExerciseByDuration(
    val id: ExerciseByDurationId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val duration: Duration,
): SavedExercise

data class UpdatedExerciseByDuration(
    val id: ExerciseByDurationId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val duration: Duration,
): UpdatedExercise

data class NewExerciseByDuration(
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val duration: Duration,
): NewExercise
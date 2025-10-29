package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

@JvmInline value class ExerciseByRepsId(val value: Long)

data class SavedExerciseByReps(
    val id: ExerciseByRepsId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val countOfRepetitions: Int,
): SavedExercise {
    override fun toUpdated(): UpdatedExerciseByReps =
        UpdatedExerciseByReps(
            id = id,
            exerciseDefinition = exerciseDefinition,
            recommendedRestDurationBetweenSets = recommendedRestDurationBetweenSets,
            amountOfSets = amountOfSets,
            weight = weight,
            countOfRepetitions = countOfRepetitions
        )
}

data class UpdatedExerciseByReps(
    val id: ExerciseByRepsId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val countOfRepetitions: Int,
): UpdatedExercise

data class NewExerciseByReps(
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val countOfRepetitions: Int,
): NewExercise

fun NewExerciseByReps.withCountOfRepetitions(count: Int) = copy(countOfRepetitions = count)
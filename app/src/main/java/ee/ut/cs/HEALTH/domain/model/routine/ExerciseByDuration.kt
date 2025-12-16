package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

/**
 * A type-safe value class for the unique identifier of a duration-based exercise item.
 *
 * @property value The underlying ID value.
 */
@JvmInline value class ExerciseByDurationId(val value: Long)

/**
 * Represents a duration-based exercise that has been saved to persistent storage.
 *
 * It implements [SavedExercise] and contains a persistent [id].
 *
 * @property id The unique identifier for this saved exercise instance.
 * @property exerciseDefinition The definition of the exercise being performed.
 * @property recommendedRestDurationBetweenSets The rest time to take after each set.
 * @property amountOfSets The total number of sets for this exercise.
 * @property weight The weight to be used for this exercise, if applicable.
 * @property duration The duration for which the exercise should be performed per set.
 */
data class SavedExerciseByDuration(
    val id: ExerciseByDurationId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val duration: Duration,
): SavedExercise {
    /**
     * Converts this saved item into an [UpdatedExerciseByDuration] instance,
     * typically used when starting an editing session for a routine.
     */
    override fun toUpdated(): UpdatedExerciseByDuration =
        UpdatedExerciseByDuration(
            id = id,
            exerciseDefinition = exerciseDefinition,
            recommendedRestDurationBetweenSets = recommendedRestDurationBetweenSets,
            amountOfSets = amountOfSets,
            weight = weight,
            duration = duration
        )
}

/**
 * Represents a duration-based exercise that is part of a routine currently being edited.
 *
 * It implements [UpdatedExercise] and holds a reference to the original [id].
 *
 * @property id The persistent identifier of the original saved item.
 * @property exerciseDefinition The definition of the exercise.
 * @property recommendedRestDurationBetweenSets The rest time after each set.
 * @property amountOfSets The total number of sets.
 * @property weight The weight to be used, if applicable.
 * @property duration The duration of the exercise per set.
 */
data class UpdatedExerciseByDuration(
    val id: ExerciseByDurationId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val duration: Duration,
): UpdatedExercise

/**
 * Represents a new duration-based exercise that has been created but not yet saved.
 *
 * It has no persistent ID and implements [NewExercise].
 *
 * @property exerciseDefinition The definition of the exercise.
 * @property recommendedRestDurationBetweenSets The rest time after each set.
 * @property amountOfSets The total number of sets.
 * @property weight The weight to be used, if applicable.
 * @property duration The duration of the exercise per set.
 */
data class NewExerciseByDuration(
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val duration: Duration,
): NewExercise

/** Returns a new [NewExerciseByDuration] with an updated duration. */
fun NewExerciseByDuration.withDuration(duration: Duration) = copy(duration = duration)

/** Returns a new [NewExerciseByDuration] with an updated number of sets. */
fun NewExerciseByDuration.withAmountOfSets(sets: Int) = copy(amountOfSets = sets)

/** Returns a new [NewExerciseByDuration] with an updated weight. */
fun NewExerciseByDuration.withWeight(weight: Weight?) = copy(weight = weight)

/** Returns a new [NewExerciseByDuration] with an updated exercise definition. */
fun NewExerciseByDuration.withExerciseDefinition(exerciseDefinition: SavedExerciseDefinition) =
    copy(exerciseDefinition = exerciseDefinition)

/** Returns a new [NewExerciseByDuration] with an updated rest duration between sets. */
fun NewExerciseByDuration.withRecommendedRestDurationBetweenSets(duration: Duration) =
    copy(recommendedRestDurationBetweenSets = duration)


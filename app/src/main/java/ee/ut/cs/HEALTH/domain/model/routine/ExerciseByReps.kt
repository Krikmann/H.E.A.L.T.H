package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

/**
 * A type-safe value class for the unique identifier of a reps-based exercise item.
 *
 * @property value The underlying ID value.
 */
@JvmInline value class ExerciseByRepsId(val value: Long)

/**
 * Represents a repetition-based exercise that has been saved to persistent storage.
 *
 * It implements [SavedExercise] and contains a persistent [id].
 *
 * @property id The unique identifier for this saved exercise instance.
 * @property exerciseDefinition The definition of the exercise being performed.
 * @property recommendedRestDurationBetweenSets The rest time to take after each set.
 * @property amountOfSets The total number of sets for this exercise.
 * @property weight The weight to be used for this exercise, if applicable.
 * @property countOfRepetitions The number of repetitions to perform per set.
 */
data class SavedExerciseByReps(
    val id: ExerciseByRepsId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val countOfRepetitions: Int,
): SavedExercise {
    /**
     * Converts this saved item into an [UpdatedExerciseByReps] instance,
     * typically used when starting an editing session for a routine.
     */
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

/**
 * Represents a repetition-based exercise that is part of a routine currently being edited.
 *
 * It implements [UpdatedExercise] and holds a reference to the original [id].
 *
 * @property id The persistent identifier of the original saved item.
 * @property exerciseDefinition The definition of the exercise.
 * @property recommendedRestDurationBetweenSets The rest time after each set.
 * @property amountOfSets The total number of sets.
 * @property weight The weight to be used, if applicable.
 * @property countOfRepetitions The number of repetitions per set.
 */
data class UpdatedExerciseByReps(
    val id: ExerciseByRepsId,
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val countOfRepetitions: Int,
): UpdatedExercise

/**
 * Represents a new repetition-based exercise that has been created but not yet saved.
 *
 * It has no persistent ID and implements [NewExercise].
 *
 * @property exerciseDefinition The definition of the exercise.
 * @property recommendedRestDurationBetweenSets The rest time after each set.
 * @property amountOfSets The total number of sets.
 * @property weight The weight to be used, if applicable.
 * @property countOfRepetitions The number of repetitions per set.
 */
data class NewExerciseByReps(
    override val exerciseDefinition: SavedExerciseDefinition,
    override val recommendedRestDurationBetweenSets: Duration,
    override val amountOfSets: Int,
    override val weight: Weight?,
    val countOfRepetitions: Int,
): NewExercise

/** Returns a new [NewExerciseByReps] with an updated repetition count. */
fun NewExerciseByReps.withCountOfRepetitions(count: Int) = copy(countOfRepetitions = count)

/** Returns a new [NewExerciseByReps] with an updated number of sets. */
fun NewExerciseByReps.withAmountOfSets(sets: Int) = copy(amountOfSets = sets)

/** Returns a new [NewExerciseByReps] with an updated weight. */
fun NewExerciseByReps.withWeight(weight: Weight?) = copy(weight = weight)

/** Returns a new [NewExerciseByReps] with an updated exercise definition. */
fun NewExerciseByReps.withExerciseDefinition(exerciseDefinition: SavedExerciseDefinition) =
    copy(exerciseDefinition = exerciseDefinition)

/** Returns a new [NewExerciseByReps] with an updated rest duration between sets. */
fun NewExerciseByReps.withRecommendedRestDurationBetweenSets(duration: Duration) =
    copy(recommendedRestDurationBetweenSets = duration)


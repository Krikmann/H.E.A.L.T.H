package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

/**
 * A sealed interface representing a generic exercise, which is a type of [RoutineItem].
 *
 * This interface defines the common properties that all exercises must have, regardless
 * of whether they are measured by repetitions or duration.
 *
 * @property exerciseDefinition The core definition of the exercise (e.g., "Push-up").
 * @property recommendedRestDurationBetweenSets The suggested rest time to take after each set.
 * @property amountOfSets The total number of sets for this exercise.
 * @property weight The weight to be used for this exercise, if applicable.
 */
sealed interface Exercise: RoutineItem {
    val exerciseDefinition: SavedExerciseDefinition
    val recommendedRestDurationBetweenSets: Duration
    val amountOfSets: Int
    val weight: Weight?
}

/**
 * A sealed interface representing a newly created exercise that has not been saved yet.
 * It inherits from both [Exercise] and [NewRoutineItem].
 */
sealed interface NewExercise: Exercise, NewRoutineItem

/**
 * A sealed interface representing an exercise that has been saved to persistent storage.
 * It inherits from both [Exercise] and [SavedRoutineItem].
 */
sealed interface SavedExercise: Exercise, SavedRoutineItem

/**
 * A sealed interface representing an exercise that is part of a routine currently being edited.
 * It inherits from both [Exercise] and [UpdatedRoutineItem].
 */
sealed interface UpdatedExercise: Exercise, UpdatedRoutineItem

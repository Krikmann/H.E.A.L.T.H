package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

sealed interface Exercise: RoutineItem {
    val exerciseDefinition: SavedExerciseDefinition
    val recommendedRestDurationBetweenSets: Duration
    val amountOfSets: Int
    val weight: Weight?
}

sealed interface NewExercise: Exercise, NewRoutineItem
sealed interface SavedExercise: Exercise, SavedRoutineItem
sealed interface UpdatedExercise: Exercise, UpdatedRoutineItem
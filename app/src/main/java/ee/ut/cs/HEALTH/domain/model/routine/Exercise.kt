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

inline fun <reified T: NewExercise> T.withExerciseDefinition(
    exerciseDefinition: SavedExerciseDefinition
): T = when (this) {
    is NewExerciseByReps -> copy(exerciseDefinition = exerciseDefinition)
    is NewExerciseByDuration -> copy(exerciseDefinition = exerciseDefinition)
} as T

inline fun <reified T : NewExercise> T.withRecommendedRestDurationBetweenSets(
    d: Duration
): T = when (this) {
    is NewExerciseByReps -> copy(recommendedRestDurationBetweenSets = d)
    is NewExerciseByDuration -> copy(recommendedRestDurationBetweenSets = d)
} as T

inline fun <reified T : NewExercise> T.withAmountOfSets(sets: Int): T = when (this) {
    is NewExerciseByReps -> copy(amountOfSets = sets)
    is NewExerciseByDuration -> copy(amountOfSets = sets)
} as T

inline fun <reified T : NewExercise> T.withWeight(w: Weight?): T = when (this) {
    is NewExerciseByReps -> copy(weight = w)
    is NewExerciseByDuration -> copy(weight = w)
} as T
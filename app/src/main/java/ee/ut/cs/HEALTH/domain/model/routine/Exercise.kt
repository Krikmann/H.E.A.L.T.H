package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

sealed interface Exercise: RoutineItem {
    val exerciseDefinition: ExerciseDefinition
    val recommendedRestDurationBetweenSets: Duration
    val amountOfSets: Int
    val weight: Weight?
}
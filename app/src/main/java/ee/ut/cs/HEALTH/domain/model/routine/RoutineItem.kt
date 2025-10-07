package ee.ut.cs.HEALTH.domain.model.routine

sealed interface RoutineItem {
}

sealed interface SavedRoutineItem: RoutineItem
sealed interface UpdatedRoutineItem: RoutineItem
sealed interface NewRoutineItem: RoutineItem

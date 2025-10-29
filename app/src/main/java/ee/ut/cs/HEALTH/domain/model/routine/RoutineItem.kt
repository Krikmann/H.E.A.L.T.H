package ee.ut.cs.HEALTH.domain.model.routine

sealed interface RoutineItem {
}

sealed interface SavedRoutineItem: RoutineItem {
    fun toUpdated(): UpdatedRoutineItem
}
sealed interface UpdatedRoutineItem: RoutineItem
sealed interface NewRoutineItem: RoutineItem

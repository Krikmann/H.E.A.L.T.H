package ee.ut.cs.HEALTH.domain.model.routine

/**
 * A sealed interface representing a single item within a workout routine.
 * This is the base type for all possible items, such as an exercise or a rest period.
 */
sealed interface RoutineItem

/**
 * A sealed interface representing a [RoutineItem] that has been saved to the database.
 * These items have a persistent ID and represent the canonical state of a routine's component.
 */
sealed interface SavedRoutineItem : RoutineItem {
    /**
     * Converts a saved item into an updatable version.
     * This is used when an existing routine is being edited.
     *
     * @return An [UpdatedRoutineItem] instance corresponding to this saved item.
     */
    fun toUpdated(): UpdatedRoutineItem
}

/**
 * A sealed interface representing a [RoutineItem] that is part of a routine currently being edited.
 * This can be either an original [SavedRoutineItem] that has been modified or a completely new item.
 */
sealed interface UpdatedRoutineItem : RoutineItem

/**
 * A sealed interface representing a [RoutineItem] that has been newly created in the UI
 * and has not yet been saved to the database. These items do not have a persistent ID.
 */
sealed interface NewRoutineItem : RoutineItem

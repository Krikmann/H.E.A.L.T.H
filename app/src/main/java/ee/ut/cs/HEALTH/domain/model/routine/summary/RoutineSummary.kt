package ee.ut.cs.HEALTH.domain.model.routine.summary

import ee.ut.cs.HEALTH.domain.model.routine.RoutineId

/**
 * A lightweight, summarized representation of a workout routine.
 *
 * This data class is designed for displaying routines in lists (e.g., on the search screen),
 * providing only the essential information needed for a brief overview, thus improving performance
 * by not loading the full list of routine items.
 *
 * @property id The unique identifier of the routine.
 * @property name The user-defined name of the routine.
 * @property description An optional, short description of the routine.
 * @property completionCount The total number of times this routine has been completed by the user.
 */
data class RoutineSummary(
    val id: RoutineId,
    val name: String,
    val description: String?,
    val completionCount: Int
)

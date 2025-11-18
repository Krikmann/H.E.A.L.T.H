package ee.ut.cs.HEALTH.domain.model.routine.summary

import ee.ut.cs.HEALTH.domain.model.routine.RoutineId

data class RoutineSummary(
    val id: RoutineId,
    val name: String,
    val description: String?,
    val completionCount: Int
)

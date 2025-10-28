// Asukoht: .../domain/model/routine/EnrichedRoutineItem.kt

package ee.ut.cs.HEALTH.domain.model.routine

import ee.ut.cs.HEALTH.data.local.dto.ExerciseDetailDto

/**
 * See klass ühendab sammu andmebaasist (RoutineItem)
 * ja sellele vastavad rikastatud andmed API-st (ExerciseDetailDto).
 */
data class EnrichedRoutineItem(
    val routineItem: RoutineItem,
    val details: ExerciseDetailDto?
)

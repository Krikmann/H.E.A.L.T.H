package ee.ut.cs.HEALTH.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemEntity

data class RoutineDto(
    @Embedded
    val routine: RoutineEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "routineId",
        entity = RoutineItemEntity::class
    )
    val routineItems: List<RoutineItemDto>
)

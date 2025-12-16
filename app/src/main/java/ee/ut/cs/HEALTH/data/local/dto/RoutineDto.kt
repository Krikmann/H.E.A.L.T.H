package ee.ut.cs.HEALTH.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemEntity
/**
 * A Data Transfer Object (DTO) that represents a complete routine, including its main entity
 * and all of its associated items.
 *
 * This class is used by Room to construct a full routine object from the database in a single,
 * efficient query. It combines the parent [RoutineEntity] with a list of all its child
 * [RoutineItemDto] objects.
 *
 * @property routine The main [RoutineEntity] object, embedded directly into this DTO.
 * @property routineItems A list of [RoutineItemDto] objects related to this routine.
 *                        The relationship is defined by matching the `id` of the parent
 *                        [RoutineEntity] with the `routineId` foreign key in the child
 *                        [RoutineItemEntity].
 */
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

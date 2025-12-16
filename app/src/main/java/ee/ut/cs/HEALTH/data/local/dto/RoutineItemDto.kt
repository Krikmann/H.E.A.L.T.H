package ee.ut.cs.HEALTH.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import ee.ut.cs.HEALTH.data.local.entities.ExerciseEntity
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemEntity

/**
 * A Data Transfer Object (DTO) that represents a complete routine item by combining
 * data from multiple database tables.
 *
 * This class uses Room's `@Relation` to fetch related data in a single query. It fetches
 * the base [RoutineItemEntity] and then, based on the item's type, conditionally populates
 * either the [exercise] or [restDurationBetweenExercises] field.
 *
 * @property routineItem The core [RoutineItemEntity] embedded directly into this DTO.
 * @property exercise The detailed [ExerciseDto] if this item is an exercise. This field will be
 *                    `null` if the item is a rest period. The relation is based on the
 *                    matching primary key (`id`).
 * @property restDurationBetweenExercises The [RestDurationBetweenExercisesEntity] if this item
 *                                        is a rest period. This field will be `null` if the
 *                                        item is an exercise. The relation is based on the
 *                                        matching primary key (`id`).
 */
data class RoutineItemDto(
    @Embedded
    val routineItem: RoutineItemEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = ExerciseEntity::class
    )
    val exercise: ExerciseDto?,


    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = RestDurationBetweenExercisesEntity::class
    )
    val restDurationBetweenExercises: RestDurationBetweenExercisesEntity?,
)

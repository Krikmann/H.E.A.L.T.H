package ee.ut.cs.HEALTH.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseEntity

/**
 * A Data Transfer Object (DTO) that represents a complete exercise by combining
 * data from multiple related database tables.
 *
 * This class is used by Room to efficiently construct a full exercise object with all its details
 * in a single query. It brings together the common exercise properties, its definition,
 * and the specific data for either a reps-based or duration-based exercise.
 *
 * @property exercise The core [ExerciseEntity] containing common properties like sets, rest, and weight.
 *                    This is embedded directly into the DTO.
 * @property exerciseDefinition The [ExerciseDefinitionEntity] that this exercise references.
 *                              The relation is based on the `exerciseDefinitionId` foreign key.
 * @property byReps The [ExerciseByRepsEntity] if this exercise is repetition-based. This field will be
 *                  `null` for duration-based exercises. The relation is based on the matching primary key (`id`).
 * @property byDuration The [ExerciseByDurationEntity] if this exercise is duration-based. This field will be
 *                      `null` for reps-based exercises. The relation is based on the matching primary key (`id`).
 */
data class ExerciseDto(
    @Embedded
    val exercise: ExerciseEntity,

    @Relation(
        parentColumn = "exerciseDefinitionId",
        entityColumn = "id",
        entity = ExerciseDefinitionEntity::class
    )
    val exerciseDefinition: ExerciseDefinitionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = ExerciseByRepsEntity::class
    )
    val byReps: ExerciseByRepsEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = ExerciseByDurationEntity::class
    )
    val byDuration: ExerciseByDurationEntity?
)

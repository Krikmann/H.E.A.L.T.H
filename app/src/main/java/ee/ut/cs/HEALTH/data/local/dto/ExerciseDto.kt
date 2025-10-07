package ee.ut.cs.HEALTH.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseEntity

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

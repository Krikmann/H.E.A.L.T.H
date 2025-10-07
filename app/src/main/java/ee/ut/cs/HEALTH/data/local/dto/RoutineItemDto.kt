package ee.ut.cs.HEALTH.data.local.dto

import androidx.room.Embedded
import androidx.room.Relation
import ee.ut.cs.HEALTH.data.local.entities.ExerciseEntity
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemEntity

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

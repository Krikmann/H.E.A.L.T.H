package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.RESTRICT,
        )
    ],
    indices = [
        Index("exerciseDefinitionId"),
        Index("type"),
    ]
)
data class ExerciseEntity(
    @PrimaryKey
    val id: RoutineItemId,
    val exerciseDefinitionId: ExerciseDefinitionId,
    val type: ExerciseType,
    val recommendedRestDurationBetweenSetsInSeconds: Long,
    val amountOfSets: Int,
    val weightInKg: Double?,
)

enum class ExerciseType {
    DURATION,
    REPS,
}
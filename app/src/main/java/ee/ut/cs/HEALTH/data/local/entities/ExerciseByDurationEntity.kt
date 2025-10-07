package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises_by_duration",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class ExerciseByDurationEntity(
    @PrimaryKey
    val id: RoutineItemId,
    val durationInSeconds: Long
)

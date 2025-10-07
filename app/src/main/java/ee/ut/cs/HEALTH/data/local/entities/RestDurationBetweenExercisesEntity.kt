package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "rest_durations_between_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RestDurationBetweenExercisesEntity(
    @PrimaryKey
    val id: RoutineItemId,
    val durationInSeconds: Long
)
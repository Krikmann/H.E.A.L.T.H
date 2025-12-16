package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents the specific data for a duration-based exercise in the database.
 *
 * This entity has a one-to-one relationship with [ExerciseEntity]. The primary key [id]
 * of this entity is also a foreign key that references the [ExerciseEntity.id].
 * This structure is used when an [ExerciseEntity] has its `type` set to [ExerciseType.DURATION].
 *
 * @property id The unique identifier for this exercise, which also serves as a foreign key
 *              to the corresponding [ExerciseEntity]. If the parent exercise is deleted,
 *              this entry is also deleted (CASCADE).
 * @property durationInSeconds The duration of the exercise per set, stored in whole seconds.
 */
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


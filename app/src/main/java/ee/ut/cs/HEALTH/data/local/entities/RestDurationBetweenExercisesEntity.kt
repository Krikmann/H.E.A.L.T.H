package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents the specific data for a rest period between exercises in the database.
 *
 * This entity has a one-to-one relationship with [RoutineItemEntity]. The primary key [id]
 * of this entity is also a foreign key that references the [RoutineItemEntity.id].
 * This structure is used when a [RoutineItemEntity] has its `type` set to [RoutineItemType.REST].
 *
 * @property id The unique identifier for this rest duration, which also serves as a foreign key
 *              to the corresponding [RoutineItemEntity]. If the parent routine item is deleted,
 *              this entry is also deleted (CASCADE).
 * @property durationInSeconds The duration of the rest period, stored in whole seconds.
 */
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

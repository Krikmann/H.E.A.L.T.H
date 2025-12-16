package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A type-safe value class for the unique identifier of a routine item.
 *
 * @property value The underlying ID value.
 */
@JvmInline value class RoutineItemId(val value: Long)

/**
 * Represents a single item within a routine in the database.
 *
 * This entity acts as a link between a [RoutineEntity] and the specific item data
 * (e.g., [ExerciseEntity] or [RestDurationBetweenExercisesEntity]). It establishes the
 * order of items within a routine using the [position] field.
 *
 * @property id The unique primary key for this routine item.
 * @property routineId A foreign key referencing the [RoutineEntity] this item belongs to.
 *                     If the parent routine is deleted, all its items are also deleted (CASCADE).
 * @property type The type of this item, which determines whether it's an exercise or a rest period.
 * @property position The zero-based index of this item within the routine, defining the workout sequence.
 */
@Entity(
    tableName = "routine_items",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["routineId", "position"]),
        Index("type"),
    ]
)
data class RoutineItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: RoutineItemId = RoutineItemId(0),
    val routineId: RoutineId,
    val type: RoutineItemType,
    val position: Int,
)

/**
 * Defines the possible types of a [RoutineItemEntity].
 * This is used to differentiate between an actual exercise and a rest period between exercises.
 */
enum class RoutineItemType {
    /** Indicates that the routine item is an exercise. */
    EXERCISE,
    /** Indicates that the routine item is a rest period. */
    REST,
}

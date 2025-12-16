package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a record of a completed workout routine in the database.
 *
 * This entity stores a history of every time a user finishes a routine. It links
 * back to the specific [RoutineEntity] that was completed and stores the timestamp
 * of the completion.
 *
 * @property id The unique primary key for this completion record, automatically generated.
 * @property routineId A foreign key referencing the [RoutineEntity] that was completed.
 *                     If the parent routine is deleted, all its completion records are also deleted (CASCADE).
 * @property completionDate The exact date and time when the routine was marked as completed.
 * @property completionNote An optional user-provided note or comment about the workout session.
 */
@Entity(
    tableName = "completed_routines",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId"])]
)
data class CompletedRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: RoutineId,
    val completionDate: Date,
    val completionNote: String?
)

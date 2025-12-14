package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

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
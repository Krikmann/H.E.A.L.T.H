package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@JvmInline value class RoutineId(val value: Long)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: RoutineId = RoutineId(0),
    val name: String,
    val description: String?,
    val counter: Int = 0
)

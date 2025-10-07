package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: RoutineId,
    val name: String,
    val description: String?,
    val counter: Int = 0

)

@JvmInline value class RoutineId(val id: Int)
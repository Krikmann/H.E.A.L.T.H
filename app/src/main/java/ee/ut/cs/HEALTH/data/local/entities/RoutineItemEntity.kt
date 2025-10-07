package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


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
    @PrimaryKey
    val id: RoutineItemId,
    val routineId: RoutineId,
    val type: RoutineItemType,
    val position: Int,
)

enum class RoutineItemType {
    EXERCISE,
    REST,
}

@JvmInline value class RoutineItemId(val id: Int)
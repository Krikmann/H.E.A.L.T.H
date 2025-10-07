package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises_by_reps",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class ExerciseByRepsEntity(
    @PrimaryKey
    val id: RoutineItemId,
    val countOfRepetitions: Int
)
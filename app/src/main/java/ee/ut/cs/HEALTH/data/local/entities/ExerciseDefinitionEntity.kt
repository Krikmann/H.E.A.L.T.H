package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_definitions")
data class ExerciseDefinitionEntity(
    @PrimaryKey
    val id: ExerciseDefinitionId,
    val name: String
)

@JvmInline value class ExerciseDefinitionId(val value: String)
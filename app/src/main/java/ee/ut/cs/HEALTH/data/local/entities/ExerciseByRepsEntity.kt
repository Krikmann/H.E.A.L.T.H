package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises_by_reps")
data class ExerciseByRepsEntity(
    @PrimaryKey val id: Int,
    val routineId: Int,
    val exerciseDefinitionId: Int,
    val restDurationBetweenSetsMillis: Long,
    val amountOfSets: Int,
    val weightInKg: Double?,
    val countOfRepetitions: Int
)
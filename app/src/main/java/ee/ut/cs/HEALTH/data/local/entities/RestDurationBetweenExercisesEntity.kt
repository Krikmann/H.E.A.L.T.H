package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rest_durations_between_exercises")
data class RestDurationBetweenExercisesEntity(
    @PrimaryKey val id: Int,
    val routineId: Int,
    val restDurationMillis: Long
)
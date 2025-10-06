package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.*
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Insert
    suspend fun insertExerciseByReps(exercise: ExerciseByRepsEntity)

    @Insert
    suspend fun insertExerciseByDuration(exercise: ExerciseByDurationEntity)

    @Insert
    suspend fun insertRest(rest: RestDurationBetweenExercisesEntity)

    @Insert
    suspend fun insertExerciseDefinition(definition: ExerciseDefinitionEntity)

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutine(id: Int): RoutineEntity
}
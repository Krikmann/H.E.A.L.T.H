package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.*
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<RoutineEntity>>


    // delete functions
    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()

    @Query("DELETE FROM exercises_by_reps")
    suspend fun deleteAllExercisesByReps()

    @Query("DELETE FROM exercises_by_duration")
    suspend fun deleteAllExercisesByDuration()

    @Query("DELETE FROM rest_durations_between_exercises")
    suspend fun deleteAllRests()

    @Query("DELETE FROM exercise_definitions")
    suspend fun deleteAllExerciseDefinitions()

    @Query("UPDATE routines SET counter = counter + 1 WHERE id = :routineId")
    suspend fun incrementRoutineCounter(routineId: Int)
}
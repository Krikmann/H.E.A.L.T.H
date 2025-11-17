package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.*
import ee.ut.cs.HEALTH.data.local.dto.RoutineItemDto
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionId
import ee.ut.cs.HEALTH.data.local.entities.ExerciseEntity
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineId
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemEntity
import kotlinx.coroutines.flow.Flow
import org.jetbrains.annotations.ApiStatus

@Dao
interface RoutineDao {
    @Insert
    suspend fun insertRoutine(entity: RoutineEntity): Long

    @Insert
    suspend fun insertRoutineItem(entity: RoutineItemEntity): Long

    @Insert
    suspend fun insertExercise(entity: ExerciseEntity): Long

    @Insert
    suspend fun insertExerciseByReps(entity: ExerciseByRepsEntity): Long

    @Insert
    suspend fun insertExerciseByDuration(entity: ExerciseByDurationEntity): Long

    @Insert
    suspend fun insertRestDurationBetweenExercises(entity: RestDurationBetweenExercisesEntity): Long

    @Upsert
    suspend fun upsertRoutine(routine: RoutineEntity)

    @Upsert
    suspend fun upsertRoutineItem(item: RoutineItemEntity)

    @Upsert
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Upsert
    suspend fun upsertExerciseByReps(exercise: ExerciseByRepsEntity)

    @Upsert
    suspend fun upsertExerciseByDuration(exercise: ExerciseByDurationEntity)

    @Upsert
    suspend fun upsertRest(rest: RestDurationBetweenExercisesEntity)

    @Upsert
    suspend fun upsertExerciseDefinition(definition: ExerciseDefinitionEntity)

    @Deprecated(
        message = "For internal use only, use getRoutine instead",
        replaceWith = ReplaceWith("getRoutine(id)"),
        level = DeprecationLevel.WARNING
    )
    @ApiStatus.Internal
    @Query("SELECT * FROM routines WHERE id = :id")
    fun getRoutineEntityFlow(id: RoutineId): Flow<RoutineEntity?>

    @Query("SELECT * FROM routine_items WHERE routineId = :routineId ORDER BY position ASC")
    fun getRoutineItemsOrderedFlow(routineId: RoutineId): Flow<List<RoutineItemDto>>

    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM exercise_definitions ORDER BY name")
    fun getAllExerciseDefinitions(): Flow<List<ExerciseDefinitionEntity>>

    // delete functions
    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()

    @Query("DELETE FROM routine_items")
    suspend fun deleteAllRoutineItems()

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    @Query("DELETE FROM exercises_by_reps")
    suspend fun deleteAllExercisesByReps()

    @Query("DELETE FROM exercises_by_duration")
    suspend fun deleteAllExercisesByDuration()

    @Query("DELETE FROM rest_durations_between_exercises")
    suspend fun deleteAllRests()

    @Query("DELETE FROM exercise_definitions")
    suspend fun deleteAllExerciseDefinitions()

    @Query("UPDATE routines SET counter = counter + 1 WHERE id = :routineId")
    suspend fun incrementRoutineCounter(routineId: RoutineId)

    @Query("SELECT * FROM exercise_definitions WHERE id = :exerciseId")
    fun getExerciseDefinition(exerciseId: ExerciseDefinitionId?): Flow<ExerciseDefinitionEntity?>



    @Query("SELECT * FROM routines WHERE name LIKE '%' || :query || '%'")
    fun searchRoutines(query: String): Flow<List<RoutineEntity>>

    /**
     * Increments the completion counter for a specific routine.
     * @param routineId The ID of the routine to update.
     */
    @Query("UPDATE routines SET counter = counter + 1 WHERE id = :routineId")
    suspend fun incrementRoutineCompletionCounter(routineId: RoutineId)


}
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

/**
 * Data Access Object (DAO) for all routine-related database operations.
 *
 * This interface defines the methods for interacting with the routine-related tables in the
 * Room database. It includes functions for inserting, updating, deleting, and querying data.
 */
@Dao
interface RoutineDao {

    /** Inserts a single [RoutineEntity] and returns its new ID. */
    @Insert
    suspend fun insertRoutine(entity: RoutineEntity): Long

    /** Inserts a single [RoutineItemEntity] and returns its new ID. */
    @Insert
    suspend fun insertRoutineItem(entity: RoutineItemEntity): Long

    /** Inserts a single [ExerciseEntity] and returns its new ID. */
    @Insert
    suspend fun insertExercise(entity: ExerciseEntity): Long

    /** Inserts a single [ExerciseByRepsEntity] and returns its new ID. */
    @Insert
    suspend fun insertExerciseByReps(entity: ExerciseByRepsEntity): Long

    /** Inserts a single [ExerciseByDurationEntity] and returns its new ID. */
    @Insert
    suspend fun insertExerciseByDuration(entity: ExerciseByDurationEntity): Long

    /** Inserts a single [RestDurationBetweenExercisesEntity] and returns its new ID. */
    @Insert
    suspend fun insertRestDurationBetweenExercises(entity: RestDurationBetweenExercisesEntity): Long

    /** Inserts or updates a [RoutineEntity]. */
    @Upsert
    suspend fun upsertRoutine(routine: RoutineEntity)

    /** Inserts or updates a [RoutineItemEntity]. */
    @Upsert
    suspend fun upsertRoutineItem(item: RoutineItemEntity)

    /** Inserts or updates an [ExerciseEntity]. */
    @Upsert
    suspend fun upsertExercise(exercise: ExerciseEntity)

    /** Inserts or updates an [ExerciseByRepsEntity]. */
    @Upsert
    suspend fun upsertExerciseByReps(exercise: ExerciseByRepsEntity)

    /** Inserts or updates an [ExerciseByDurationEntity]. */
    @Upsert
    suspend fun upsertExerciseByDuration(exercise: ExerciseByDurationEntity)

    /** Inserts or updates a [RestDurationBetweenExercisesEntity]. */
    @Upsert
    suspend fun upsertRest(rest: RestDurationBetweenExercisesEntity)

    /** Inserts or updates an [ExerciseDefinitionEntity]. */
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

    /**
     * Retrieves all items for a specific routine, ordered by their position.
     * This is a transactional query that fetches related data for each item.
     * @param routineId The ID of the parent routine.
     * @return A [Flow] emitting a list of [RoutineItemDto] objects.
     */
    @Transaction
    @Query("SELECT * FROM routine_items WHERE routineId = :routineId ORDER BY position ASC")
    fun getRoutineItemsOrderedFlow(routineId: RoutineId): Flow<List<RoutineItemDto>>

    /**
     * Retrieves all routines from the database.
     * @return A [Flow] emitting a list of all [RoutineEntity] objects.
     */
    @Query("SELECT * FROM routines")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    /**
     * Retrieves all exercise definitions from the database, ordered alphabetically by name.
     * @return A [Flow] emitting a list of all [ExerciseDefinitionEntity] objects.
     */
    @Query("SELECT * FROM exercise_definitions ORDER BY name")
    fun getAllExerciseDefinitions(): Flow<List<ExerciseDefinitionEntity>>

    /** Deletes all records from the `routines` table. */
    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()

    /** Deletes all records from the `routine_items` table. */
    @Query("DELETE FROM routine_items")
    suspend fun deleteAllRoutineItems()

    /** Deletes all records from the `exercises` table. */
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    /** Deletes all records from the `exercises_by_reps` table. */
    @Query("DELETE FROM exercises_by_reps")
    suspend fun deleteAllExercisesByReps()

    /** Deletes all records from the `exercises_by_duration` table. */
    @Query("DELETE FROM exercises_by_duration")
    suspend fun deleteAllExercisesByDuration()

    /** Deletes all records from the `rest_durations_between_exercises` table. */
    @Query("DELETE FROM rest_durations_between_exercises")
    suspend fun deleteAllRests()

    /** Deletes all records from the `exercise_definitions` table. */
    @Query("DELETE FROM exercise_definitions")
    suspend fun deleteAllExerciseDefinitions()

    /**
     * Retrieves a single exercise definition by its ID.
     * @param exerciseId The ID of the exercise definition to retrieve.
     * @return A [Flow] emitting the [ExerciseDefinitionEntity] or null if not found.
     */
    @Query("SELECT * FROM exercise_definitions WHERE id = :exerciseId")
    fun getExerciseDefinition(exerciseId: ExerciseDefinitionId?): Flow<ExerciseDefinitionEntity?>

    /**
     * Searches for routines where the name contains the given query string.
     * @param query The text to search for within routine names.
     * @return A [Flow] emitting a list of matching [RoutineEntity] objects.
     */
    @Query("SELECT * FROM routines WHERE name LIKE '%' || :query || '%'")
    fun searchRoutines(query: String): Flow<List<RoutineEntity>>

    /**
     * Increments the completion counter for a specific routine by one.
     * @param routineId The ID of the routine to update.
     */
    @Query("UPDATE routines SET counter = counter + 1 WHERE id = :routineId")
    suspend fun incrementRoutineCompletionCounter(routineId: RoutineId)

    /**
     * Finds the routine with the highest 'counter' value.
     * @return A [Flow] emitting the most popular [RoutineEntity], or null if the table is empty.
     */
    @Query("SELECT * FROM routines ORDER BY counter DESC LIMIT 1")
    fun getMostPopularRoutine(): Flow<RoutineEntity?>

    /**
     * Finds the most recently added routine (by the highest ID).
     * @return A [Flow] emitting the newest [RoutineEntity], or null if the table is empty.
     */
    @Query("SELECT * FROM routines ORDER BY id DESC LIMIT 1")
    fun getNewestRoutine(): Flow<RoutineEntity?>
}

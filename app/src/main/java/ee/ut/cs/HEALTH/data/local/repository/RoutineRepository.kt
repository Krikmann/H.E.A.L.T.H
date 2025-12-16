package ee.ut.cs.HEALTH.data.local.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.dto.RoutineDto
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemId
import ee.ut.cs.HEALTH.data.mapper.newRoutineItemToEntity
import ee.ut.cs.HEALTH.data.mapper.toDomain
import ee.ut.cs.HEALTH.data.mapper.toDomainSummary
import ee.ut.cs.HEALTH.data.local.entities.RoutineId as EntityRoutineId
import ee.ut.cs.HEALTH.data.mapper.toEntity
import ee.ut.cs.HEALTH.data.mapper.toExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.mapper.toExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.mapper.toExerciseEntity
import ee.ut.cs.HEALTH.data.mapper.toRestEntity
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByDurationId
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByRepsId
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.NewRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutine
import ee.ut.cs.HEALTH.domain.model.routine.RestDurationBetweenExercisesId
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseDefinition
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.RoutineId as DomainRoutineId
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutineItem
import ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineDao
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineHistoryItem
import ee.ut.cs.HEALTH.data.local.entities.CompletedRoutineEntity
import java.util.Date

import ee.ut.cs.HEALTH.data.local.dao.DailyRoutineCount
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import java.util.Calendar

/**
 * A repository that acts as a single source of truth for all routine-related data.
 *
 * It abstracts the data sources (local Room database DAOs) from the rest of the app,
 * providing a clean API for the ViewModels to interact with. It handles mapping between
 * database entities and domain models.
 *
 * @param db The main Room database instance, used for transactions.
 * @param dao The Data Access Object for routines and their components.
 * @param completedRoutineDao The DAO for handling completed routine history.
 * @param profileDao The DAO for accessing user profile data.
 */
class RoutineRepository(
    private val db: RoomDatabase,
    private val dao: RoutineDao,
    private val completedRoutineDao: CompletedRoutineDao,
    private val profileDao: ProfileDao
) {

    /**
     * Retrieves the user's profile from the database.
     * @return A [Flow] emitting the [ProfileEntity], or null if it doesn't exist.
     */
    fun getProfile(): Flow<ProfileEntity?> {
        return profileDao.getProfile()
    }

    /**
     * Calculates the total number of completed routines within a given number of past days.
     * @param days The number of days to look back from today.
     * @return A [Flow] emitting the total count.
     */
    fun getCompletedCountSince(days: Int): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        return completedRoutineDao.getCompletedCountSince(calendar.time)
    }

    /**
     * Inserts a new routine and all its associated items into the database within a single transaction.
     *
     * @param new The [NewRoutine] domain model to be saved.
     * @return The resulting [SavedRoutine] domain model, including the new database-generated IDs.
     */
    suspend fun insert(new: NewRoutine): SavedRoutine = db.withTransaction {
        val routineId = EntityRoutineId(dao.insertRoutine(new.toEntity()))

        val savedItems: List<SavedRoutineItem> = new.routineItems.mapIndexed { index, routineItem ->
            val routineItemEntity = newRoutineItemToEntity(routineId, index, routineItem)
            val routineItemId = RoutineItemId(dao.insertRoutineItem(routineItemEntity))

            when (routineItem) {
                is NewExerciseByReps -> insertNewExerciseByReps(routineItemId, routineItem)
                is NewExerciseByDuration -> insertNewExerciseByDuration(routineItemId, routineItem)
                is NewRestDurationBetweenExercises -> insertNewRestDurationBetweenExercises(routineItemId, routineItem)
            }
        }

        SavedRoutine(
            id = DomainRoutineId(routineId.value),
            name = new.name,
            description = new.description,
            routineItems = savedItems,
            counter = new.counter
        )
    }

    /**
     * Private helper to insert a repetition-based exercise.
     * It ensures the exercise definition exists before creating the exercise records.
     */
    private suspend fun insertNewExerciseByReps(
        id: RoutineItemId,
        newExercise: NewExerciseByReps
    ): SavedExerciseByReps {
        dao.upsertExerciseDefinition(newExercise.exerciseDefinition.toEntity())
        dao.insertExercise(newExercise.toExerciseEntity(id))
        dao.insertExerciseByReps(newExercise.toExerciseByRepsEntity(id))

        return SavedExerciseByReps(
            id = ExerciseByRepsId(id.value),
            exerciseDefinition = newExercise.exerciseDefinition,
            recommendedRestDurationBetweenSets = newExercise.recommendedRestDurationBetweenSets,
            amountOfSets = newExercise.amountOfSets,
            weight = newExercise.weight,
            countOfRepetitions = newExercise.countOfRepetitions
        )
    }

    /**
     * Private helper to insert a duration-based exercise.
     * It ensures the exercise definition exists before creating the exercise records.
     */
    private suspend fun insertNewExerciseByDuration(
        id: RoutineItemId,
        newExercise: NewExerciseByDuration
    ): SavedExerciseByDuration {
        dao.upsertExerciseDefinition(newExercise.exerciseDefinition.toEntity())
        dao.insertExercise(newExercise.toExerciseEntity(id))
        dao.insertExerciseByDuration(newExercise.toExerciseByDurationEntity(id))

        return SavedExerciseByDuration(
            id = ExerciseByDurationId(id.value),
            exerciseDefinition = newExercise.exerciseDefinition,
            recommendedRestDurationBetweenSets = newExercise.recommendedRestDurationBetweenSets,
            amountOfSets = newExercise.amountOfSets,
            weight = newExercise.weight,
            duration = newExercise.duration
        )
    }

    /**
     * Private helper to insert a rest period between exercises.
     */
    private suspend fun insertNewRestDurationBetweenExercises(
        id: RoutineItemId,
        restDuration: NewRestDurationBetweenExercises
    ): SavedRestDurationBetweenExercises {
        dao.insertRestDurationBetweenExercises(restDuration.toRestEntity(id))
        return SavedRestDurationBetweenExercises(
            id = RestDurationBetweenExercisesId(id.value),
            restDuration = restDuration.restDuration
        )
    }

    /**
     * Retrieves all saved exercise definitions from the database.
     * @return A [Flow] emitting a list of [SavedExerciseDefinition] domain models.
     */
    fun getAllExerciseDefinitions(): Flow<List<SavedExerciseDefinition>> =
        dao.getAllExerciseDefinitions().map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Retrieves a lightweight summary of all routines.
     * @return A [Flow] emitting a list of [RoutineSummary] domain models.
     */
    fun getAllRoutineSummaries(): Flow<List<RoutineSummary>> =
        dao.getAllRoutines().map { list -> list.map { it.toDomainSummary() } }

    /**
     * Searches for routine summaries where the name matches the query.
     * @param query The text to search for in routine names.
     * @return A [Flow] emitting a list of matching [RoutineSummary]s, sorted by completion count.
     */
    fun searchRoutineSummaries(query: String): Flow<List<RoutineSummary>> {
        return dao.searchRoutines(query).map { entityList ->
            entityList
                .map { it.toDomainSummary() }
                .sortedBy { it.completionCount }
        }
    }

    /**
     * Retrieves the full details of a single routine by its ID.
     * @param id The domain-layer ID of the routine.
     * @return A [Flow] emitting the complete [SavedRoutine] domain model.
     */
    @Suppress("DEPRECATION")
    fun getRoutine(id: DomainRoutineId): Flow<SavedRoutine> =
        dao.getRoutineEntityFlow(EntityRoutineId(id.value))
            .combine(dao.getRoutineItemsOrderedFlow(EntityRoutineId(id.value))) { entity, items ->
                entity?.let { RoutineDto(it, items) }
            }.filterNotNull()
            .map { it.toDomain() }

    /**
     * Marks a routine as completed.
     * This action is performed in a transaction to ensure both the history is recorded
     * and the routine's counter is incremented atomically.
     * @param id The domain-layer ID of the routine that was completed.
     * @param note An optional user-provided note for the completion.
     */
    suspend fun markRoutineAsCompleted(id: DomainRoutineId, note: String?) {
        db.withTransaction {
            val completedRoutine = CompletedRoutineEntity(
                routineId = EntityRoutineId(id.value),
                completionDate = Date(),
                completionNote = note
            )
            completedRoutineDao.insertCompletedRoutine(completedRoutine)
            dao.incrementRoutineCompletionCounter(EntityRoutineId(id.value))
        }
    }

    /**
     * Retrieves the full completion history, joining routine names with completion records.
     * @return A [Flow] emitting a list of [CompletedRoutineHistoryItem]s.
     */
    fun getCompletionHistory(): Flow<List<CompletedRoutineHistoryItem>> {
        return completedRoutineDao.getAllCompletedRoutinesWithName()
    }

    /**
     * Retrieves a summary of the most popular routine (highest completion count).
     * @return A [Flow] emitting a [RoutineSummary], or null if no routines exist.
     */
    fun getMostPopularRoutineSummary(): Flow<RoutineSummary?> =
        dao.getMostPopularRoutine().map { entity -> entity?.toDomainSummary() }

    /**
     * Retrieves a summary of the newest routine (highest ID).
     * @return A [Flow] emitting a [RoutineSummary], or null if no routines exist.
     */
    fun getNewestRoutineSummary(): Flow<RoutineSummary?> =
        dao.getNewestRoutine().map { entity -> entity?.toDomainSummary() }

    /**
     * Retrieves the history item for the most recently completed routine.
     * @return A [Flow] emitting a [CompletedRoutineHistoryItem], or null if no history exists.
     */
    fun getLatestCompletedRoutine(): Flow<CompletedRoutineHistoryItem?> =
        completedRoutineDao.getLatestCompletedRoutine()

    /**
     * Retrieves the daily routine completion counts for the last 7 days.
     * @return A [Flow] emitting a list of [DailyRoutineCount]s.
     */
    fun getWeeklyActivity(): Flow<List<DailyRoutineCount>> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return completedRoutineDao.getDailyCounts(calendar.time)
    }

    /**
     * Retrieves the daily routine completion counts for the last 30 days.
     * @return A [Flow] emitting a list of [DailyRoutineCount]s.
     */
    fun getMonthlyActivity(): Flow<List<DailyRoutineCount>> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -29)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return completedRoutineDao.getDailyCounts(calendar.time)
    }
}

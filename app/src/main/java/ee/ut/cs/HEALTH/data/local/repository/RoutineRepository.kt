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
import java.util.Calendar


class RoutineRepository(
    private val db: RoomDatabase,
    private val dao: RoutineDao,
    private val completedRoutineDao: CompletedRoutineDao
) {
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
            routineItems = savedItems
        )
    }

    private suspend fun insertNewExerciseByReps(
        id: RoutineItemId,
        newExercise: NewExerciseByReps
    ): SavedExerciseByReps {
        // Ensure exercise definition exists in database before inserting exercise
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

    private suspend fun insertNewExerciseByDuration(
        id: RoutineItemId,
        newExercise: NewExerciseByDuration
    ): SavedExerciseByDuration {
        // Ensure exercise definition exists in database before inserting exercise
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

    fun getAllExerciseDefinitions(): Flow<List<SavedExerciseDefinition>> =
        dao.getAllExerciseDefinitions().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getAllRoutineSummaries(): Flow<List<RoutineSummary>> =
        dao.getAllRoutines().map { list -> list.map { it.toDomainSummary() } }

    fun searchRoutineSummaries(query: String): Flow<List<RoutineSummary>> {

        return dao.searchRoutines(query)
            .map { entityList ->

                entityList.map { entity ->
                    RoutineSummary(
                        id = DomainRoutineId(entity.id.value),
                        name = entity.name,
                        description = entity.description,

                    )
                }
            }
    }

    @Suppress("DEPRECATION")
    fun getRoutine(id: DomainRoutineId): Flow<SavedRoutine> =
        dao.getRoutineEntityFlow(EntityRoutineId(id.value))
            .combine(dao.getRoutineItemsOrderedFlow(EntityRoutineId(id.value))) { entity, items ->
                entity?.let { RoutineDto(it, items) }
            }.filterNotNull()
                .map { it.toDomain() }

    /**
     * Marks a routine as completed by incrementing its counter in the database.
     * @param id The domain-layer ID of the routine that was completed.
     */
    suspend fun markRoutineAsCompleted(id: DomainRoutineId) {
        db.withTransaction {
            val completedRoutine = CompletedRoutineEntity(
                routineId = EntityRoutineId(id.value),
                completionDate = Date()
            )
            completedRoutineDao.insertCompletedRoutine(completedRoutine)
            dao.incrementRoutineCompletionCounter(EntityRoutineId(id.value))
        }
    }

    /**
     * Returns full history
     */
    fun getCompletionHistory(): Flow<List<CompletedRoutineHistoryItem>> {
        return completedRoutineDao.getAllCompletedRoutinesWithName()
    }

    // Returns a summary of the most popular routine.
    fun getMostPopularRoutineSummary(): Flow<RoutineSummary?> =
        dao.getMostPopularRoutine().map { entity -> entity?.toDomainSummary() }

    // Returns a summary of the newest routine.
    fun getNewestRoutineSummary(): Flow<RoutineSummary?> =
        dao.getNewestRoutine().map { entity -> entity?.toDomainSummary() }

    // Returns the history item for the most recently completed routine.
    fun getLatestCompletedRoutine(): Flow<CompletedRoutineHistoryItem?> =
        completedRoutineDao.getLatestCompletedRoutine()

    /**
     * Returns the daily routine completion counts for the last 7 days.
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
}
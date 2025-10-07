package ee.ut.cs.HEALTH.data.local.repository

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemId
import ee.ut.cs.HEALTH.data.mapper.newRoutineItemToEntity
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
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.RoutineId as DomainRoutineId
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutineItem

class RoutineRepository(
    private val db: RoomDatabase,
    private val dao: RoutineDao
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
}
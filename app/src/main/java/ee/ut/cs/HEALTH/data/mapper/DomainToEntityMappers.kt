package ee.ut.cs.HEALTH.data.mapper

import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionId
import ee.ut.cs.HEALTH.data.local.entities.ExerciseEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseType
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineId
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemId
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemType
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.NewExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.NewRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutine
import ee.ut.cs.HEALTH.domain.model.routine.NewRoutineItem

fun NewRoutine.toEntity(): RoutineEntity =
    RoutineEntity(
        name = name,
        description = description,
        counter = counter
    )

fun newRoutineItemToEntity(
    routineId: RoutineId,
    position: Int,
    item: NewRoutineItem
): RoutineItemEntity =
    RoutineItemEntity(
        routineId = routineId,
        type = when (item) {
            is NewExerciseByReps, is NewExerciseByDuration -> RoutineItemType.EXERCISE
            is NewRestDurationBetweenExercises -> RoutineItemType.REST
        },
        position = position
    )


fun NewExerciseByReps.toExerciseEntity(
    routineItemId: RoutineItemId
): ExerciseEntity =
    ExerciseEntity(
        id = routineItemId,
        exerciseDefinitionId = ExerciseDefinitionId(exerciseDefinition.id.value),
        type = ExerciseType.REPS,
        recommendedRestDurationBetweenSetsInSeconds = recommendedRestDurationBetweenSets.inWholeSeconds,
        amountOfSets = amountOfSets,
        weightInKg = weight?.inKg
    )

fun NewExerciseByReps.toExerciseByRepsEntity(
    routineItemId: RoutineItemId
): ExerciseByRepsEntity =
    ExerciseByRepsEntity(
        id = routineItemId,
        countOfRepetitions = countOfRepetitions
    )

fun NewExerciseByDuration.toExerciseEntity(
    routineItemId: RoutineItemId
): ExerciseEntity =
    ExerciseEntity(
        id = routineItemId,
        exerciseDefinitionId = ExerciseDefinitionId(exerciseDefinition.id.value),
        type = ExerciseType.DURATION,
        recommendedRestDurationBetweenSetsInSeconds = recommendedRestDurationBetweenSets.inWholeSeconds,
        amountOfSets = amountOfSets,
        weightInKg = weight?.inKg
    )

fun NewExerciseByDuration.toExerciseByDurationEntity(
    routineItemId: RoutineItemId
): ExerciseByDurationEntity =
    ExerciseByDurationEntity(
        id = routineItemId,
        durationInSeconds = duration.inWholeSeconds
    )

fun NewRestDurationBetweenExercises.toRestEntity(
    itemId: RoutineItemId
): RestDurationBetweenExercisesEntity =
    RestDurationBetweenExercisesEntity(
        id = itemId,
        durationInSeconds = restDuration.inWholeSeconds
    )
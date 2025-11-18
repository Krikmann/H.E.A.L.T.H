package ee.ut.cs.HEALTH.data.mapper

import ee.ut.cs.HEALTH.data.local.dto.ExerciseDto
import ee.ut.cs.HEALTH.data.local.dto.RoutineDto
import ee.ut.cs.HEALTH.data.local.dto.RoutineItemDto
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByDurationId
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByRepsId
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseDefinition
import ee.ut.cs.HEALTH.domain.model.routine.Weight
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseDefinitionId
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.RestDurationBetweenExercisesId
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine
import ee.ut.cs.HEALTH.domain.model.routine.RoutineId
import ee.ut.cs.HEALTH.domain.model.routine.SavedExercise
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutineItem
import ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary


fun ExerciseDefinitionEntity.toDomain(): SavedExerciseDefinition =
    SavedExerciseDefinition(
        id = ExerciseDefinitionId(id.value),
        name = name
    )

fun RoutineEntity.toDomainSummary() = RoutineSummary(
    id = RoutineId(id.value),
    name = name,
    description = description,
    completionCount = counter
)

fun ExerciseDto.toDomain(): SavedExercise {
    val def = exerciseDefinition.toDomain()

    val amountOfSets = exercise.amountOfSets
    val recRest: Duration = exercise.recommendedRestDurationBetweenSetsInSeconds.seconds
    val weight: Weight? = exercise.weightInKg?.let { Weight.fromKg(it) }

    byReps?.let { reps ->
        return SavedExerciseByReps(
            id = ExerciseByRepsId(reps.id.value),
            exerciseDefinition = def,
            recommendedRestDurationBetweenSets = recRest,
            amountOfSets = amountOfSets,
            weight = weight,
            countOfRepetitions = reps.countOfRepetitions
        )
    }

    byDuration?.let { dur ->
        return SavedExerciseByDuration(
            id = ExerciseByDurationId(dur.id.value),
            exerciseDefinition = def,
            recommendedRestDurationBetweenSets = recRest,
            amountOfSets = amountOfSets,
            weight = weight,
            duration = dur.durationInSeconds.seconds
        )
    }

    error("ExerciseDto(id=${exercise.id}) has neither reps nor duration payload")
}

fun RoutineItemDto.toDomain(): SavedRoutineItem {
    exercise?.let { return it.toDomain() }

    val rest = requireNotNull(restDurationBetweenExercises) {
        "RoutineItemDto(id=${routineItem.id}) has no exercise nor rest payload"
    }
    return SavedRestDurationBetweenExercises(
        id = RestDurationBetweenExercisesId(rest.id.value),
        restDuration = rest.durationInSeconds.seconds
    )
}

fun RoutineDto.toDomain(): SavedRoutine =
    SavedRoutine(
        id = RoutineId(routine.id.value),
        name = routine.name,
        description = routine.description,
        routineItems = routineItems.map { it.toDomain() },
        counter = routine.counter
    )
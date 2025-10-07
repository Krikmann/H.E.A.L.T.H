package ee.ut.cs.HEALTH.data.mapper

import ee.ut.cs.HEALTH.data.local.dto.ExerciseDto
import ee.ut.cs.HEALTH.data.local.dto.RoutineDto
import ee.ut.cs.HEALTH.data.local.dto.RoutineItemDto
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.domain.model.routine.Exercise
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByDurationId
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseByRepsId
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseDefinition
import ee.ut.cs.HEALTH.domain.model.routine.Weight
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import ee.ut.cs.HEALTH.domain.model.routine.ExerciseDefinitionId
import ee.ut.cs.HEALTH.domain.model.routine.RestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.RestDurationBetweenExercisesId
import ee.ut.cs.HEALTH.domain.model.routine.Routine
import ee.ut.cs.HEALTH.domain.model.routine.RoutineId
import ee.ut.cs.HEALTH.domain.model.routine.RoutineItem


fun ExerciseDefinitionEntity.toDomain(): ExerciseDefinition =
    ExerciseDefinition(
        id = ExerciseDefinitionId(id.id),
        name = name
    )

fun ExerciseDto.toDomain(): Exercise {
    val def = exerciseDefinition.toDomain()

    val amountOfSets = exercise.amountOfSets
    val recRest: Duration = exercise.recommendedRestDurationBetweenSetsInSeconds.seconds
    val weight: Weight? = exercise.weightInKg?.let { Weight.fromKg(it) }

    byReps?.let { reps ->
        return ExerciseByReps(
            id = ExerciseByRepsId(reps.id.id),
            exerciseDefinition = def,
            recommendedRestDurationBetweenSets = recRest,
            amountOfSets = amountOfSets,
            weight = weight,
            countOfRepetitions = reps.countOfRepetitions
        )
    }

    byDuration?.let { dur ->
        return ExerciseByDuration(
            id = ExerciseByDurationId(dur.id.id),
            exerciseDefinition = def,
            recommendedRestDurationBetweenSets = recRest,
            amountOfSets = amountOfSets,
            weight = weight,
            duration = dur.durationInSeconds.seconds
        )
    }

    error("ExerciseDto(id=${exercise.id}) has neither reps nor duration payload")
}

fun RoutineItemDto.toDomain(): RoutineItem {
    exercise?.let { return it.toDomain() }

    val rest = requireNotNull(restDurationBetweenExercises) {
        "RoutineItemDto(id=${routineItem.id}) has no exercise nor rest payload"
    }
    return RestDurationBetweenExercises(
        id = RestDurationBetweenExercisesId(rest.id.id),
        restDuration = rest.durationInSeconds.seconds
    )
}

fun RoutineDto.toDomain(): Routine =
    Routine(
        id = RoutineId(routine.id.id),
        name = routine.name,
        description = routine.description,
        routineItems = routineItems.map { it.toDomain() },
        counter = routine.counter
    )
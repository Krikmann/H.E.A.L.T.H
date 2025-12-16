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

/**
 * Converts a database [ExerciseDefinitionEntity] to a [SavedExerciseDefinition] domain model.
 */
fun ExerciseDefinitionEntity.toDomain(): SavedExerciseDefinition =
    SavedExerciseDefinition(
        id = ExerciseDefinitionId(id.value),
        name = name
    )

/**
 * Converts a database [RoutineEntity] to a lightweight [RoutineSummary] domain model.
 * This is used for displaying lists of routines without loading all their items.
 */
fun RoutineEntity.toDomainSummary() = RoutineSummary(
    id = RoutineId(id.value),
    name = name,
    description = description,
    completionCount = counter
)

/**
 * Converts a composite [ExerciseDto] from the database layer into a specific [SavedExercise]
 * domain model ([SavedExerciseByReps] or [SavedExerciseByDuration]).
 *
 * This function handles the logic of checking whether the exercise is repetition-based or
 * duration-based and constructs the appropriate domain object.
 *
 * @throws IllegalStateException if the DTO contains neither a reps nor a duration payload.
 */
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

/**
 * Converts a generic [RoutineItemDto] from the database into a specific [SavedRoutineItem]
 * domain model ([SavedExercise] or [SavedRestDurationBetweenExercises]).
 *
 * It determines whether the item is an exercise or a rest period and calls the corresponding
 * more specific mapper.
 *
 * @throws IllegalStateException if the DTO is neither an exercise nor a rest period.
 */
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

/**
 * Converts a full [RoutineDto] (which includes the routine entity and its list of items)
 * into a complete [SavedRoutine] domain model.
 *
 * This function orchestrates the mapping of the entire routine object graph from the
 * database representation to the domain representation.
 */
fun RoutineDto.toDomain(): SavedRoutine =
    SavedRoutine(
        id = RoutineId(routine.id.value),
        name = routine.name,
        description = routine.description,
        routineItems = routineItems.map { it.toDomain() },
        counter = routine.counter
    )

package ee.ut.cs.HEALTH.data.mapper
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
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
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseDefinition

/**
 * Converts a [NewRoutine] domain model into a [RoutineEntity] for database insertion.
 */
fun NewRoutine.toEntity(): RoutineEntity =
    RoutineEntity(
        name = name,
        description = description,
        counter = counter
    )

/**
 * Converts a generic [NewRoutineItem] from the domain layer into a database-ready [RoutineItemEntity].
 *
 * This function determines the correct [RoutineItemType] (EXERCISE or REST) and assigns the
 * routine's foreign key and the item's position within the routine.
 *
 * @param routineId The ID of the parent routine this item belongs to.
 * @param position The zero-based index of this item within the routine's sequence.
 * @param item The domain model of the new routine item.
 * @return A [RoutineItemEntity] ready for database insertion.
 */
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

/**
 * Converts a [NewExerciseByReps] domain model into a generic [ExerciseEntity].
 * This entity holds properties common to all exercise types.
 *
 * @param routineItemId The primary key of the corresponding [RoutineItemEntity].
 * @return An [ExerciseEntity] ready for database insertion.
 */
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

/**
 * Converts a [NewExerciseByReps] domain model into its specific [ExerciseByRepsEntity].
 * This entity holds the properties unique to repetition-based exercises.
 *
 * @param routineItemId The primary key of the corresponding [RoutineItemEntity].
 * @return An [ExerciseByRepsEntity] ready for database insertion.
 */
fun NewExerciseByReps.toExerciseByRepsEntity(
    routineItemId: RoutineItemId
): ExerciseByRepsEntity =
    ExerciseByRepsEntity(
        id = routineItemId,
        countOfRepetitions = countOfRepetitions
    )

/**
 * Converts a [NewExerciseByDuration] domain model into a generic [ExerciseEntity].
 * This entity holds properties common to all exercise types.
 *
 * @param routineItemId The primary key of the corresponding [RoutineItemEntity].
 * @return An [ExerciseEntity] ready for database insertion.
 */
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

/**
 * Converts a [NewExerciseByDuration] domain model into its specific [ExerciseByDurationEntity].
 * This entity holds the properties unique to duration-based exercises.
 *
 * @param routineItemId The primary key of the corresponding [RoutineItemEntity].
 * @return An [ExerciseByDurationEntity] ready for database insertion.
 */
fun NewExerciseByDuration.toExerciseByDurationEntity(
    routineItemId: RoutineItemId
): ExerciseByDurationEntity =
    ExerciseByDurationEntity(
        id = routineItemId,
        durationInSeconds = duration.inWholeSeconds
    )

/**
 * Converts a [NewRestDurationBetweenExercises] domain model into a [RestDurationBetweenExercisesEntity].
 *
 * @param itemId The primary key of the corresponding [RoutineItemEntity].
 * @return A [RestDurationBetweenExercisesEntity] ready for database insertion.
 */
fun NewRestDurationBetweenExercises.toRestEntity(
    itemId: RoutineItemId
): RestDurationBetweenExercisesEntity =
    RestDurationBetweenExercisesEntity(
        id = itemId,
        durationInSeconds = restDuration.inWholeSeconds
    )

/**
 * Converts a [SavedExerciseDefinition] domain model into an [ExerciseDefinitionEntity] for database upsertion.
 * This is used to ensure an exercise definition exists in the local database before being linked to a routine item.
 */
fun SavedExerciseDefinition.toEntity(): ExerciseDefinitionEntity =
    ExerciseDefinitionEntity(
        id = ExerciseDefinitionId(id.value),
        name = name
    )


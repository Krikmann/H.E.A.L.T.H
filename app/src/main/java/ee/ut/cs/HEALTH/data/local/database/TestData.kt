package ee.ut.cs.HEALTH.data.local.database

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
import java.util.stream.Collectors

object TestData {
    val testDefinitions = listOf(
        ExerciseDefinitionEntity(id = ExerciseDefinitionId(1), name = "Push-ups"),
        ExerciseDefinitionEntity(id = ExerciseDefinitionId(2), name = "Plank")
    )

    val testRoutines = listOf(
        RoutineEntity(
        id = RoutineId(1),
        name = "Morning Workout",
        description = "Quick bodyweight warm-up"
        ),
        RoutineEntity(
            id = RoutineId(2),
            name = "Evening Workout",
            description = "Slow bodyweight warm-up"
        ),
        RoutineEntity(
            id = RoutineId(3),
            name = "Boring Workout",
            description = "Workout that makes you sleep"
        ),
        RoutineEntity(
            id = RoutineId(4),
            name = "Very Hard Workout",
            description = "Workout only for the experts"
        ),
        RoutineEntity(
            id = RoutineId(5),
            name = "Very Easy Workout",
            description = "Recommended when you are just starting out"
        ),
        RoutineEntity(
            id = RoutineId(6),
            name = "Long Workout",
            description = "When you have too much free time"
        ),
        RoutineEntity(
            id = RoutineId(7),
            name = "Short Workout",
            description = "Workout you can do in just 5 minutes"
        ),
    )


    val testRoutineItems = listOf(
        RoutineItemEntity(
            id = RoutineItemId(1),
            routineId = RoutineId(1),
            type = RoutineItemType.EXERCISE,
            position = 0
        ),
        RoutineItemEntity(
            id = RoutineItemId(2),
            routineId = RoutineId(1),
            type = RoutineItemType.EXERCISE,
            position = 1
        ),
        RoutineItemEntity(
            id = RoutineItemId(3),
            routineId = RoutineId(1),
            type = RoutineItemType.REST,
            position = 2
        )
    )

    val testExerciseEntities = listOf(
        ExerciseEntity(
            id = RoutineItemId(1),
            exerciseDefinitionId = ExerciseDefinitionId(1),
            type = ExerciseType.REPS,
            recommendedRestDurationBetweenSetsInSeconds = 60,
            amountOfSets = 3,
            weightInKg = null
        ),
        ExerciseEntity(
            id = RoutineItemId(2),
            exerciseDefinitionId = ExerciseDefinitionId(2),
            type = ExerciseType.DURATION,
            recommendedRestDurationBetweenSetsInSeconds = 60,
            amountOfSets = 2,
            weightInKg = null
        )
    )

    val testExercisesByReps = listOf(
        ExerciseByRepsEntity(
            id = RoutineItemId(1),
            countOfRepetitions = 15
        )
    )

    val testExercisesByDuration = listOf(
        ExerciseByDurationEntity(
            id = RoutineItemId(2),
            durationInSeconds = 30
        )
    )

    val testRestItems = listOf(
        RestDurationBetweenExercisesEntity(
            id = RoutineItemId(3),
            durationInSeconds = 120
        )
    )

    val testExerciseDefinitions: List<ExerciseDefinitionEntity> = testExerciseDefinitionStrings.stream().map { definitionString ->
        ExerciseDefinitionEntity(
            id = ExerciseDefinitionId(0),
            name = definitionString
        )
    }.collect(Collectors.toList())
}
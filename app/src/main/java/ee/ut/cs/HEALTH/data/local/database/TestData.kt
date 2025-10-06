package ee.ut.cs.HEALTH.data.local.database

import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity

object TestData {
    val testDefinitions = listOf(
        ExerciseDefinitionEntity(id = 1, name = "Push-ups"),
        ExerciseDefinitionEntity(id = 2, name = "Plank")
    )

    val testRoutines = listOf(
        RoutineEntity(
        id = 1,
        name = "Morning Workout",
        description = "Quick bodyweight warm-up"
        ),
        RoutineEntity(
            id = 2,
            name = "Evening Workout",
            description = "Slow bodyweight warm-up"
        ),
        RoutineEntity(
            id = 3,
            name = "Boring Workout",
            description = "Workout that makes you sleep"
        ),
        RoutineEntity(
            id = 4,
            name = "Very Hard Workout",
            description = "Workout only for the experts"
        ),
        RoutineEntity(
            id = 5,
            name = "Very Easy Workout",
            description = "Recommended when you are just starting out"
        ),
        RoutineEntity(
            id = 6,
            name = "Long Workout",
            description = "When you have too much free time"
        ),
        RoutineEntity(
            id = 7,
            name = "Short Workout",
            description = "Workout you can do in just 5 minutes"
        ),
        )

    val testExercises = listOf(
        ExerciseByRepsEntity(
            id = 1,
            routineId = 1,
            exerciseDefinitionId = 1,
            restDurationBetweenSetsMillis = 60000,
            amountOfSets = 3,
            weightInKg = null,
            countOfRepetitions = 15
        ),
        ExerciseByDurationEntity(
            id = 2,
            routineId = 1,
            exerciseDefinitionId = 2,
            restDurationBetweenSetsMillis = 60000,
            amountOfSets = 2,
            weightInKg = null,
            durationMillis = 30000
        )
    )

    val testRestItem = RestDurationBetweenExercisesEntity(
        id = 1,
        routineId = 1,
        restDurationMillis = 120000
    )
}
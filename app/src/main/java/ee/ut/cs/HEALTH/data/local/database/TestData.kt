package ee.ut.cs.HEALTH.data.local.database

import ee.ut.cs.HEALTH.data.local.entities.CompletedRoutineEntity
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
import java.util.Date
import java.util.concurrent.TimeUnit

object TestData {
    private val EXERCISE_1_ID = ExerciseDefinitionId("exr_41n2hsSnmS946i2k") // Single Leg Squat with Support
    private val EXERCISE_2_ID = ExerciseDefinitionId("exr_41n2hGioS8HumEF7") // Hammer Curl
    private val EXERCISE_3_ID = ExerciseDefinitionId("exr_41n2hKoQnnSRPZrE") // Front Plank with Leg Lift
    private val EXERCISE_4_ID = ExerciseDefinitionId("exr_41n2hnx1hnDdketU") // Feet and Ankles Stretch


    val testCompletedRoutines = listOf(
        // Rutiin tehtud täna (et graafikul oleks tänane tulp)
        CompletedRoutineEntity(
            routineId = RoutineId(1),
            completionDate = Date(System.currentTimeMillis())
        ),
        // Rutiin "Morning Workout" (ID 1), tehtud eile
        CompletedRoutineEntity(
            routineId = RoutineId(1),
            completionDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
        ),
        // Rutiin "Morning Workout" (ID 1), tehtud 3 päeva tagasi
        CompletedRoutineEntity(
            routineId = RoutineId(1),
            completionDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3))
        ),
        // Rutiin "Evening Workout" (ID 2), tehtud 3 päeva tagasi
        CompletedRoutineEntity(
            routineId = RoutineId(2),
            completionDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3))
        ),
        // Rutiin "Long Workout" (ID 6), tehtud nädal tagasi
        CompletedRoutineEntity(
            routineId = RoutineId(6),
            completionDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))
        )
    )

    val testRoutines = listOf(
        RoutineEntity(
            id = RoutineId(1),
            name = "Morning Workout",
            description = "Quick bodyweight warm-up",
            counter = 301
        ),
        RoutineEntity(
            id = RoutineId(2),
            name = "Evening Workout",
            description = "Slow bodyweight warm-up",
            counter = 134
        ),
        RoutineEntity(
            id = RoutineId(3),
            name = "Boring Workout",
            description = "Workout that makes you sleep",
            counter = 124
        ),
        RoutineEntity(
            id = RoutineId(4),
            name = "Very Hard Workout",
            description = "Workout only for the experts",
            counter = 2
        ),
        RoutineEntity(
            id = RoutineId(5),
            name = "Very Easy Workout",
            description = "Recommended when you are just starting out",
            counter = 0
        ),
        RoutineEntity(
            id = RoutineId(6),
            name = "Long Workout",
            description = "When you have too much free time",
            counter = 1
        ),
        RoutineEntity(
            id = RoutineId(7),
            name = "Short Workout",
            description = "Workout you can do in just 5 minutes",
            counter = 0
        ),
    )


    val testRoutineItems = listOf(
        // --- Items for "Morning Workout" (ID 1) ---
        RoutineItemEntity(id = RoutineItemId(1), routineId = RoutineId(1), type = RoutineItemType.EXERCISE, position = 0),
        RoutineItemEntity(id = RoutineItemId(2), routineId = RoutineId(1), type = RoutineItemType.EXERCISE, position = 1),
        RoutineItemEntity(id = RoutineItemId(3), routineId = RoutineId(1), type = RoutineItemType.REST, position = 2),

        // --- Items for "Evening Workout" (ID 2) ---
        RoutineItemEntity(id = RoutineItemId(4), routineId = RoutineId(2), type = RoutineItemType.EXERCISE, position = 0),
        RoutineItemEntity(id = RoutineItemId(5), routineId = RoutineId(2), type = RoutineItemType.REST, position = 1),
        RoutineItemEntity(id = RoutineItemId(6), routineId = RoutineId(2), type = RoutineItemType.EXERCISE, position = 2),

        // --- Items for "Boring Workout" (ID 3) ---
        RoutineItemEntity(id = RoutineItemId(12), routineId = RoutineId(3), type = RoutineItemType.EXERCISE, position = 0),
        RoutineItemEntity(id = RoutineItemId(13), routineId = RoutineId(3), type = RoutineItemType.EXERCISE, position = 1),

        // --- Items for "Very Hard Workout" (ID 4) ---
        RoutineItemEntity(id = RoutineItemId(14), routineId = RoutineId(4), type = RoutineItemType.EXERCISE, position = 0),
        RoutineItemEntity(id = RoutineItemId(15), routineId = RoutineId(4), type = RoutineItemType.EXERCISE, position = 1),

        // --- Items for "Very Easy Workout" (ID 5) ---
        RoutineItemEntity(id = RoutineItemId(16), routineId = RoutineId(5), type = RoutineItemType.EXERCISE, position = 0),
        RoutineItemEntity(id = RoutineItemId(17), routineId = RoutineId(5), type = RoutineItemType.REST, position = 1),
        RoutineItemEntity(id = RoutineItemId(18), routineId = RoutineId(5), type = RoutineItemType.EXERCISE, position = 2),

        // --- Items for "Long Workout" (ID 6) ---
        RoutineItemEntity(id = RoutineItemId(7), routineId = RoutineId(6), type = RoutineItemType.EXERCISE, position = 0),
        RoutineItemEntity(id = RoutineItemId(8), routineId = RoutineId(6), type = RoutineItemType.EXERCISE, position = 1),
        RoutineItemEntity(id = RoutineItemId(9), routineId = RoutineId(6), type = RoutineItemType.EXERCISE, position = 2),
        RoutineItemEntity(id = RoutineItemId(10), routineId = RoutineId(6), type = RoutineItemType.REST, position = 3),
        RoutineItemEntity(id = RoutineItemId(11), routineId = RoutineId(6), type = RoutineItemType.EXERCISE, position = 4),

        // --- Items for "Short Workout" (ID 7) ---
        RoutineItemEntity(id = RoutineItemId(19), routineId = RoutineId(7), type = RoutineItemType.EXERCISE, position = 0),
    )

    val testExerciseEntities = listOf(
        // Exercises for "Morning Workout" (ID 1)
        ExerciseEntity(id = RoutineItemId(1), exerciseDefinitionId = EXERCISE_1_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 60, amountOfSets = 3, weightInKg = null),
        ExerciseEntity(id = RoutineItemId(2), exerciseDefinitionId = EXERCISE_2_ID, type = ExerciseType.DURATION, recommendedRestDurationBetweenSetsInSeconds = 60, amountOfSets = 2, weightInKg = null),

        // Exercises for "Evening Workout" (ID 2)
        ExerciseEntity(id = RoutineItemId(4), exerciseDefinitionId = EXERCISE_3_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 90, amountOfSets = 4, weightInKg = 20.0),
        ExerciseEntity(id = RoutineItemId(6), exerciseDefinitionId = EXERCISE_4_ID, type = ExerciseType.DURATION, recommendedRestDurationBetweenSetsInSeconds = 45, amountOfSets = 3, weightInKg = null),

        // Exercises for "Boring Workout" (ID 3)
        ExerciseEntity(id = RoutineItemId(12), exerciseDefinitionId = EXERCISE_4_ID, type = ExerciseType.DURATION, recommendedRestDurationBetweenSetsInSeconds = 120, amountOfSets = 1, weightInKg = null),
        ExerciseEntity(id = RoutineItemId(13), exerciseDefinitionId = EXERCISE_4_ID, type = ExerciseType.DURATION, recommendedRestDurationBetweenSetsInSeconds = 120, amountOfSets = 1, weightInKg = null),

        // Exercises for "Very Hard Workout" (ID 4)
        ExerciseEntity(id = RoutineItemId(14), exerciseDefinitionId = EXERCISE_2_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 180, amountOfSets = 5, weightInKg = 50.0),
        ExerciseEntity(id = RoutineItemId(15), exerciseDefinitionId = EXERCISE_2_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 180, amountOfSets = 5, weightInKg = 50.0),

        // Exercises for "Very Easy Workout" (ID 5)
        ExerciseEntity(id = RoutineItemId(16), exerciseDefinitionId = EXERCISE_1_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 30, amountOfSets = 2, weightInKg = 5.0),
        ExerciseEntity(id = RoutineItemId(18), exerciseDefinitionId = EXERCISE_3_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 30, amountOfSets = 2, weightInKg = 10.0),

        // Exercises for "Long Workout" (ID 6)
        ExerciseEntity(id = RoutineItemId(7), exerciseDefinitionId = EXERCISE_1_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 30, amountOfSets = 5, weightInKg = 10.0),
        ExerciseEntity(id = RoutineItemId(8), exerciseDefinitionId = EXERCISE_2_ID, type = ExerciseType.DURATION, recommendedRestDurationBetweenSetsInSeconds = 30, amountOfSets = 5, weightInKg = null),
        ExerciseEntity(id = RoutineItemId(9), exerciseDefinitionId = EXERCISE_3_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 30, amountOfSets = 5, weightInKg = 15.0),
        ExerciseEntity(id = RoutineItemId(11), exerciseDefinitionId = EXERCISE_4_ID, type = ExerciseType.DURATION, recommendedRestDurationBetweenSetsInSeconds = 30, amountOfSets = 5, weightInKg = null),

        // Exercises for "Short Workout" (ID 7)
        ExerciseEntity(id = RoutineItemId(19), exerciseDefinitionId = EXERCISE_1_ID, type = ExerciseType.REPS, recommendedRestDurationBetweenSetsInSeconds = 20, amountOfSets = 3, weightInKg = null),
    )

    val testExercisesByReps = listOf(
        // Reps for "Morning Workout"
        ExerciseByRepsEntity(id = RoutineItemId(1), countOfRepetitions = 15),

        // Reps for "Evening Workout"
        ExerciseByRepsEntity(id = RoutineItemId(4), countOfRepetitions = 10),

        // Reps for "Very Hard Workout"
        ExerciseByRepsEntity(id = RoutineItemId(14), countOfRepetitions = 10),
        ExerciseByRepsEntity(id = RoutineItemId(15), countOfRepetitions = 10),

        // Reps for "Very Easy Workout"
        ExerciseByRepsEntity(id = RoutineItemId(16), countOfRepetitions = 8),
        ExerciseByRepsEntity(id = RoutineItemId(18), countOfRepetitions = 8),

        // Reps for "Long Workout"
        ExerciseByRepsEntity(id = RoutineItemId(7), countOfRepetitions = 20),
        ExerciseByRepsEntity(id = RoutineItemId(9), countOfRepetitions = 12),
        // Reps for "Short Workout"
        ExerciseByRepsEntity(id = RoutineItemId(19), countOfRepetitions = 10)
    )

    val testExercisesByDuration = listOf(
        // Duration for "Morning Workout"
        ExerciseByDurationEntity(id = RoutineItemId(2), durationInSeconds = 30),

        // Duration for "Evening Workout"
        ExerciseByDurationEntity(id = RoutineItemId(6), durationInSeconds = 60),

        // Duration for "Boring Workout"
        ExerciseByDurationEntity(id = RoutineItemId(12), durationInSeconds = 300),
        ExerciseByDurationEntity(id = RoutineItemId(13), durationInSeconds = 300),

        // Duration for "Long Workout"
        ExerciseByDurationEntity(id = RoutineItemId(8), durationInSeconds = 45),
        ExerciseByDurationEntity(id = RoutineItemId(11), durationInSeconds = 90)
    )

    val testRestItems = listOf(
        // Rest for "Morning Workout"
        RestDurationBetweenExercisesEntity(id = RoutineItemId(3), durationInSeconds = 120),

        // Rest for "Evening Workout"
        RestDurationBetweenExercisesEntity(id = RoutineItemId(5), durationInSeconds = 60),

        // Rest for "Very Easy Workout"
        RestDurationBetweenExercisesEntity(id = RoutineItemId(17), durationInSeconds = 45),

        // Rest for "Long Workout"
        RestDurationBetweenExercisesEntity(id = RoutineItemId(10), durationInSeconds = 180)
    )

    val testExerciseDefinitions: List<ExerciseDefinitionEntity> = listOf(
        ExerciseDefinitionEntity(id = EXERCISE_1_ID, name = "Single Leg Squat with Support"),
        ExerciseDefinitionEntity(id = EXERCISE_2_ID, name = "Hammer Curl"),
        ExerciseDefinitionEntity(id = EXERCISE_3_ID, name = "Front Plank with Leg Lift"),
        ExerciseDefinitionEntity(id = EXERCISE_4_ID, name = "Feet and Ankles Stretch"),
    )
}

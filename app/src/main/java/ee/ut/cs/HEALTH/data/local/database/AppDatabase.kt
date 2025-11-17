package ee.ut.cs.HEALTH.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineDao
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseEntity
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import ee.ut.cs.HEALTH.data.local.entities.RestDurationBetweenExercisesEntity
import ee.ut.cs.HEALTH.data.local.entities.CompletedRoutineEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemEntity

@Database(
    entities = [
        RoutineEntity::class,
        RoutineItemEntity::class,
        RestDurationBetweenExercisesEntity::class,
        ExerciseEntity::class,
        ExerciseByRepsEntity::class,
        ExerciseByDurationEntity::class,
        ExerciseDefinitionEntity::class,
        ProfileEntity::class,
        CompletedRoutineEntity::class
    ],
    version = 3,
    exportSchema = true
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun profileDao(): ProfileDao
    abstract fun completedRoutineDao(): CompletedRoutineDao
}
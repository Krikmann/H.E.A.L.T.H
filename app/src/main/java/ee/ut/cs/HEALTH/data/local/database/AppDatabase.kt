package ee.ut.cs.HEALTH.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

/**
 * The main Room database class for the application.
 *
 * This abstract class serves as the main access point to the persisted data. It defines
 * the list of entities that make up the database, the database version, and provides
 * abstract methods for accessing the Data Access Objects (DAOs).
 *
 * The `exportSchema` parameter, when true, exports the database schema into a JSON file,
 * which is useful for version control and understanding the database structure.
 *
 * @see Database The annotation that marks this class as a Room database.
 * @see TypeConverters Specifies the [Converters] class to handle custom data type conversions.
 */
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
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the Data Access Object for routine-related operations.
     * @return An instance of [RoutineDao].
     */
    abstract fun routineDao(): RoutineDao

    /**
     * Provides access to the Data Access Object for profile-related operations.
     * @return An instance of [ProfileDao].
     */
    abstract fun profileDao(): ProfileDao

    /**
     * Provides access to the Data Access Object for completed routine history operations.
     * @return An instance of [CompletedRoutineDao].
     */
    abstract fun completedRoutineDao(): CompletedRoutineDao
}

/**
 * Migration from database version 3 to 4.
 *
 * This migration adds `weeklyGoal` and `monthlyGoal` columns to the `profile` table
 * to store user-defined workout goals. Default values are provided to ensure existing
 * rows are populated correctly.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE profile ADD COLUMN weeklyGoal INTEGER NOT NULL DEFAULT 4")
        db.execSQL("ALTER TABLE profile ADD COLUMN monthlyGoal INTEGER NOT NULL DEFAULT 16")
    }
}

/**
 * Migration from database version 4 to 5.
 *
 * This migration adds a nullable `completionNote` column to the `completed_routines` table,
 * allowing users to add an optional note when they complete a workout.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE completed_routines ADD COLUMN completionNote TEXT")
    }
}


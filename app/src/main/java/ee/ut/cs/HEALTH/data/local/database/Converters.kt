package ee.ut.cs.HEALTH.data.local.database

import androidx.room.TypeConverter
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionId
import ee.ut.cs.HEALTH.data.local.entities.RoutineId
import java.util.Date

/**
 * Provides type converters for Room database.
 *
 * This class contains methods that allow Room to persist and retrieve complex data types
 * that it doesn't natively support, such as [Date] and custom value classes like [RoutineId].
 */
class Converters {

    /**
     * Converts a [Long] timestamp from the database into a [Date] object.
     * @param value The timestamp in milliseconds since the epoch.
     * @return A [Date] object, or `null` if the input value is null.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts a [Date] object into a [Long] timestamp for database storage.
     * @param date The [Date] object to convert.
     * @return A [Long] representing the timestamp, or `null` if the input date is null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts a [Long] from the database into a type-safe [RoutineId] value class.
     * @param value The raw ID value from the database.
     * @return A [RoutineId] instance, or `null` if the input is null.
     */
    @TypeConverter
    fun fromRoutineId(value: Long?): RoutineId? {
        return value?.let { RoutineId(it) }
    }

    /**
     * Converts a type-safe [RoutineId] value class into a [Long] for database storage.
     * @param routineId The [RoutineId] to convert.
     * @return The underlying [Long] value, or `null` if the input is null.
     */
    @TypeConverter
    fun routineIdToLong(routineId: RoutineId?): Long? {
        return routineId?.value
    }

    /**
     * Converts a [String] from the database into a type-safe [ExerciseDefinitionId] value class.
     * @param value The raw ID value from the database.
     * @return An [ExerciseDefinitionId] instance, or `null` if the input is null.
     */
    @TypeConverter
    fun fromExerciseDefinitionId(value: String?): ExerciseDefinitionId? {
        return value?.let { ExerciseDefinitionId(it) }
    }

    /**
     * Converts a type-safe [ExerciseDefinitionId] value class into a [String] for database storage.
     * @param exerciseId The [ExerciseDefinitionId] to convert.
     * @return The underlying [String] value, or `null` if the input is null.
     */
    @TypeConverter
    fun exerciseDefinitionIdToString(exerciseId: ExerciseDefinitionId?): String? {
        return exerciseId?.value
    }
}

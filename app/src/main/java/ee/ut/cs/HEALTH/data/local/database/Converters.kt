
package ee.ut.cs.HEALTH.data.local.database

import androidx.room.TypeConverter
import ee.ut.cs.HEALTH.data.local.entities.RoutineId
import java.util.Date

class Converters {
    /**
     * Converter for java.util.Date to Long (Timestamp)
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converter for our custom RoutineId value class to Long
     */
    @TypeConverter
    fun fromRoutineId(value: Long?): RoutineId? {
        return value?.let { RoutineId(it) }
    }

    @TypeConverter
    fun routineIdToLong(routineId: RoutineId?): Long? {
        return routineId?.value
    }
}

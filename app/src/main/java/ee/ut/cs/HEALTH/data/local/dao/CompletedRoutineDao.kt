package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ee.ut.cs.HEALTH.data.local.entities.CompletedRoutineEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * A data class to hold a flattened representation of a completed routine's history.
 *
 * This is used as the return type for a query that joins `completed_routines` and `routines` tables
 * to include the routine's name alongside the completion details.
 *
 * @property routineId The ID of the completed routine.
 * @property name The name of the completed routine.
 * @property completionDate The timestamp when the routine was completed.
 * @property completionNote An optional user-provided note for the completion.
 */
data class CompletedRoutineHistoryItem(
    val routineId: Long,
    val name: String,
    val completionDate: java.util.Date,
    val completionNote: String?
)

/**
 * A data class to hold the count of completed routines for a specific day.
 *
 * This is used as the return type for aggregation queries that group completions by date.
 *
 * @property day The start-of-day timestamp for which the count is calculated.
 * @property count The total number of routines completed on that day.
 */
data class DailyRoutineCount(
    val day: Date,
    val count: Int
)

/**
 * Data Access Object (DAO) for completed routine-related database operations.
 *
 * This interface defines the methods for interacting with the `completed_routines` table,
 * which stores the history of user's completed workouts.
 */
@Dao
interface CompletedRoutineDao {
    /**
     * Inserts a record of a completed routine into the database.
     *
     * @param completedRoutine The [CompletedRoutineEntity] to insert.
     */
    @Insert
    suspend fun insertCompletedRoutine(completedRoutine: CompletedRoutineEntity)

    /**
     * Retrieves the entire history of completed routines, joined with their names.
     *
     * This query joins `completed_routines` with the `routines` table to fetch the routine's name
     * for each history entry. The results are ordered by completion date, with the most recent first.
     *
     * @return A [Flow] emitting a list of [CompletedRoutineHistoryItem]s.
     */
    @Query("""
    SELECT R.id as routineId, R.name, CR.completionDate , CR.completionNote
    FROM completed_routines AS CR
    JOIN routines AS R ON CR.routineId = R.id
    ORDER BY CR.completionDate DESC
""")
    fun getAllCompletedRoutinesWithName(): Flow<List<CompletedRoutineHistoryItem>>

    /**
     * Deletes all records from the `completed_routines` table.
     */
    @Query("DELETE FROM completed_routines")
    suspend fun deleteAllCompletedRoutines()

    /**
     * Finds the most recently completed routine and returns its details.
     *
     * A JOIN is performed to get the routine name from the 'routines' table.
     * The result is limited to the single most recent entry based on completion date.
     *
     * @return A [Flow] emitting the [CompletedRoutineHistoryItem] for the latest completion, or null if none exist.
     */
    @Query("SELECT r.id as routineId, r.name as name, c.completionDate, c.completionNote FROM completed_routines c JOIN routines r ON c.routineId = r.id ORDER BY c.completionDate DESC LIMIT 1")
    fun getLatestCompletedRoutine(): Flow<CompletedRoutineHistoryItem?>

    /**
     * Gets the number of completed routines for each day since a given date.
     *
     * The 'strftime' function is used to handle date grouping in SQLite.
     * It groups results by the start of the day and is suitable for displaying activity charts.
     *
     * @param since The start date from which to begin counting.
     * @return A [Flow] emitting a list of [DailyRoutineCount] objects.
     */
    @Query("""
    SELECT
        strftime('%s', date(completionDate / 1000, 'unixepoch', 'start of day')) * 1000 as day,
        COUNT(id) as count
    FROM completed_routines
    WHERE completionDate >= :since
    GROUP BY 1 
    ORDER BY 1 ASC 
""")
    fun getDailyCounts(since: Date): Flow<List<DailyRoutineCount>>

    /**
     * Gets the total count of all completed routines since a given date.
     *
     * @param since The start date from which to begin counting.
     * @return A [Flow] emitting the total integer count.
     */
    @Query("SELECT COUNT(id) FROM completed_routines WHERE completionDate >= :since")
    fun getCompletedCountSince(since: Date): Flow<Int>
}

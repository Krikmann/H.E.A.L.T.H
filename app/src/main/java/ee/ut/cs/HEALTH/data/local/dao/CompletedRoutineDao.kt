package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ee.ut.cs.HEALTH.data.local.entities.CompletedRoutineEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date


data class CompletedRoutineHistoryItem(
    val name: String,
    val completionDate: java.util.Date
)
data class DailyRoutineCount(
    val day: Date,
    val count: Int
)

@Dao
interface CompletedRoutineDao {
    @Insert
    suspend fun insertCompletedRoutine(completedRoutine: CompletedRoutineEntity)

    @Query("""
        SELECT R.name, CR.completionDate
        FROM completed_routines AS CR
        JOIN routines AS R ON CR.routineId = R.id
        ORDER BY CR.completionDate DESC
    """)
    fun getAllCompletedRoutinesWithName(): Flow<List<CompletedRoutineHistoryItem>>

    @Query("DELETE FROM completed_routines")
    suspend fun deleteAllCompletedRoutines()
    /**
     * Finds the most recently completed routine (by the latest date) and returns its name.
     * A JOIN is needed to get the routine name from the 'routines' table.
     */
    @Query("SELECT r.name as name, c.completionDate FROM completed_routines c JOIN routines r ON c.routineId = r.id ORDER BY c.completionDate DESC LIMIT 1")
    fun getLatestCompletedRoutine(): Flow<CompletedRoutineHistoryItem?>

    /**
     * Gets the number of completed routines for each of the last 7 days.
     * The 'unixepoch' function is used to handle date comparisons in SQLite.
     * It groups results by the start of the day.
     */
    @Query("""
        SELECT
            date(completionDate / 1000, 'unixepoch') as day,
            COUNT(id) as count
        FROM completed_routines
        WHERE completionDate >= :since
        GROUP BY day
    """)
    fun getDailyCounts(since: Date): Flow<List<DailyRoutineCount>>
}
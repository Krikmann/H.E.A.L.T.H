package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ee.ut.cs.HEALTH.data.local.entities.CompletedRoutineEntity
import kotlinx.coroutines.flow.Flow


data class CompletedRoutineHistoryItem(
    val name: String,
    val completionDate: java.util.Date
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
}
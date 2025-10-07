package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.*
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>
}
package ee.ut.cs.HEALTH.data.local.dao

import androidx.room.*
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for profile-related database operations.
 *
 * This interface defines the methods for interacting with the `profile` table,
 * which stores a single entry for the user's profile.
 */
@Dao
interface ProfileDao {
    /**
     * Inserts or replaces the user's profile.
     *
     * The `OnConflictStrategy.REPLACE` ensures that if a profile already exists
     * (identified by its primary key), it will be replaced with the new data,
     * maintaining a single profile entry in the database.
     *
     * @param profile The [ProfileEntity] to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    /**
     * Retrieves the user's profile from the database.
     *
     * It queries for the profile with a fixed ID (0) and returns it as a [Flow].
     * This allows the UI to observe changes to the profile data reactively.
     *
     * @return A [Flow] emitting the [ProfileEntity], or `null` if no profile exists.
     */
    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>
}

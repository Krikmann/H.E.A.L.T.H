package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ee.ut.cs.HEALTH.R

/**
 * Represents the user's profile data in the local database.
 *
 * This entity defines the structure of the "profile" table. It holds a single entry for the
 * application user, identified by a fixed primary key.
 *
 * @property id The primary key for the profile entry. A fixed value (e.g., 0) is used to ensure a single profile record.
 * @property profilePicture A resource ID for the user's chosen profile picture, with a default image.
 * @property nameOfUser The user's full name.
 * @property emailOfUser The user's email address.
 * @property phoneNumber The user's phone number.
 * @property dateOfBirth The user's date of birth, stored as a string.
 * @property description A short bio or description provided by the user.
 * @property userHasSetTheirInfo A flag to indicate if the user has completed the initial profile setup.
 * @property weeklyGoal The user's target for number of workouts per week.
 * @property monthlyGoal The user's target for number of workouts per month.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val profilePicture: Int = R.drawable.default_profile_pic,
    val nameOfUser: String = "",
    val emailOfUser: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val description: String = "",
    val userHasSetTheirInfo: Boolean = false,
    @ColumnInfo(defaultValue = "4")
    val weeklyGoal: Int,

    @ColumnInfo(defaultValue = "16")
    val monthlyGoal: Int
)

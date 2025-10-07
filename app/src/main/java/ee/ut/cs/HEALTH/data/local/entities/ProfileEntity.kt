package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ee.ut.cs.HEALTH.R

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val profilePicture: Int = R.drawable.default_profile_pic,
    val nameOfUser: String = "",
    val emailOfUser: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val description: String = "",
    val userHasSetTheirInfo: Boolean = false
)
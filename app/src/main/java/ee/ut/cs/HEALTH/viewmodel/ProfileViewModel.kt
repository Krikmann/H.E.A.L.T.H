package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import ee.ut.cs.HEALTH.ui.screens.FormData
import kotlinx.coroutines.launch

/**
 * ViewModel for handling profile-related logic, such as saving user information.
 *
 * This class interacts with the [ProfileDao] to persist user profile data.
 *
 * @param profileDao The Data Access Object for the user's profile.
 */
class ProfileViewModel(private val profileDao: ProfileDao) : ViewModel() {

    /**
     * Saves or updates the user's profile information in the database.
     *
     * It takes the form data from the UI, maps it to a [ProfileEntity], and
     * inserts it into the database within a coroutine scope.
     *
     * @param formData The data collected from the profile editing form.
     */
    fun saveProfile(formData: FormData) {
        viewModelScope.launch {
            val profile = ProfileEntity(
                nameOfUser = "${formData.firstName} ${formData.lastName}",
                emailOfUser = formData.email,
                phoneNumber = formData.phone,
                dateOfBirth = "${formData.day}. ${formData.month} ${formData.year}",
                description = formData.description,
                userHasSetTheirInfo = true,
                weeklyGoal = formData.weeklyGoal.toIntOrNull() ?: 4,
                monthlyGoal = formData.monthlyGoal.toIntOrNull() ?: 16
            )
            profileDao.insertProfile(profile)
        }
    }
}

/**
 * Factory for creating [ProfileViewModel] instances.
 *
 * This factory is required because the [ProfileViewModel] has a constructor
 * that takes a [ProfileDao] as a dependency.
 *
 * @param profileDao The Data Access Object for the user's profile.
 */
class ProfileViewModelFactory(private val profileDao: ProfileDao) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(profileDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

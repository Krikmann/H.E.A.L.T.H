package ee.ut.cs.HEALTH.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import ee.ut.cs.HEALTH.ui.screens.FormData
import kotlinx.coroutines.launch

class ProfileViewModel(private val profileDao: ProfileDao) : ViewModel() {

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
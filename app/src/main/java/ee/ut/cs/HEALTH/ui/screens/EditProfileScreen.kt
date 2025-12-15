package ee.ut.cs.HEALTH.ui.screens

import android.util.Patterns.EMAIL_ADDRESS
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.ui.navigation.NavDestination
import ee.ut.cs.HEALTH.viewmodel.ProfileViewModel
import ee.ut.cs.HEALTH.viewmodel.ProfileViewModelFactory

/**
 * A data class to hold the validated data from the profile editing form.
 *
 * @property firstName The user's first name.
 * @property lastName The user's last name.
 * @property phone The user's phone number.
 * @property email The user's email address.
 * @property day The selected day of birth.
 * @property month The selected month of birth.
 * @property year The selected year of birth.
 * @property description A short bio or description provided by the user.
 * @property weeklyGoal The user's target for workouts per week.
 * @property monthlyGoal The user's target for workouts per month.
 */
data class FormData(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val day: String,
    val month: String,
    val year: String,
    val description: String,
    val weeklyGoal: String,
    val monthlyGoal: String
)

/**
 * A screen for creating or editing the user's profile.
 *
 * This composable function provides a form for the user to input their personal information,
 * such as name, contact details, birthday, and workout goals. It performs validation on the
 * input fields and displays errors accordingly.
 *
 * It uses the [ProfileViewModel] to save the data and handles navigation back to the
 * profile screen upon successful save. The screen's title dynamically changes between
 * "Create profile" and "Edit profile" based on whether a profile already exists.
 *
 * @param profileDao The Data Access Object for fetching and saving profile data.
 * @param navController The [NavController] for handling navigation.
 * @param darkMode A boolean indicating if dark mode is currently enabled (passed down but not used directly).
 * @param onToggleDarkMode A lambda function to toggle the dark mode setting (passed down but not used directly).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profileDao: ProfileDao,
    navController: NavController,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val profile by profileDao.getProfile().collectAsState(initial = null)

    var firstName by remember { mutableStateOf("") }
    var firstNameError by remember { mutableStateOf(false) }
    var lastName by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf(false) }

    var phone by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }

    val days = (1..31).map { it.toString() }
    val months =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val years = (1900..2025).map { it.toString() }.reversed()

    var dayExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    var daySelected by remember { mutableStateOf("") }
    var monthSelected by remember { mutableStateOf("") }
    var yearSelected by remember { mutableStateOf("") }

    var description by remember { mutableStateOf("") }
    var weeklyGoal by remember { mutableStateOf("4") }
    var monthlyGoal by remember { mutableStateOf("16") }

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(profileDao)
    )

    LaunchedEffect(profile) {
        profile?.let {
            firstName = it.nameOfUser.split(" ").getOrNull(0) ?: ""
            lastName = it.nameOfUser.split(" ").getOrNull(1) ?: ""
            phone = it.phoneNumber
            email = it.emailOfUser

            val parts = it.dateOfBirth.split(". ", " ")
            daySelected = parts.getOrNull(0) ?: "1"
            monthSelected = parts.getOrNull(1) ?: months[0]
            yearSelected = parts.getOrNull(2) ?: years.first()
            description = it.description
            weeklyGoal = it.weeklyGoal.toString()
            monthlyGoal = it.monthlyGoal.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 8.dp)
        ) {
            Button(
                onClick = {
                    emailError = !EMAIL_ADDRESS.matcher(email).matches()
                    firstNameError =
                        firstName.isEmpty() || !firstName.all { it.isLetter() || it == ' ' || it == '-' }
                    lastNameError =
                        lastName.isEmpty() || !lastName.all { it.isLetter() || it == ' ' || it == '-' }
                    phoneError =
                        phone.isEmpty() || !phone.all { it.isDigit() || it == ' ' || it == '-' }

                    if (!emailError && !firstNameError && !lastNameError && !phoneError) {
                        val formData = FormData(
                            firstName,
                            lastName,
                            phone,
                            email,
                            daySelected,
                            monthSelected,
                            yearSelected,
                            description,
                            weeklyGoal.ifBlank { "0" },
                            monthlyGoal.ifBlank { "0" }
                        )

                        viewModel.saveProfile(formData)
                        navController.navigate(NavDestination.PROFILE.route) {
                            popUpTo(NavDestination.PROFILE.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (profile?.userHasSetTheirInfo ?: false) Text(
            text = "Edit profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        else Text(
            text = "Create profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { it ->
                    firstName = it
                    if (firstNameError) firstNameError =
                        firstName.isEmpty() || !firstName.all { it.isLetter() || it == ' ' || it == '-' }
                },
                label = { Text("First name") },
                isError = firstNameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { it ->
                    lastName = it; if (lastNameError) lastNameError =
                    lastName.isEmpty() || !lastName.all { it.isLetter() || it == ' ' || it == '-' }
                },
                label = { Text("Last name") },
                isError = lastNameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (firstNameError) Text("Enter a valid first name", color = Red, fontSize = 12.sp)
            else Spacer(Modifier)

            if (lastNameError) Text("Enter a valid last name", color = Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { it ->
                phone = it; if (phoneError) phoneError =
                phone.isEmpty() || !phone.all { it.isDigit() || it == ' ' || it == '-' }
            },
            label = { Text("Phone number") },
            isError = phoneError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        if (phoneError) Text(
            text = "Enter a valid phone number",
            fontSize = 12.sp,
            color = Red,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it; if (emailError) emailError = !EMAIL_ADDRESS.matcher(it).matches()
            },
            label = { Text("Email") },
            isError = emailError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        if (emailError) Text(
            text = "Enter a valid email",
            fontSize = 12.sp,
            color = Red,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = dayExpanded,
                onExpandedChange = { dayExpanded = !dayExpanded },
                modifier = Modifier.weight(0.84f)
            ) {
                OutlinedTextField(
                    value = daySelected,
                    onValueChange = {},
                    label = { Text("Day") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) })
                ExposedDropdownMenu(
                    expanded = dayExpanded,
                    onDismissRequest = { dayExpanded = false }) {
                    days.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d) },
                            onClick = { daySelected = d; dayExpanded = false })
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = { monthExpanded = !monthExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = monthSelected,
                    onValueChange = {},
                    label = { Text("Month") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) })
                ExposedDropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false }) {
                    months.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = { monthSelected = m; monthExpanded = false })
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = yearExpanded,
                onExpandedChange = { yearExpanded = !yearExpanded },
                modifier = Modifier.weight(0.9f)
            ) {
                OutlinedTextField(
                    value = yearSelected,
                    onValueChange = {},
                    label = { Text("Year") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) })
                ExposedDropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false }) {
                    years.forEach { y ->
                        DropdownMenuItem(
                            text = { Text(y) },
                            onClick = { yearSelected = y; yearExpanded = false })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = weeklyGoal,
                onValueChange = { weeklyGoal = it.filter { c -> c.isDigit() } },
                label = { Text("Weekly Goal") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = monthlyGoal,
                onValueChange = { monthlyGoal = it.filter { c -> c.isDigit() } },
                label = { Text("Monthly Goal") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 16.dp),
            maxLines = 6
        )
    }
}

package ee.ut.cs.HEALTH.ui.screens

import android.util.Patterns.EMAIL_ADDRESS
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.entities.ProfileEntity
import ee.ut.cs.HEALTH.domain.model.Profile.userHasSetTheirInfo
import ee.ut.cs.HEALTH.ui.navigation.NavDestination
import ee.ut.cs.HEALTH.viewmodel.ProfileViewModel
import ee.ut.cs.HEALTH.viewmodel.ProfileViewModelFactory

// Simple form output type
data class FormData(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val day: String,
    val month: String,
    val year: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(profileDao: ProfileDao, navController: NavController) {
    val profile by profileDao.getProfile().collectAsState(initial = null)

    var firstName by remember { mutableStateOf("") }
    var firstNameError by remember { mutableStateOf(false) }
    var lastName by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf(false) }

    var phone by remember { mutableStateOf("") }
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
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Save button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = {
                emailError = !EMAIL_ADDRESS.matcher(email).matches()
                firstNameError = firstName.isEmpty();
                lastNameError = lastName.isEmpty();

                if (!emailError && !firstNameError && !lastNameError) {
                    val formData = FormData(
                        firstName,
                        lastName,
                        phone,
                        email,
                        daySelected,
                        monthSelected,
                        yearSelected,
                        description
                    )

                    viewModel.saveProfile(formData)
                    navController.navigate(NavDestination.PROFILE.route) {
                        popUpTo(NavDestination.PROFILE.route) { inclusive = true } // optional: remove edit from backstack
                    }
                }
            }) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (profile?.userHasSetTheirInfo ?: true) Text(
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

        // First and last name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First name
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    if (firstNameError) firstNameError = firstName.isEmpty()
                },
                label = { Text("First name") }, // always show as description
                isError = firstNameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            // Last name
            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it; if (lastNameError) lastNameError = lastName.isEmpty()
                },
                label = { Text("Last name") },
                isError = lastNameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        // First and last name error messages
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (firstNameError) Text("Enter your first name", color = Red, fontSize = 12.sp)
            else Spacer(Modifier) // keeps spacing if first name has no error

            if (lastNameError) Text("Enter your last name", color = Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Phone number
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it; if (emailError) emailError = !EMAIL_ADDRESS.matcher(it).matches()
            },
            label = { Text("Email") },
            isError = emailError,
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError) Text(
            text = "Enter a valid email",
            fontSize = 12.sp,
            color = Red,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Birthday
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Day
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

            // Month
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

            // Year
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

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            maxLines = 6
        )
    }
}

package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.R
import ee.ut.cs.HEALTH.ui.navigation.NavDestination

/**
 * A composable that displays the user's profile information.
 *
 * This screen fetches the user's profile data from the [ProfileDao] and presents it in a
 * read-only format. It includes the user's name, profile picture, contact details, goals,
 * and a description. An "Edit" button allows the user to navigate to the [EditProfileScreen].
 *
 * @param profileDao The Data Access Object for fetching the user's profile.
 * @param navController The [NavController] used for handling navigation, specifically to the edit profile screen.
 * @param darkMode A boolean indicating if dark mode is currently enabled (not directly used here but passed down).
 * @param onToggleDarkMode A lambda function to toggle the dark mode setting (not directly used here but passed down).
 */
@Composable
fun ProfileScreen(
    profileDao: ProfileDao,
    navController: NavController,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val profile by profileDao.getProfile().collectAsState(initial = null)

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
                    navController.navigate(NavDestination.EDITPROFILE.route) {
                        launchSingleTop = true // Prevents multiple instances of EditProfileScreen
                    }
                },
                modifier = Modifier.width(120.dp)
            ) {
                Text("Edit")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(
                id = profile?.profilePicture ?: R.drawable.default_profile_pic
            ),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(128.dp)

        )

        Text(
            text = profile?.nameOfUser ?: "John Doe",
            modifier = Modifier.padding(top = 16.dp),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(start = 32.dp)) {
                Text(text = "Email", fontWeight = FontWeight.Light, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Phone", fontWeight = FontWeight.Light, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Birthday", fontWeight = FontWeight.Light, fontSize = 16.sp)
            }

            Column(
                modifier = Modifier.padding(end = 32.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = profile?.emailOfUser ?: "example@email.com", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = profile?.phoneNumber ?: "5555 5555", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = profile?.dateOfBirth ?: "Not specified", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your Goals",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Weekly Goal:", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${profile?.weeklyGoal ?: 4} workouts",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Monthly Goal:", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${profile?.monthlyGoal ?: 16} workouts",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(top = 32.dp, start = 32.dp, end = 32.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Description",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = profile?.description ?: "", fontSize = 16.sp
            )
        }
    }
}

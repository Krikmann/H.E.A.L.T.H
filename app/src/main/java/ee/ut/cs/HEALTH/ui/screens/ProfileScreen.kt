package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.HEALTH.domain.model.Profile

@Composable
fun ProfileScreen() {
    if (Profile.userHasSetTheirInfo) askForInfo()
    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 128.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(  //Profile picture
                painter = painterResource(id = Profile.profilePicture),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(128.dp)
            )
            Text(   // User's name
                text = Profile.nameOfUser,
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Row(    // Other info
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(start = 32.dp)) {   // Left Column
                    Text(text = "Email", fontWeight = FontWeight.Light, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Phone", fontWeight = FontWeight.Light, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Birthday", fontWeight = FontWeight.Light, fontSize = 16.sp)
                }
                Column(modifier = Modifier.padding(end = 32.dp),
                    horizontalAlignment = Alignment.End) {  // Right Column
                    Text(text = Profile.emailOfUser, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = Profile.phoneNumber, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = Profile.dateOfBirth, fontSize = 16.sp)
                }
            }
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(
                    text = "Description",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(   // Description
                    text = Profile.description, fontSize = 16.sp
                )
            }
        }
    }
}

fun askForInfo() {

}

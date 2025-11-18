import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkModeTopBar(
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    TopAppBar(
        title = { Text("App Title") }, // optional
        actions = {
            IconButton(onClick = { onToggleDarkMode(!darkMode) }) {
                Icon(
                    imageVector = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Toggle Dark Mode"
                )
            }
        }
    )
}

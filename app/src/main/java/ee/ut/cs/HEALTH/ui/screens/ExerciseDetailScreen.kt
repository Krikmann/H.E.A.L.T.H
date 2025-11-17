package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ee.ut.cs.HEALTH.viewmodel.ExerciseDetailViewModel
import coil.compose.AsyncImage
import ee.ut.cs.HEALTH.R


@Composable
fun ExerciseDetailScreen(viewModel: ExerciseDetailViewModel) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Show a loading indicator while the data is being fetched.
            state.isLoading -> {
                CircularProgressIndicator()
            }
            // If an error occurred, display the error message.
            state.error != null -> {
                // Create a local immutable variable to enable smart casting.
                val errorMessage = state.error
                Text(text = errorMessage.orEmpty(), style = MaterialTheme.typography.bodyLarge)
            }
            // If data is available, display the exercise details using a safe 'let' block.
            state.data != null -> {
                state.data?.let { data ->
                    // Inside this 'let' block, 'data' is guaranteed to be non-null.
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(data.name, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                    }
                }
            }
            // Handle the case where loading is finished but no data was found.
            else -> {
                Text(text = "Exercise not found.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

package ee.ut.cs.HEALTH.ui.screens

import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import ee.ut.cs.HEALTH.viewmodel.ExerciseDetailViewModel

/**
 * Displays the detailed information for a single exercise.
 *
 * This screen fetches data from the ViewModel and shows the exercise name,
 * a video or an image, an overview, and lists of target and secondary muscles.
 * It handles loading and error states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(viewModel: ExerciseDetailViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // The TopAppBar title updates dynamically with the exercise name once loaded.
            TopAppBar(title = { Text(state.data?.name ?: "Loading...") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }
                state.error != null -> {
                    Text(text = state.error!!, style = MaterialTheme.typography.bodyLarge)
                }
                state.data != null -> {
                    val data = state.data!!
                    // Use LazyColumn to make the entire detail view scrollable.
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Media item: Video or Image
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                // Prefer video if available, otherwise show the image.
                                if (!data.videoUrl.isNullOrBlank()) {
                                    VideoPlayer(url = data.videoUrl)
                                } else if (!data.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = data.imageUrl,
                                        contentDescription = data.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }

                        // Overview/Description section
                        if (!data.overview.isNullOrBlank()) {
                            item {
                                InfoSection("Overview", listOf(data.overview))
                            }
                        }

                        // Instructions section
                        if (data.instructions.isNotEmpty()) {
                            item {
                                InfoSection("Instructions", data.instructions)
                            }
                        }

                        // Body Parts section
                        if (data.bodyParts.isNotEmpty()) {
                            item {
                                InfoSection("Body Parts", data.bodyParts)
                            }
                        }

                        // Target Muscles section
                        if (data.targetMuscles.isNotEmpty()) {
                            item {
                                InfoSection("Target Muscles", data.targetMuscles)
                            }
                        }

                        // Secondary Muscles section
                        if (data.secondaryMuscles.isNotEmpty()) {
                            item {
                                InfoSection("Secondary Muscles", data.secondaryMuscles)
                            }
                        }
                    }
                }
                else -> {
                    Text(text = "Exercise not found.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/**
 * A reusable composable that displays a title and a list of informational items.
 *
 * @param title The title of the section.
 * @param items The list of strings to display.
 */
@Composable
private fun InfoSection(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        HorizontalDivider()
        items.forEach { item ->
            Text(
                // The text is capitalized for better readability (e.g., "CALVES" -> "Calves")
                text = item.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/**
 * A composable that displays a video from a URL using ExoPlayer.
 *
 * @param url The URL of the video to play.
 */
@Composable
private fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true // Autoplay
            repeatMode = ExoPlayer.REPEAT_MODE_ONE // Loop the video
        }
    }

    DisposableEffect(
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false // Hide video controls for a cleaner look
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    ) {
        onDispose { exoPlayer.release() }
    }
}

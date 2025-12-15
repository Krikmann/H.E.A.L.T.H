package ee.ut.cs.HEALTH.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ee.ut.cs.HEALTH.viewmodel.ExerciseDetailViewModel

/**
 * Displays the detailed information for a single exercise.
 *
 * This screen fetches data from the [ExerciseDetailViewModel] and shows the exercise name,
 * a video or an image, an overview, instructions, and lists of muscle groups.
 * It handles the UI for loading, error, and success states. The screen is built using a [Scaffold]
 * with a dynamic top app bar.
 *
 * @param viewModel The [ExerciseDetailViewModel] instance that provides the state for this screen.
 * @param navController The [NavController] used for handling navigation actions, such as closing the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(viewModel: ExerciseDetailViewModel,
                         navController: NavController) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.data?.name ?: "Loading...") },
                actions = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
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

                        if (!data.overview.isNullOrBlank()) {
                            item {
                                InfoSection("Overview", listOf(data.overview))
                            }
                        }

                        if (data.instructions.isNotEmpty()) {
                            item {
                                InfoSection("Instructions", data.instructions)
                            }
                        }

                        if (data.bodyParts.isNotEmpty()) {
                            item {
                                InfoSection("Body Parts", data.bodyParts)
                            }
                        }

                        if (data.targetMuscles.isNotEmpty()) {
                            item {
                                InfoSection("Target Muscles", data.targetMuscles)
                            }
                        }

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
 * A reusable composable that displays a styled section with a title and a list of items.
 *
 * Each item in the list is formatted to start with a capital letter.
 *
 * @param title The title of the information section.
 * @param items A list of strings to be displayed as bullet points or paragraphs under the title.
 */
@Composable
private fun InfoSection(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        items.forEach { item ->

            Text(
                text = item.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * A composable that displays a video from a URL using ExoPlayer.
 *
 * It embeds an [AndroidView] hosting a [PlayerView]. The video autoplays and loops.
 * The ExoPlayer instance is managed within a [DisposableEffect] to ensure it is properly
 * released when the composable leaves the screen.
 *
 * @param url The URL of the video to be played.
 */
@Composable
private fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
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

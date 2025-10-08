package ee.ut.cs.HEALTH.ui.screens
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import ee.ut.cs.HEALTH.domain.model.routine.RoutineId
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByDuration
import ee.ut.cs.HEALTH.domain.model.routine.SavedExerciseByReps
import ee.ut.cs.HEALTH.domain.model.routine.SavedRestDurationBetweenExercises
import ee.ut.cs.HEALTH.domain.model.routine.SavedRoutine

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        placeholder = { Text("Search routines") },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun SearchScreen(repository : RoutineRepository) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }

    val summaries by repository.getAllRoutineSummaries()
        .collectAsStateWithLifecycle(initialValue = emptyList())


    if (selectedId == null) {
        val filteredRoutines = remember(summaries, query) {
            val q = query.trim()
            if (q.isEmpty()) summaries else summaries.filter { it.name.contains(q, ignoreCase = true) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                items(filteredRoutines, key = { it.id.value }) { routine ->
                    Card(
                        onClick = {
                            selectedId = routine.id.value
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Box(Modifier.fillMaxSize().height(100.dp)) {
                            Text(routine.name, Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

        }
    } else {
        BackHandler {
            selectedId = null // go back to search results instead of closing the screen
        }

        val routineFlow = remember(selectedId) { repository.getRoutine(RoutineId(selectedId!!)) }
        val routine: SavedRoutine? by routineFlow.collectAsStateWithLifecycle(initialValue = null)

        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = { selectedId = null },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }

            routine?.let { r ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 72.dp)
                ) {
                    Text(r.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Text(r.description.orEmpty(), fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 16.dp))

                    LazyColumn {
                        items(r.routineItems) { item ->
                            when (item) {
                                is SavedExerciseByReps -> {
                                    Text("Name: ${item.exerciseDefinition.name}")
                                    Text("Sets: ${item.amountOfSets}")
                                    Text("Rest: ${item.recommendedRestDurationBetweenSets.inWholeSeconds}s")
                                    Text("Reps: ${item.countOfRepetitions}")
                                }
                                is SavedExerciseByDuration -> {
                                    Text("Name: ${item.exerciseDefinition.name}")
                                    Text("Sets: ${item.amountOfSets}")
                                    Text("Rest: ${item.recommendedRestDurationBetweenSets.inWholeSeconds}s")
                                    Text("Duration: ${item.duration.inWholeSeconds}s")
                                }
                                is SavedRestDurationBetweenExercises -> {
                                    Text("Rest for ${item.restDuration.inWholeSeconds}s")
                                }
                            }
                            Text("") // spacer
                        }
                    }
                }
            } ?: run {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Loading…") }
            }
        }
    }

}


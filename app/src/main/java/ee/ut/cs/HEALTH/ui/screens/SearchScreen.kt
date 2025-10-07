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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.dto.RoutineItemDto
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseDefinitionId
import ee.ut.cs.HEALTH.data.local.entities.ExerciseType
import ee.ut.cs.HEALTH.data.local.entities.RoutineEntity
import ee.ut.cs.HEALTH.data.local.entities.RoutineId
import ee.ut.cs.HEALTH.data.local.entities.RoutineItemType

var query by mutableStateOf("")

@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        placeholder = { Text("Search routines") },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true
    )
}

@Composable
fun SearchScreen(dao : RoutineDao) {
    val routines by dao.getAllRoutines().collectAsState(initial = emptyList())
    val filteredRoutines = routines.filter { it.name.contains(query.trim()) }
    var selectedId by remember { mutableStateOf("") }

    if (selectedId == "") {
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
                items(filteredRoutines) { routine ->
                    Card(
                        onClick = {
                            selectedId = routine.id.value.toString()
                        },
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Box(Modifier.fillMaxSize().height(100.dp)) {
                            Text(routine.name, Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
            SearchBar(modifier = Modifier.align(Alignment.BottomCenter))

        }
    } else {
        BackHandler {
            selectedId = "" // go back to search results instead of closing the screen
        }

        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = { selectedId = ""},
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
            var activeRoutine: RoutineEntity?
            var routineName by remember { mutableStateOf("") }
            var routineDescription by remember { mutableStateOf("") }
            var routineItems by remember { mutableStateOf<List<RoutineItemDto>>(emptyList()) }

            LaunchedEffect(selectedId) {
                activeRoutine = dao.getRoutine(RoutineId(selectedId.toLong()))?.routine
                routineName = activeRoutine?.name.toString()
                routineDescription = activeRoutine?.description.toString()
                routineItems = dao.getRoutineItemsOrdered(RoutineId(selectedId.toLong()))


            }
            Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(top = 72.dp)) {
                Text(
                    text = routineName,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = routineDescription,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )


                LazyColumn {
                    var exerciseName: String
                    items(routineItems) { item ->
                        Text("")
                        val restOrExercise = item.routineItem.type
                        exerciseName =
                            dao.getExerciseDefinition(item.exercise?.exercise?.exerciseDefinitionId)
                                .collectAsState(
                                    initial = ExerciseDefinitionEntity(
                                        id = ExerciseDefinitionId(0), name = ""
                                    )
                                ).value?.name.toString()

                        val type = item.exercise?.exercise?.type

                        if (restOrExercise == RoutineItemType.EXERCISE) {
                            Text("Name: $exerciseName")
                            if (type == ExerciseType.REPS) {
                                val amountOfReps = item.exercise.byReps?.countOfRepetitions
                                Text("Repetitions: $amountOfReps")
                            } else if (type == ExerciseType.DURATION) {
                                val duration = item.exercise.byDuration?.durationInSeconds
                                Text("Duration: $duration seconds")
                            }


                        } else if (restOrExercise == RoutineItemType.REST) {
                            val restSeconds = item.restDurationBetweenExercises?.durationInSeconds
                            Text("Rest for $restSeconds seconds")
                        }

                    }
                }
            }
        }
    }

}


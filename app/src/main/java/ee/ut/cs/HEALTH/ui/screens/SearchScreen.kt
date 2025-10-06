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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.domain.model.Profile

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
        ){
            items(filteredRoutines) { routine ->
                Card(
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

}


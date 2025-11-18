package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineHistoryItem
import ee.ut.cs.HEALTH.domain.model.routine.summary.RoutineSummary
import ee.ut.cs.HEALTH.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import ee.ut.cs.HEALTH.data.local.dao.DailyRoutineCount
import java.util.Calendar
import com.patrykandpatrick.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.component.shape.Shapes
import ee.ut.cs.HEALTH.ui.navigation.DarkModeTopBar
import ee.ut.cs.HEALTH.ui.navigation.NavDestination
import java.util.Date

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
    darkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    // Collect the data streams from the ViewModel as state.
    val recentActivity by viewModel.recentActivity.collectAsStateWithLifecycle()
    val newestRoutine by viewModel.newestRoutine.collectAsStateWithLifecycle()
    val mostPopularRoutine by viewModel.mostPopularRoutine.collectAsStateWithLifecycle()
    val weeklyActivity by viewModel.weeklyActivity.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main title
        Text(
            text = "Welcome to your HEALTH app",
            style = MaterialTheme.typography.headlineMedium
        )
        WeeklyActivityChart(
            dailyCounts = weeklyActivity,
            modelProducer = viewModel.chartModelProducer
        )
        InfoCard(
            title = "Your Recent Activity",
            routineItem = recentActivity,
            placeholder = "No completed routines yet.",
            onClick = {
                navController.navigate(NavDestination.STATS.route)
            }
        )

        InfoCard(
            title = "Newest Routine",
            routineItem = newestRoutine,
            placeholder = "No routines added yet.",
            onClick = {
                newestRoutine?.let { routine ->
                    val route = NavDestination.SEARCH.route.replace(
                        "{routineId}",
                        routine.id.value.toString()
                    )
                    navController.navigate(route)
                }
            }
        )

        InfoCard(
            title = "Most Popular Routine",
            routineItem = mostPopularRoutine,
            placeholder = "Complete routines to find a favorite!",
            onClick = {
                mostPopularRoutine?.let { routine ->
                    val route = NavDestination.SEARCH.route.replace(
                        "{routineId}",
                        routine.id.value.toString()
                    )
                    navController.navigate(route)
                }
            }
        )
    }
}

/**
 * A reusable Composable for displaying information in a styled card.
 * It is generic (<T>) to handle different data types like RoutineSummary
 * and CompletedRoutineHistoryItem.
 *
 * @param title The title to display above the card.
 * @param routineItem The data item to display. Can be null.
 * @param placeholder The text to show if routineItem is null.
 */
@Composable
private fun <T> InfoCard(title: String, routineItem: T?, placeholder: String, onClick: () -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = { if (routineItem != null) onClick() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (routineItem != null) {
                    // Use a 'when' block to display data based on its type.
                    when (routineItem) {
                        is CompletedRoutineHistoryItem -> {
                            val formatter =
                                SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            Text(
                                routineItem.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Completed on: ${formatter.format(routineItem.completionDate)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        is RoutineSummary -> {
                            Text(
                                routineItem.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            routineItem.description?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        else -> {
                            // Fallback for any unexpected data type.
                            Text(
                                "Unsupported data type",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    // Display the placeholder text if no data is available.
                    Text(placeholder, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun WeeklyActivityChart(
    dailyCounts: List<DailyRoutineCount>,
    modelProducer: ChartEntryModelProducer
) {
    val dayFormatter = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    val last7DaysMap = remember {
        (0..6).map { i ->
            val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            dayFormatter.format(calendar.time) to 0
        }.reversed().toMap(LinkedHashMap())
    }

    dailyCounts.forEach { dailyCount ->
        val dayKey = dayFormatter.format(dailyCount.day)
        if (last7DaysMap.containsKey(dayKey)) {
            last7DaysMap[dayKey] = dailyCount.count
        }
    }

    val chartEntries = last7DaysMap.entries.mapIndexed { index, entry ->
        entryOf(index.toFloat(), entry.value)
    }
    modelProducer.setEntries(chartEntries)

    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        last7DaysMap.keys.elementAt(value.toInt())
    }

    // Theme-aware axis text
    val axisTextComponent = textComponent(
        color = MaterialTheme.colorScheme.onBackground
    )

    val startAxis = rememberStartAxis(
        itemPlacer = remember {
            AxisItemPlacer.Vertical.default(maxItemCount = 3)
        },
        label = axisTextComponent
    )

    val bottomAxis = rememberBottomAxis(
        valueFormatter = bottomAxisValueFormatter,
        label = axisTextComponent
    )

    Column {
        Text(
            text = "Weekly Activity",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (chartEntries.isNotEmpty()) {
                Chart(
                    chart = columnChart(
                        columns = listOf(
                            lineComponent(
                                color = MaterialTheme.colorScheme.primary,
                                thickness = 12.dp,
                                shape = Shapes.roundedCornerShape(all = 4.dp)
                            )
                        )
                    ),
                    chartModelProducer = modelProducer,
                    startAxis = startAxis,
                    bottomAxis = bottomAxis,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No activity this week.", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

package ee.ut.cs.HEALTH.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
            },
            navController = navController
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
            },
            navController = navController
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
            },
            navController = navController
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
private fun <T> InfoCard(
    title: String,
    routineItem: T?,
    placeholder: String,
    onClick: () -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { if (routineItem != null) onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Valime ikooni vastavalt pealkirjale
                val icon = when (title) {
                    "Your Recent Activity" -> Icons.Default.History
                    "Newest Routine" -> Icons.Default.FiberNew
                    "Most Popular Routine" -> Icons.Default.Star
                    else -> null
                }


                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )

            if (routineItem != null) {
                when (routineItem) {
                    is CompletedRoutineHistoryItem -> {
                        Text(
                            routineItem.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        Text(
                            "Completed on: ${formatter.format(routineItem.completionDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is RoutineSummary -> {

                        Text(
                            routineItem.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        routineItem.description?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }

                    else -> {
                        Text("Unsupported data type", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {

                Text(placeholder, style = MaterialTheme.typography.bodyMedium)


                if (title == "Newest Routine") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { navController.navigate(NavDestination.ADD.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add new routine")
                    }
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart, // Lisame ikooni
                        contentDescription = "Weekly Activity",
                        tint = MaterialTheme.colorScheme.primary, // Värv siniseks
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Weekly Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                        Text(
                            "No activity this week.",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

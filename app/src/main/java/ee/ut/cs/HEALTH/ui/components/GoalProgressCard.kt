package ee.ut.cs.HEALTH.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A composable card that displays the user's progress towards their weekly and monthly workout goals.
 *
 * This card provides a high-level overview of the user's performance by showing two circular
 * progress indicators: one for the weekly goal and one for the monthly goal.
 *
 * @param weeklyGoal The user's target number of workouts for the week.
 * @param weeklyProgress The number of workouts the user has completed this week.
 * @param monthlyGoal The user's target number of workouts for the month.
 * @param monthlyProgress The number of workouts the user has completed this month.
 */
@Composable
fun GoalProgressCard(
    weeklyGoal: Int,
    weeklyProgress: Int,
    monthlyGoal: Int,
    monthlyProgress: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = "Goals",
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Your Goals",
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoalIndicator(
                    title = "This Week",
                    progress = weeklyProgress,
                    goal = weeklyGoal
                )
                GoalIndicator(
                    title = "This Month",
                    progress = monthlyProgress,
                    goal = monthlyGoal
                )
            }
        }
    }
}

/**
 * A private composable that displays a circular progress indicator for a single goal.
 *
 * It visualizes the progress as a circular arc and shows the progress text (e.g., "5/10")
 * and a title (e.g., "This Week") in the center. The progress animation is handled smoothly
 * using [animateFloatAsState].
 *
 * @param title The title to be displayed below the progress text (e.g., "This Week").
 * @param progress The current progress value.
 * @param goal The target goal value.
 * @param size The diameter of the circular indicator. Defaults to 100.dp.
 * @param strokeWidth The thickness of the progress arc. Defaults to 8.dp.
 */
@Composable
private fun GoalIndicator(
    title: String,
    progress: Int,
    goal: Int,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp
) {
    val progressFraction = if (goal > 0) progress.toFloat() / goal.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        label = "progressAnimation"
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$progress/$goal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

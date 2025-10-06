package ee.ut.cs.HEALTH

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.lifecycle.lifecycleScope
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.database.AppDatabase
import ee.ut.cs.HEALTH.data.local.database.TestData
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByDurationEntity
import ee.ut.cs.HEALTH.data.local.entities.ExerciseByRepsEntity
import ee.ut.cs.HEALTH.ui.components.MainNavigationBar
import ee.ut.cs.HEALTH.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.room.Room


class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var dao: RoutineDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "health-db"
        ).build()

        dao = db.routineDao()


        lifecycleScope.launch {
            insertTestData()
        }

        setContent {
            MyApplicationTheme {
                MainNavigationBar()
            }
        }
    }

    private suspend fun insertTestData() {
        TestData.testDefinitions.forEach { dao.insertExerciseDefinition(it) }
        dao.insertRoutine(TestData.testRoutine)
        TestData.testExercises.forEach {
            when (it) {
                is ExerciseByRepsEntity -> dao.insertExerciseByReps(it)
                is ExerciseByDurationEntity -> dao.insertExerciseByDuration(it)
            }
        }
        dao.insertRest(TestData.testRestItem)
    }
}

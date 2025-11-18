package ee.ut.cs.HEALTH

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.remember

import androidx.lifecycle.lifecycleScope
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.database.AppDatabase
import ee.ut.cs.HEALTH.data.local.database.TestData
import ee.ut.cs.HEALTH.ui.components.MainNavigationBar
import ee.ut.cs.HEALTH.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.room.Room
import ee.ut.cs.HEALTH.data.local.dao.CompletedRoutineDao
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository
import kotlinx.coroutines.Dispatchers



class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var dao: RoutineDao
    private lateinit var profileDao: ProfileDao
    private lateinit var completedRoutineDao: CompletedRoutineDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "health-db"
        )
            .fallbackToDestructiveMigration()  // for if database schema changes
            .build()

        dao = db.routineDao()
        profileDao = db.profileDao()
        completedRoutineDao = db.completedRoutineDao()


        lifecycleScope.launch(Dispatchers.IO) {
            insertTestData()
        }

        val repository = RoutineRepository(db, dao, completedRoutineDao)

        setContent {
            var darkMode by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = darkMode) {
                MainNavigationBar(
                    dao = dao,
                    profileDao = profileDao,
                    repository = repository,
                    darkMode = darkMode,
                    onToggleDarkMode = { darkMode = it }
                )
            }
        }
    }

    private suspend fun insertTestData() {
        completedRoutineDao.deleteAllCompletedRoutines()
        dao.deleteAllRests()
        dao.deleteAllExercisesByDuration()
        dao.deleteAllExercisesByReps()
        dao.deleteAllExercises()
        dao.deleteAllRoutineItems()
        dao.deleteAllRoutines()
        dao.deleteAllExerciseDefinitions()

        TestData.testExerciseDefinitions.forEach { dao.upsertExerciseDefinition(it) }
        TestData.testRoutines.forEach { dao.upsertRoutine(it) }
        TestData.testRoutineItems.forEach { dao.upsertRoutineItem(it) }
        TestData.testExerciseEntities.forEach { dao.upsertExercise(it) }
        TestData.testExercisesByReps.forEach { dao.upsertExerciseByReps(it) }
        TestData.testExercisesByDuration.forEach { dao.upsertExerciseByDuration(it) }
        TestData.testRestItems.forEach { dao.upsertRest(it) }
        TestData.testCompletedRoutines.forEach { completedRoutineDao.insertCompletedRoutine(it) }
    }
}

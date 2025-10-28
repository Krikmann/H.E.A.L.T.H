package ee.ut.cs.HEALTH

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.lifecycle.lifecycleScope
import ee.ut.cs.HEALTH.data.local.dao.RoutineDao
import ee.ut.cs.HEALTH.data.local.database.AppDatabase
import ee.ut.cs.HEALTH.data.local.database.TestData
import ee.ut.cs.HEALTH.ui.components.MainNavigationBar
import ee.ut.cs.HEALTH.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import androidx.room.Room
import ee.ut.cs.HEALTH.data.local.dao.ProfileDao
import ee.ut.cs.HEALTH.data.local.repository.RoutineRepository


class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var dao: RoutineDao
    private lateinit var profileDao: ProfileDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "health-db"
        )
            .fallbackToDestructiveMigration(false)  // for if database schema changes
            .build()

        dao = db.routineDao()
        profileDao = db.profileDao()


        lifecycleScope.launch {
            insertTestData()
        }

        val repository = RoutineRepository(db, dao)

        setContent {
            MyApplicationTheme {
                MainNavigationBar(
                    dao = dao,
                    profileDao = profileDao,
                    repository = repository
                )
            }
        }
    }

    private suspend fun insertTestData() {
        dao.deleteAllRests()
        dao.deleteAllExercisesByDuration()
        dao.deleteAllExercisesByReps()
        dao.deleteAllExercises()
        dao.deleteAllRoutineItems()
        dao.deleteAllRoutines()
        dao.deleteAllExerciseDefinitions()

        //TestData.testDefinitions.forEach { dao.upsertExerciseDefinition(it) }
        TestData.testRoutines.forEach { dao.upsertRoutine(it) }
        TestData.testRoutineItems.forEach { dao.upsertRoutineItem(it) }
        //TestData.testExerciseEntities.forEach { dao.upsertExercise(it) }
        //TestData.testExercisesByReps.forEach { dao.upsertExerciseByReps(it) }
        //TestData.testExercisesByDuration.forEach { dao.upsertExerciseByDuration(it) }
        TestData.testRestItems.forEach { dao.upsertRest(it) }
        //TestData.testExerciseDefinitions.forEach { dao.upsertExerciseDefinition(it) }
    }
}

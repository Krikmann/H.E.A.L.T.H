package ee.ut.cs.HEALTH

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import androidx.test.espresso.Espresso

/**
 * UI test for the Add Routine flow.
 *
 * This test verifies the complete user journey:
 * 1. Navigate to the Add Routine screen
 * 2. Fill in routine name and description
 * 3. Add multiple exercises with different configurations
 * 4. Save the routine
 * 5. Navigate to Search screen and verify the routine appears
 */
@RunWith(AndroidJUnit4::class)
class AddRoutineUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun hideKeyboard() {
        runCatching { Espresso.closeSoftKeyboard() }
        Thread.sleep(300)
    }

    private fun clickText(text: String) {
        val node = composeTestRule.onNodeWithText(text)
        runCatching { node.performScrollTo() }
        node.performClick()
    }

    private fun waitForResultItem(name: String, timeoutMs: Long = 8000L) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
            composeTestRule.onAllNodes(
                hasTestTag("exerciseResultItem") and hasText(name),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Before
    fun setUp() {
        composeTestRule.waitForIdle()
    }

    @Test
    fun addRoutineWithExercisesAndVerifyInSearch() {
        navigateToAddRoutineScreen()

        val routineName = "My Test Workout"
        val routineDescription = "A comprehensive test routine"
        fillRoutineDetails(routineName, routineDescription)

        addExerciseByReps(
            exerciseName = "Chin-ups",
            sets = 3,
            reps = 15,
            weightKg = 0.0,
            restBetweenSets = 60
        )

        addRestBetweenExercises(restSeconds = 120)

        addExerciseByDuration(
            exerciseName = "Jump Rope",
            sets = 3,
            durationSeconds = 45,
            weightKg = null,
            restBetweenSets = 30
        )

        saveRoutine()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        verifyRoutineInSearchList(routineName)

        openRoutineFromSearch(routineName)

        verifyRoutineDetails(routineName, routineDescription)
    }

    private fun navigateToAddRoutineScreen() {
        composeTestRule.onNodeWithContentDescription("Add")
            .assertExists("Add button should exist in navigation bar")
            .performClick()

        composeTestRule.onNodeWithText("Add New Routine")
            .assertExists("Add New Routine screen should be displayed")
    }

    private fun fillRoutineDetails(name: String, description: String) {
        composeTestRule.onNodeWithText("Routine name")
            .assertExists("Routine name field should exist")
            .performTextInput(name)

        composeTestRule.onNodeWithText("Routine description")
            .assertExists("Routine description field should exist")
            .performTextInput(description)

        hideKeyboard()
        composeTestRule.waitForIdle()
    }

    private fun addExerciseByReps(
        exerciseName: String,
        sets: Int,
        reps: Int,
        weightKg: Double,
        restBetweenSets: Int
    ) {
        clickText("Add Item")

        composeTestRule.onNodeWithText("Add routine item")
            .assertExists("Add routine item dialog should be displayed")

        composeTestRule.onNodeWithText("Exercise")
            .assertExists("Exercise option should exist")

        composeTestRule.onNode(hasTestTag("exerciseSearchField"))
            .performTextInput(exerciseName)
        hideKeyboard()

        composeTestRule.onNode(hasTestTag("exerciseSearchButton"))
            .performClick()

        waitForResultItem(exerciseName)

        composeTestRule.onAllNodes(
            hasTestTag("exerciseResultItem") and hasText(exerciseName),
            useUnmergedTree = true
        ).onFirst().performClick()

        composeTestRule.onNode(hasSetTextAction() and hasText("Sets"))
            .performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasText("Sets"))
            .performTextInput(sets.toString())

        if (weightKg > 0) {
            composeTestRule.onNode(hasSetTextAction() and hasText("Weight (kg)"))
                .performTextClearance()
            composeTestRule.onNode(hasSetTextAction() and hasText("Weight (kg)"))
                .performTextInput(weightKg.toString())
        }

        composeTestRule.onNode(hasSetTextAction() and hasText("Rest between sets (seconds)"))
            .performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasText("Rest between sets (seconds)"))
            .performTextInput(restBetweenSets.toString())
        hideKeyboard()

        composeTestRule.onAllNodesWithText("Reps")
            .onLast()
            .performClick()

        composeTestRule.onAllNodesWithText("Reps")
            .filter(hasSetTextAction())
            .onFirst()
            .performTextClearance()
        composeTestRule.onAllNodesWithText("Reps")
            .filter(hasSetTextAction())
            .onFirst()
            .performTextInput(reps.toString())
        hideKeyboard()

        clickText("Add item")
    }

    private fun addExerciseByDuration(
        exerciseName: String,
        sets: Int,
        durationSeconds: Int,
        weightKg: Double?,
        restBetweenSets: Int
    ) {
        clickText("Add Item")

        composeTestRule.onNodeWithText("Add routine item")
            .assertExists("Add routine item dialog should be displayed")

        composeTestRule.onNodeWithText("Exercise")
            .assertExists("Exercise option should exist")

        composeTestRule.onNode(hasTestTag("exerciseSearchField"))
            .performTextInput(exerciseName)
        hideKeyboard()

        composeTestRule.onNode(hasTestTag("exerciseSearchButton"))
            .performClick()

        waitForResultItem(exerciseName)

        composeTestRule.onAllNodes(
            hasTestTag("exerciseResultItem") and hasText(exerciseName),
            useUnmergedTree = true
        ).onFirst().performClick()

        composeTestRule.onNode(hasSetTextAction() and hasText("Sets"))
            .performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasText("Sets"))
            .performTextInput(sets.toString())

        weightKg?.let {
            composeTestRule.onNode(hasSetTextAction() and hasText("Weight (kg)"))
                .performTextClearance()
            composeTestRule.onNode(hasSetTextAction() and hasText("Weight (kg)"))
                .performTextInput(it.toString())
        }

        composeTestRule.onNode(hasSetTextAction() and hasText("Rest between sets (seconds)"))
            .performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasText("Rest between sets (seconds)"))
            .performTextInput(restBetweenSets.toString())
        hideKeyboard()

        composeTestRule.onAllNodesWithText("Duration")
            .onLast()
            .performClick()

        composeTestRule.onNode(hasSetTextAction() and hasText("Duration (seconds)"))
            .performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasText("Duration (seconds)"))
            .performTextInput(durationSeconds.toString())
        hideKeyboard()

        clickText("Add item")
    }

    private fun addRestBetweenExercises(restSeconds: Int) {
        clickText("Add Item")

        clickText("Rest time")

        composeTestRule.onNode(hasSetTextAction() and hasText("Rest duration (seconds)"))
            .performTextClearance()
        composeTestRule.onNode(hasSetTextAction() and hasText("Rest duration (seconds)"))
            .performTextInput(restSeconds.toString())
        hideKeyboard()

        clickText("Add item")
    }

    private fun saveRoutine() {
        val node = composeTestRule.onNodeWithText("Save routine")
        runCatching { node.performScrollTo() }
        node.assertExists("Save routine button should exist")
            .assertIsEnabled()
            .performClick()
    }

    private fun ensureOnSearchScreen() {
        val searchIcon = composeTestRule.onNodeWithContentDescription("Search")
        searchIcon.assertExists("Search nav icon should exist")
        runCatching { searchIcon.performClick() }
    }

    private fun verifyRoutineInSearchList(routineName: String) {
        ensureOnSearchScreen()

        val routineNode = composeTestRule.onNodeWithText(routineName)
        runCatching { routineNode.performScrollTo() }
        routineNode.assertExists("Routine '$routineName' should appear in search list")
    }

    private fun openRoutineFromSearch(routineName: String) {
        hideKeyboard()
        val node = composeTestRule.onNodeWithText(routineName)
        runCatching { node.performScrollTo() }
        node.performClick()
    }

    private fun verifyRoutineDetails(routineName: String, routineDescription: String) {
        composeTestRule.onNodeWithText(routineName)
            .assertExists("Routine name should be displayed")

        composeTestRule.onNodeWithText(routineDescription)
            .assertExists("Routine description should be displayed")
    }

    @Test
    fun addRoutineWithMinimalData() {
        navigateToAddRoutineScreen()

        val routineName = "Quick Routine"
        composeTestRule.onNodeWithText("Routine name")
            .performTextInput(routineName)

        addExerciseByReps(
            exerciseName = "Chin-ups",
            sets = 1,
            reps = 10,
            weightKg = 0.0,
            restBetweenSets = 0
        )

        saveRoutine()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        verifyRoutineInSearchList(routineName)
    }

    @Test
    fun verifyAddItemButtonExistsAndDialogOpens() {
        navigateToAddRoutineScreen()

        composeTestRule.onNodeWithText("Add Item")
            .assertExists("Add Item button should exist")
            .performClick()

        composeTestRule.onNodeWithText("Add routine item")
            .assertExists("Add routine item dialog should open")

        composeTestRule.onNodeWithText("Exercise")
            .assertExists("Exercise option should exist in dialog")

        composeTestRule.onNodeWithText("Rest time")
            .assertExists("Rest time option should exist in dialog")

        composeTestRule.onNodeWithText("Cancel")
            .performClick()
    }

    @Test
    fun verifyCannotSaveEmptyRoutine() {
        navigateToAddRoutineScreen()

        composeTestRule.onNodeWithText("Save routine")
            .assertExists("Save routine button should exist")
            .assertIsNotEnabled()
    }

    @Test
    fun verifyCanRemoveExerciseFromRoutine() {
        navigateToAddRoutineScreen()

        composeTestRule.onNodeWithText("Routine name")
            .performTextInput("Test Routine")

        addExerciseByReps(
            exerciseName = "Chin-ups",
            sets = 3,
            reps = 10,
            weightKg = 0.0,
            restBetweenSets = 60
        )

        composeTestRule.onNodeWithText("Chin-ups", substring = true)
            .assertExists("Push-ups should appear in routine")

        composeTestRule.onNodeWithText("Remove")
            .performClick()

        composeTestRule.onNodeWithText("Save routine")
            .assertIsNotEnabled()
    }
}

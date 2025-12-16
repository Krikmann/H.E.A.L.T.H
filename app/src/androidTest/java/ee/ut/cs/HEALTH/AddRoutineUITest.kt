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
 * This test suite verifies the complete user journey for creating and verifying a new routine.
 * It covers scenarios like:
 * 1. Navigating to the Add Routine screen.
 * 2. Filling in routine name and description.
 * 3. Adding multiple exercises with different configurations (reps, duration, rest).
 * 4. Saving the routine.
 * 5. Navigating to the Search screen and verifying the new routine appears.
 * 6. Handling edge cases like saving an empty routine or removing exercises.
 */
@RunWith(AndroidJUnit4::class)
class AddRoutineUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Hides the soft keyboard if it is open.
     * Includes a short delay to allow the UI to settle.
     */
    private fun hideKeyboard() {
        runCatching { Espresso.closeSoftKeyboard() }
        Thread.sleep(300)
    }

    /**
     * Finds a node by its text, scrolls to it, and performs a click.
     * @param text The text of the node to click.
     */
    private fun clickText(text: String) {
        val node = composeTestRule.onNodeWithText(text)
        runCatching { node.performScrollTo() }
        node.performClick()
    }

    /**
     * Waits until an exercise result item with the given name appears in the search list.
     * This is crucial for handling network delays when searching for exercises.
     * @param name The name of the exercise to wait for.
     * @param timeoutMs The maximum time to wait in milliseconds.
     */
    private fun waitForResultItem(name: String, timeoutMs: Long = 8000L) {
        composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
            composeTestRule.onAllNodes(
                hasTestTag("exerciseResultItem") and hasText(name),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Prepares the test environment before each test case by waiting for the UI to be idle.
     */
    @Before
    fun setUp() {
        composeTestRule.waitForIdle()
    }

    /**
     * Tests the "happy path": creating a complete routine with both reps-based and duration-based
     * exercises, saving it, and then verifying it appears correctly in the search list and
     * its details can be opened.
     */
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

    /**
     * Navigates from the main screen to the "Add New Routine" screen via the bottom navigation bar.
     */
    private fun navigateToAddRoutineScreen() {
        composeTestRule.onNodeWithContentDescription("Add")
            .assertExists("Add button should exist in navigation bar")
            .performClick()

        composeTestRule.onNodeWithText("Add New Routine")
            .assertExists("Add New Routine screen should be displayed")
    }

    /**
     * Fills in the routine's name and description fields.
     * @param name The name of the routine.
     * @param description The description of the routine.
     */
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

    /**
     * Adds a repetition-based exercise to the routine by interacting with the "Add Item" dialog.
     * This includes searching for the exercise, filling in its parameters, and confirming the addition.
     */
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

    /**
     * Adds a duration-based exercise to the routine by interacting with the "Add Item" dialog.
     * This includes searching for the exercise, filling in its parameters, and confirming the addition.
     */
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

    /**
     * Adds a rest period between exercises via the "Add Item" dialog.
     * @param restSeconds The duration of the rest in seconds.
     */
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

    /**
     * Finds and clicks the "Save routine" button.
     */
    private fun saveRoutine() {
        val node = composeTestRule.onNodeWithText("Save routine")
        runCatching { node.performScrollTo() }
        node.assertExists("Save routine button should exist")
            .assertIsEnabled()
            .performClick()
    }

    /**
     * Ensures the UI is on the Search screen by clicking the navigation icon if necessary.
     */
    private fun ensureOnSearchScreen() {
        val searchIcon = composeTestRule.onNodeWithContentDescription("Search")
        searchIcon.assertExists("Search nav icon should exist")
        runCatching { searchIcon.performClick() }
    }

    /**
     * Navigates to the search screen and verifies that a routine with the given name is present.
     * @param routineName The name of the routine to find.
     */
    private fun verifyRoutineInSearchList(routineName: String) {
        ensureOnSearchScreen()

        val routineNode = composeTestRule.onNodeWithText(routineName)
        runCatching { routineNode.performScrollTo() }
        routineNode.assertExists("Routine '$routineName' should appear in search list")
    }

    /**
     * Clicks on a routine in the search list to open its detail/preview view.
     * @param routineName The name of the routine to open.
     */
    private fun openRoutineFromSearch(routineName: String) {
        hideKeyboard()
        val node = composeTestRule.onNodeWithText(routineName)
        runCatching { node.performScrollTo() }
        node.performClick()
    }

    /**
     * In the routine preview screen, verifies that the correct name and description are displayed.
     * @param routineName The expected name of the routine.
     * @param routineDescription The expected description of the routine.
     */
    private fun verifyRoutineDetails(routineName: String, routineDescription: String) {
        composeTestRule.onNodeWithText(routineName)
            .assertExists("Routine name should be displayed")

        composeTestRule.onNodeWithText(routineDescription)
            .assertExists("Routine description should be displayed")
    }

    /**
     * Tests creating a routine with only the minimum required data (a name and one exercise)
     * and verifies it can be saved and found.
     */
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

    /**
     * Verifies that the "Add Item" button exists and correctly opens the "Add routine item" dialog
     * with both "Exercise" and "Rest time" options.
     */
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

    /**
     * Verifies that the "Save routine" button is disabled when the routine has no name or no exercises.
     */
    @Test
    fun verifyCannotSaveEmptyRoutine() {
        navigateToAddRoutineScreen()

        composeTestRule.onNodeWithText("Save routine")
            .assertExists("Save routine button should exist")
            .assertIsNotEnabled()
    }

    /**
     * Verifies that an exercise can be added and then removed, and that after removal,
     * the "Save routine" button becomes disabled again.
     */
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
            .assertExists("Chin-ups should appear in routine")

        composeTestRule.onNodeWithText("Remove")
            .performClick()

        composeTestRule.onNodeWithText("Save routine")
            .assertIsNotEnabled()
    }
}

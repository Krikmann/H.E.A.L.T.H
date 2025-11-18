package ee.ut.cs.HEALTH.domain.model.routine

import org.junit.Assert.*
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class RoutineTest {
    private fun def(id: String) = SavedExerciseDefinition(ExerciseDefinitionId(id), "ex-$id")

    private fun newRep(id: String, reps: Int) = NewExerciseByReps(
        exerciseDefinition = def(id),
        recommendedRestDurationBetweenSets = 30.seconds,
        amountOfSets = 3,
        weight = null,
        countOfRepetitions = reps
    )

    private fun newDur(id: String, seconds: Int) = NewExerciseByDuration(
        exerciseDefinition = def(id),
        recommendedRestDurationBetweenSets = 45.seconds,
        amountOfSets = 2,
        weight = null,
        duration = seconds.seconds
    )

    private fun newRest(sec: Int) = NewRestDurationBetweenExercises(sec.seconds)

    private fun savedRep(id: Long, defId: String, reps: Int) = SavedExerciseByReps(
        id = ExerciseByRepsId(id),
        exerciseDefinition = def(defId),
        recommendedRestDurationBetweenSets = 20.seconds,
        amountOfSets = 4,
        weight = null,
        countOfRepetitions = reps
    )

    private fun savedRest(id: Long, sec: Int) = SavedRestDurationBetweenExercises(
        id = RestDurationBetweenExercisesId(id),
        restDuration = sec.seconds
    )

    @Test
    fun newRoutine_insertAt_boundsAndMiddle() {
        val a = newRep("A", 8)
        val b = newDur("B", 60)
        val c = newRest(10)
        val d = newRep("D", 12)

        val base = NewRoutine(name = "r", description = null, routineItems = listOf(a, b))

        val atStart = base.insertAt(0, c)
        assertEquals(listOf(c, a, b), atStart.routineItems)

        val atMiddle = base.insertAt(1, c)
        assertEquals(listOf(a, c, b), atMiddle.routineItems)

        val atEndCoerced = base.insertAt(999, d)
        assertEquals(listOf(a, b, d), atEndCoerced.routineItems)

        val negativeCoercedToStart = base.insertAt(-5, d)
        assertEquals(listOf(d, a, b), negativeCoercedToStart.routineItems)
    }

    @Test
    fun newRoutine_removeAt_validAndInvalid() {
        val a = newRep("A", 8)
        val b = newDur("B", 60)
        val c = newRest(10)
        val base = NewRoutine("r", null, listOf(a, b, c))

        val removedMiddle = base.removeAt(1)
        assertEquals(listOf(a, c), removedMiddle.routineItems)

        val removedFirst = base.removeAt(0)
        assertEquals(listOf(b, c), removedFirst.routineItems)

        val invalidNegative = base.removeAt(-1)
        assertEquals(base, invalidNegative)

        val invalidTooLarge = base.removeAt(3)
        assertEquals(base, invalidTooLarge)
    }

    @Test
    fun newRoutine_replaceAt_validAndInvalid() {
        val a = newRep("A", 8)
        val b = newDur("B", 60)
        val c = newRest(10)
        val d = newRep("D", 12)
        val base = NewRoutine("r", null, listOf(a, b, c))

        val replaced = base.replaceAt(1, d)
        assertEquals(listOf(a, d, c), replaced.routineItems)

        val invalidNegative = base.replaceAt(-1, d)
        assertEquals(base, invalidNegative)

        val invalidTooLarge = base.replaceAt(10, d)
        assertEquals(base, invalidTooLarge)
    }

    @Test
    fun newRoutine_move_clampsAndNoops() {
        val a = newRep("A", 8)
        val b = newDur("B", 60)
        val c = newRep("C", 10)
        val d = newRest(15)
        val base = NewRoutine("r", null, listOf(a, b, c, d))

        val moved1 = base.move(3, 0)
        assertEquals(listOf(d, a, b, c), moved1.routineItems)

        val moved2 = base.move(0, 999)
        assertEquals(listOf(b, c, d, a), moved2.routineItems)

        val moved3 = base.move(2, -5)
        assertEquals(listOf(c, a, b, d), moved3.routineItems)

        val noop1 = base.move(-1, 2)
        assertEquals(base, noop1)
        val noop2 = base.move(10, 1)
        assertEquals(base, noop2)

        val noop3 = base.move(1, 1)
        assertEquals(base, noop3)
    }

    @Test
    fun updatedRoutine_insertAt_marksFollowingAsUpdated_andInsertsNew() {
        val s1 = savedRep(1, "A", 8)
        val s2 = savedRep(2, "B", 10)
        val r = savedRest(3, 20)
        val existing: List<RoutineItem> = listOf(s1, s2, r)
        val routine = UpdatedRoutine(id = RoutineId(1), name = "u", description = null, routineItems = existing)

        val newItem = newRest(30)
        val result = routine.insertAt(1, newItem)

        assertEquals(4, result.routineItems.size)
        assertTrue(result.routineItems[0] is SavedExerciseByReps)
        assertEquals(newItem, result.routineItems[1])
        assertTrue(result.routineItems[2] is UpdatedExerciseByReps)
        assertTrue(result.routineItems[3] is UpdatedRestDurationBetweenExercises)
    }

    @Test
    fun updatedRoutine_removeAt_marksFollowingAsUpdated() {
        val s1 = savedRep(1, "A", 8)
        val s2 = savedRep(2, "B", 10)
        val r = savedRest(3, 20)
        val routine = UpdatedRoutine(RoutineId(1), "u", null, listOf(s1, s2, r))

        val result = routine.removeAt(1)
        assertEquals(2, result.routineItems.size)
        assertTrue(result.routineItems[0] is SavedExerciseByReps)
        assertTrue(result.routineItems[1] is UpdatedRestDurationBetweenExercises)

        assertSame(routine, routine.removeAt(-1))
        assertSame(routine, routine.removeAt(3))
    }

    @Test
    fun updatedRoutine_move_marksMovedAndIntervening() {
        val s1 = savedRep(1, "A", 8)
        val s2 = savedRep(2, "B", 10)
        val s3 = savedRep(3, "C", 12)
        val s4 = savedRep(4, "D", 14)
        val base = UpdatedRoutine(RoutineId(1), "u", null, listOf(s1, s2, s3, s4))

        val m1 = base.move(0, 2)
        assertTrue(m1.routineItems[0] is UpdatedExerciseByReps)
        assertTrue(m1.routineItems[1] is UpdatedExerciseByReps)
        assertTrue(m1.routineItems[2] is UpdatedExerciseByReps)
        assertTrue(m1.routineItems[3] is SavedExerciseByReps)

        val m2 = base.move(3, 0)
        assertTrue(m2.routineItems[0] is UpdatedExerciseByReps)
        assertTrue(m2.routineItems[1] is UpdatedExerciseByReps)
        assertTrue(m2.routineItems[2] is UpdatedExerciseByReps)
        assertTrue(m2.routineItems[3] is UpdatedExerciseByReps)

        assertSame(base, base.move(-1, 2))
        assertSame(base, base.move(10, 1))
        assertSame(base, base.move(1, 1))
    }

    @Test
    fun updatedRoutine_replaceAt_withNew_doesNotRetypeOthers() {
        val s1 = savedRep(1, "A", 8)
        val s2 = savedRep(2, "B", 10)
        val r = savedRest(3, 20)
        val base = UpdatedRoutine(RoutineId(1), "u", null, listOf(s1, s2, r))

        val newItem = newRep("X", 5)
        val result = base.replaceAt(1, newItem)
        assertEquals(3, result.routineItems.size)
        assertTrue(result.routineItems[0] is SavedExerciseByReps)
        assertEquals(newItem, result.routineItems[1])
        assertTrue(result.routineItems[2] is SavedRestDurationBetweenExercises)

        assertEquals(base, base.replaceAt(-1, newItem))
        assertEquals(base, base.replaceAt(10, newItem))
    }

    @Test
    fun updatedRoutine_replaceAt_withUpdated_doesNotRetypeOthers() {
        val s1 = savedRep(1, "A", 8)
        val s2 = savedRep(2, "B", 10)
        val r = savedRest(3, 20)
        val base = UpdatedRoutine(RoutineId(1), "u", null, listOf(s1, s2, r))

        val updated = s2.toUpdated()
        val result = base.replaceAt(1, updated)
        assertEquals(3, result.routineItems.size)
        assertTrue(result.routineItems[0] is SavedExerciseByReps)
        assertTrue(result.routineItems[1] is UpdatedExerciseByReps)
        assertTrue(result.routineItems[2] is SavedRestDurationBetweenExercises)

        assertEquals(base, base.replaceAt(-1, updated))
        assertEquals(base, base.replaceAt(10, updated))
    }
}

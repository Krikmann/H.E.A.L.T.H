package ee.ut.cs.HEALTH.domain.model.routine

@JvmInline value class RoutineId(val value: Long)

sealed interface Routine {
    val name: String
    val description: String?
    val routineItems: List<RoutineItem>
    val counter: Int
}

data class SavedRoutine(
    val id: RoutineId,
    override val name: String,
    override val description: String?,
    override val routineItems: List<SavedRoutineItem>,
    override val counter: Int = 0
): Routine

data class UpdatedRoutine(
    val id: RoutineId,
    override val name: String,
    override val description: String?,
    override val routineItems: List<RoutineItem>,
    override val counter: Int = 0
): Routine

data class NewRoutine(
    override val name: String,
    override val description: String?,
    override val routineItems: List<NewRoutineItem>,
    override val counter: Int = 0
): Routine

private fun <T: RoutineItem> List<T>.insertAt(index: Int, item: T): List<T> {
    require(index in 0..size) { "index $index is out of bounds for insert" }
    return buildList(size + 1) {
        addAll(subList(0, index))
        add(item)
        addAll(subList(index, size))
    }
}

private fun <T: RoutineItem> List<T>.removeAt(index: Int): List<T> {
    require(index in indices) { "index $index out of bounds for removeAt" }
    return buildList(size - 1) {
        addAll(subList(0, index))
        addAll(subList(index + 1, size))
    }
}

private fun <T: RoutineItem> List<T>.replaceAt(index: Int, item: T): List<T> {
    require(index in indices) { "index $index out of bounds for replaceAt" }
    return buildList(size) {
        addAll(subList(0, index))
        add(item)
        addAll(subList(index + 1, size))
    }
}

private fun <T: RoutineItem> List<T>.move(fromIndex: Int, toIndex: Int): List<T> {
    require(fromIndex in indices) { "fromIndex $fromIndex out of bounds" }
    require(toIndex in indices) { "toIndex $toIndex out of bounds" }
    if (fromIndex == toIndex) return this

    val item = this[fromIndex]
    val without = removeAt(fromIndex)
    return without.insertAt(toIndex, item)
}

fun NewRoutine.add(item: NewRoutineItem): NewRoutine =
    copy(routineItems = routineItems + item)

fun NewRoutine.insertAt(index: Int, item: NewRoutineItem): NewRoutine {
    return copy(routineItems = routineItems.insertAt(index, item))
}

fun NewRoutine.removeAt(index: Int): NewRoutine {
    return copy(routineItems = routineItems.removeAt(index))
}

fun NewRoutine.replaceAt(index: Int, item: NewRoutineItem): NewRoutine {
    return copy(routineItems = routineItems.replaceAt(index, item))
}

fun NewRoutine.move(fromIndex: Int, toIndex: Int): NewRoutine {
    return copy(routineItems = routineItems.move(fromIndex, toIndex))
}
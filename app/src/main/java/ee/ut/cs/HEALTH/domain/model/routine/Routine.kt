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
    val index = index.coerceIn(0, size)
    return buildList(size + 1) {
        addAll(this@insertAt.subList(0, index))
        add(item)
        addAll(this@insertAt.subList(index, this@insertAt.size))
    }
}
private fun Int.isValidIndex(size: Int) = this >= 0 && this < size

private fun <T: RoutineItem> List<T>.removeAt(index: Int): List<T> {
    if (!index.isValidIndex(size)) return this
    return buildList(size - 1) {
        addAll(this@removeAt.subList(0, index))
        addAll(this@removeAt.subList(index + 1, this@removeAt.size))
    }
}

private fun <T: RoutineItem> List<T>.replaceAt(index: Int, item: T): List<T> {
    if (!index.isValidIndex(size)) return this
    return buildList(size) {
        addAll(this@replaceAt.subList(0, index))
        add(item)
        addAll(this@replaceAt.subList(index + 1, this@replaceAt.size))
    }
}

private fun <T: RoutineItem> List<T>.move(fromIndex: Int, toIndex: Int): List<T> {
    if (isEmpty()) return this
    if (!fromIndex.isValidIndex(size)) return this
    val target = toIndex.coerceIn(0, size - 1)
    if (fromIndex == toIndex) return this

    val item = this[fromIndex]
    val without = removeAt(fromIndex)
    return without.insertAt(target, item)
}

fun NewRoutine.withName(name: String) = copy(name = name)
fun NewRoutine.withDescription(description: String?) = copy(description = description)

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
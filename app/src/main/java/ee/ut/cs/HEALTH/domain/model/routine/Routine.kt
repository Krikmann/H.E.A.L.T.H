package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.jvm.JvmInline

/**
 * A type-safe value class representing the unique identifier for a routine.
 *
 * Using a value class prevents accidental misuse of primitive types (e.g., passing a user ID where a routine ID is expected).
 *
 * @property value The underlying primitive value of the ID.
 */
@JvmInline
value class RoutineId(val value: Long)

/**
 * A sealed interface representing the concept of a workout routine.
 *
 * It defines the common properties shared across all states of a routine (new, saved, being updated).
 *
 * @property name The user-defined name of the routine.
 * @property description An optional, more detailed description of the routine.
 * @property routineItems A list of [RoutineItem]s that make up the sequence of the workout.
 * @property counter A counter, typically used to track how many times the routine has been completed.
 */
sealed interface Routine {
    val name: String
    val description: String?
    val routineItems: List<RoutineItem>
    val counter: Int
}

/**
 * Represents a routine that has been saved to the persistent storage (database).
 *
 * This data class is considered the canonical state of a routine. It holds a persistent [id]
 * and its items are all of type [SavedRoutineItem].
 *
 * @param id The unique, persistent identifier of the routine.
 */
data class SavedRoutine(
    val id: RoutineId,
    override val name: String,
    override val description: String?,
    override val routineItems: List<SavedRoutineItem>,
    override val counter: Int = 0
): Routine

/**
 * Represents a routine that is currently being edited.
 *
 * This state is created from a [SavedRoutine] and can contain a mix of [SavedRoutineItem],
 * [UpdatedRoutineItem], and [NewRoutineItem] types as the user makes changes.
 *
 * @param id The unique, persistent identifier of the routine being updated.
 */
data class UpdatedRoutine(
    val id: RoutineId,
    override val name: String,
    override val description: String?,
    override val routineItems: List<RoutineItem>,
    override val counter: Int = 0
): Routine

/**
 * Represents a brand new routine that has not yet been saved to the database.
 *
 * It has no persistent [id] and all of its items are of type [NewRoutineItem]. This is the
 * initial state when a user starts creating a new workout.
 */
data class NewRoutine(
    override val name: String,
    override val description: String?,
    override val routineItems: List<NewRoutineItem>,
    override val counter: Int = 0
): Routine

private fun <T: RoutineItem> List<T>.insertAt(index: Int, item: T): List<T> {
    val coercedIndex = index.coerceIn(0, size)
    return buildList(size + 1) {
        addAll(this@insertAt.subList(0, coercedIndex))
        add(item)
        addAll(this@insertAt.subList(coercedIndex, this@insertAt.size))
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
    if (isEmpty() || !fromIndex.isValidIndex(size) || fromIndex == toIndex) return this
    val target = toIndex.coerceIn(0, size - 1)

    val item = this[fromIndex]
    val without = removeAt(fromIndex)
    return without.insertAt(target, item)
}

/** Returns a new [NewRoutine] instance with the updated name. */
fun NewRoutine.withName(name: String) = copy(name = name)
/** Returns a new [NewRoutine] instance with the updated description. */
fun NewRoutine.withDescription(description: String?) = copy(description = description)

/** Returns a new [NewRoutine] with the given [item] appended to the end of its item list. */
fun NewRoutine.add(item: NewRoutineItem): NewRoutine =
    copy(routineItems = routineItems + item)

/** Returns a new [NewRoutine] with the given [item] inserted at the specified [index]. */
fun NewRoutine.insertAt(index: Int, item: NewRoutineItem): NewRoutine =
    copy(routineItems = routineItems.insertAt(index, item))

/** Returns a new [NewRoutine] with the item at the specified [index] removed. */
fun NewRoutine.removeAt(index: Int): NewRoutine =
    copy(routineItems = routineItems.removeAt(index))

/** Returns a new [NewRoutine] with the item at the specified [index] replaced by the new [item]. */
fun NewRoutine.replaceAt(index: Int, item: NewRoutineItem): NewRoutine =
    copy(routineItems = routineItems.replaceAt(index, item))

/** Returns a new [NewRoutine] with an item moved from [fromIndex] to [toIndex]. */
fun NewRoutine.move(fromIndex: Int, toIndex: Int): NewRoutine =
    copy(routineItems = routineItems.move(fromIndex, toIndex))

private fun RoutineItem.markMovedAsUpdated(): RoutineItem = when (this) {
    is SavedRoutineItem -> toUpdated()
    is UpdatedRoutineItem -> this
    is NewRoutineItem -> this
}

/** Returns a new [UpdatedRoutine] instance with the updated name. */
fun UpdatedRoutine.withName(name: String) = copy(name = name)
/** Returns a new [UpdatedRoutine] instance with the updated description. */
fun UpdatedRoutine.withDescription(description: String?) = copy(description = description)

/** Returns a new [UpdatedRoutine] with the given [item] appended to the end of its item list. */
fun UpdatedRoutine.add(item: NewRoutineItem): UpdatedRoutine =
    copy(routineItems = routineItems + item)

/** Returns a new [UpdatedRoutine] with the given [item] inserted at the specified [index], marking subsequent items as updated. */
fun UpdatedRoutine.insertAt(index: Int, item: NewRoutineItem): UpdatedRoutine {
    val coercedIndex = index.coerceIn(0, routineItems.size)
    val newRoutineItems = buildList(routineItems.size + 1) {
        addAll(routineItems.subList(0, coercedIndex))
        add(item)
        addAll(routineItems
            .subList(coercedIndex, routineItems.size)
            .map { it.markMovedAsUpdated() })
    }
    return copy(routineItems = newRoutineItems)
}

/** Returns a new [UpdatedRoutine] with the item at the specified [index] removed, marking subsequent items as updated. */
fun UpdatedRoutine.removeAt(index: Int): UpdatedRoutine {
    if (!index.isValidIndex(routineItems.size)) return this
    val newRoutineItems = buildList(routineItems.size - 1) {
        addAll(routineItems.subList(0, index))
        addAll(routineItems
            .subList(index + 1, routineItems.size)
            .map { it.markMovedAsUpdated() })
    }

    return copy(routineItems = newRoutineItems)
}

/** Returns a new [UpdatedRoutine] with the item at the specified [index] replaced by the new [item]. */
fun UpdatedRoutine.replaceAt(index: Int, item: NewRoutineItem): UpdatedRoutine =
    copy(routineItems = routineItems.replaceAt(index, item))

/** Returns a new [UpdatedRoutine] with the item at the specified [index] replaced by the updated [item]. */
fun UpdatedRoutine.replaceAt(index: Int, item: UpdatedRoutineItem): UpdatedRoutine =
    copy(routineItems = routineItems.replaceAt(index, item))

/** Returns a new [UpdatedRoutine] with an item moved from [fromIndex] to [toIndex], marking affected items as updated. */
fun UpdatedRoutine.move(fromIndex: Int, toIndex: Int): UpdatedRoutine {
    if (routineItems.isEmpty() || !fromIndex.isValidIndex(routineItems.size) || fromIndex == toIndex) return this

    val coercedToIndex = toIndex.coerceIn(0, routineItems.size - 1)

    val moved = routineItems[fromIndex].markMovedAsUpdated()

    val newRoutineItems = buildList(routineItems.size) {
        if (fromIndex < coercedToIndex) {
            addAll(routineItems.subList(0, fromIndex))
            addAll(routineItems.subList(fromIndex + 1, coercedToIndex + 1)
                .map { it.markMovedAsUpdated() })
            add(moved)
            addAll(routineItems.subList(coercedToIndex + 1, routineItems.size))
        } else { // fromIndex > toIndex
            addAll(routineItems.subList(0, coercedToIndex))
            add(moved)
            addAll(routineItems.subList(coercedToIndex, fromIndex)
                .map { it.markMovedAsUpdated() })
            addAll(routineItems.subList(fromIndex + 1, routineItems.size))
        }
    }

    return copy(routineItems = newRoutineItems)
}

/** Converts a [SavedRoutine] into an [UpdatedRoutine] to begin an editing session. */
private fun SavedRoutine.toUpdated(): UpdatedRoutine =
    UpdatedRoutine(
        id = id,
        name = name,
        description = description,
        routineItems = routineItems
    )

/** Creates an [UpdatedRoutine] from a [SavedRoutine] with a new name. */
fun SavedRoutine.withName(name: String): UpdatedRoutine =
    toUpdated().withName(name)

/** Creates an [UpdatedRoutine] from a [SavedRoutine] with a new description. */
fun SavedRoutine.withDescription(description: String?): UpdatedRoutine =
    toUpdated().withDescription(description)

/** Creates an [UpdatedRoutine] from a [SavedRoutine] and adds a new item to it. */
fun SavedRoutine.add(item: NewRoutineItem): UpdatedRoutine =
    toUpdated().add(item)

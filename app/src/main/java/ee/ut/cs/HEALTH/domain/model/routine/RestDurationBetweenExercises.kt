package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.time.Duration

/**
 * A type-safe value class for the unique identifier of a rest duration item.
 *
 * @property value The underlying ID value.
 */
@JvmInline value class RestDurationBetweenExercisesId(val value: Long)

/**
 * Represents a rest period between exercises that has been saved to persistent storage.
 *
 * It implements [SavedRoutineItem] and contains a persistent [id].
 *
 * @property id The unique identifier for this saved rest period.
 * @property restDuration The duration of the rest.
 */
data class SavedRestDurationBetweenExercises(
    val id: RestDurationBetweenExercisesId,
    val restDuration: Duration,
): SavedRoutineItem {
    /**
     * Converts this saved item into an [UpdatedRestDurationBetweenExercises] instance,
     * typically used when starting an editing session for a routine.
     */
    override fun toUpdated(): UpdatedRestDurationBetweenExercises =
        UpdatedRestDurationBetweenExercises(
            id = id,
            restDuration = restDuration
        )
}

/**
 * Represents a rest period that is part of a routine currently being edited.
 *
 * This can be an item that was originally a [SavedRestDurationBetweenExercises] or one that
 * has been modified. It implements [UpdatedRoutineItem].
 *
 * @property id The persistent identifier of the original saved item.
 * @property restDuration The current duration of the rest, which may have been modified.
 */
data class UpdatedRestDurationBetweenExercises(
    val id: RestDurationBetweenExercisesId,
    val restDuration: Duration,
): UpdatedRoutineItem

/**
 * Represents a new rest period that has been created in the UI but not yet saved.
 *
 * It has no persistent ID and implements [NewRoutineItem].
 *
 * @property restDuration The duration of the new rest period.
 */
data class NewRestDurationBetweenExercises(
    val restDuration: Duration,
): NewRoutineItem

/**
 * Returns a new [NewRestDurationBetweenExercises] instance with an updated duration.
 *
 * @param d The new [Duration] for the rest period.
 */
fun NewRestDurationBetweenExercises.withRestDuration(d: Duration) =
    copy(restDuration = d)


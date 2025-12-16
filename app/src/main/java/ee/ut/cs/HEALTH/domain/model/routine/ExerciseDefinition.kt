package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.jvm.JvmInline

/**
 * A type-safe value class representing the unique identifier for an exercise definition.
 *
 * This is typically a string-based ID from a remote API or a local database.
 *
 * @property value The underlying string value of the ID.
 */
@JvmInline
value class ExerciseDefinitionId(val value: String)

/**
 * A sealed interface representing the fundamental definition of an exercise.
 *
 * It establishes a common contract for all exercise definition states, ensuring they
 * all have a `name`.
 */
sealed interface ExerciseDefinition {
    val name: String
}

/**
 * Represents an exercise definition that has been saved to persistent storage.
 *
 * This is the canonical representation of an exercise, holding its persistent ID.
 *
 * @property id The unique, persistent identifier.
 * @property name The official name of the exercise.
 */
data class SavedExerciseDefinition(
    val id: ExerciseDefinitionId,
    val name: String,
)

/**
 * Represents an exercise definition that is being edited.
 *
 * This state would be used if the user were allowed to modify the properties
 * of a saved exercise definition.
 *
 * @property id The persistent identifier of the exercise being updated.
 * @property name The current (potentially modified) name of the exercise.
 */
data class UpdatedExerciseDefinition(
    val id: ExerciseDefinitionId,
    val name: String,
)

/**
 * Represents a brand-new exercise definition that has not yet been saved.
 *
 * This would be used if the application allowed users to create completely new
 * exercise definitions from scratch.
 *
 * @property name The name of the new exercise.
 */
data class NewExerciseDefinition(
    val name: String,
)
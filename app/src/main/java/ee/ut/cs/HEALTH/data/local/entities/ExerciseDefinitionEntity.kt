package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the static definition of an exercise in the local database.
 *
 * This entity stores the core, unchanging information about an exercise, such as
 * its unique identifier and its name. It serves as a central reference point
 * for routines, where each exercise instance ([ExerciseEntity]) links back to one
 * of these definitions.
 *
 * @property id The unique identifier for the exercise definition. This is the primary key and
 *              often corresponds to an ID from a remote API.
 * @property name The human-readable name of the exercise (e.g., "Push-up").
 */
@Entity(tableName = "exercise_definitions")
data class ExerciseDefinitionEntity(
    @PrimaryKey
    val id: ExerciseDefinitionId,
    val name: String
)

/**
 * A type-safe value class for the unique identifier of an exercise definition.
 *
 * Using a value class helps prevent accidental misuse of primitive types like String,
 * ensuring that only valid exercise definition IDs are used where required.
 *
 * @property value The underlying ID value, which is a String.
 */
@JvmInline value class ExerciseDefinitionId(val value: String)

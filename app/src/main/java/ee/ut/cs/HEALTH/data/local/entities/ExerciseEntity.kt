package ee.ut.cs.HEALTH.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents the common properties of an exercise within a routine in the database.
 *
 * This entity has a one-to-one relationship with [RoutineItemEntity] where its primary key [id]
 * is also a foreign key to [RoutineItemEntity.id]. It also holds a foreign key to an
 * [ExerciseDefinitionEntity] to link to the static details of an exercise (like its name).
 * This table stores properties applicable to all exercise types (reps or duration-based).
 *
 * @property id The unique primary key, which is also a foreign key to the corresponding [RoutineItemEntity].
 *              If the parent routine item is deleted, this entry is also deleted (CASCADE).
 * @property exerciseDefinitionId A foreign key to the [ExerciseDefinitionEntity]. [ForeignKey.RESTRICT]
 *                                prevents the deletion of a definition if it is still used by an exercise.
 * @property type The type of the exercise, determining if it is measured in reps or duration.
 * @property recommendedRestDurationBetweenSetsInSeconds The rest time in seconds to be taken between sets.
 * @property amountOfSets The total number of sets to be performed for this exercise.
 * @property weightInKg The weight in kilograms to be used for the exercise, if applicable.
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseDefinitionId"],
            onDelete = ForeignKey.RESTRICT,
        )
    ],
    indices = [
        Index("exerciseDefinitionId"),
        Index("type"),
    ]
)
data class ExerciseEntity(
    @PrimaryKey
    val id: RoutineItemId,
    val exerciseDefinitionId: ExerciseDefinitionId,
    val type: ExerciseType,
    val recommendedRestDurationBetweenSetsInSeconds: Long,
    val amountOfSets: Int,
    val weightInKg: Double?,
)

/**
 * Defines the possible types for an [ExerciseEntity].
 * This determines which specific data table ([ExerciseByDurationEntity] or [ExerciseByRepsEntity])
 * is linked to this exercise.
 */
enum class ExerciseType {
    /** Indicates that the exercise is measured by a set duration. */
    DURATION,
    /** Indicates that the exercise is measured by a number of repetitions. */
    REPS,
}

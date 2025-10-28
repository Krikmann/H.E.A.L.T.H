package ee.ut.cs.HEALTH.domain.model.routine

@JvmInline value class ExerciseDefinitionId(val value: String)

sealed interface ExerciseDefinition {
    val name: String
}

data class SavedExerciseDefinition(
    val id: ExerciseDefinitionId,
    val name: String,
)

data class UpdatedExerciseDefinition(
    val id: ExerciseDefinitionId,
    val name: String,
)

data class NewExerciseDefinition(
    val name: String,
)
package ee.ut.cs.HEALTH.domain.model.routine

data class ExerciseDefinition(
    val id: ExerciseDefinitionId,
    val name: String,
)

@JvmInline value class ExerciseDefinitionId(val id: Int)

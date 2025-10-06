package ee.ut.cs.HEALTH.domain.model.routine

data class Routine(
    val id: RoutineId,
    val name: String,
    val description: String?,
    val routineItems: List<RoutineItem>,
    var counter: Int = 0
)

@JvmInline value class RoutineId(val id: Int)

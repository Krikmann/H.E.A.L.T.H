package ee.ut.cs.HEALTH.domain.model.routine

@JvmInline
value class Weight private constructor(val kilograms: Double) {
    companion object {
        private const val LBS_IN_ONE_KG = 2.20462

        fun fromKg(kg: Double) = Weight(kg)
        fun fromLbs(lbs: Double) = Weight(lbs / LBS_IN_ONE_KG)
    }

    val inKg get() = kilograms
    val inLbs get() = kilograms * LBS_IN_ONE_KG
}

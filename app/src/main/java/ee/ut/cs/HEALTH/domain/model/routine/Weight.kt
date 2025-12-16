package ee.ut.cs.HEALTH.domain.model.routine

import kotlin.jvm.JvmInline

/**
 * A type-safe value class representing a weight measurement.
 *
 * This class ensures that weight values are handled consistently throughout the application,
 * avoiding confusion between different units like kilograms and pounds. The private constructor
 * forces the use of the provided factory methods (`fromKg`, `fromLbs`) for instantiation.
 *
 * @property kilograms The weight value stored internally in kilograms.
 */
@JvmInline
value class Weight private constructor(val kilograms: Double) {
    companion object {
        private const val LBS_IN_ONE_KG = 2.20462

        /**
         * Creates a [Weight] instance from a value in kilograms.
         * @param kg The weight in kilograms.
         * @return A new [Weight] instance.
         */
        fun fromKg(kg: Double) = Weight(kg)

        /**
         * Creates a [Weight] instance from a value in pounds (lbs).
         * @param lbs The weight in pounds.
         * @return A new [Weight] instance.
         */
        fun fromLbs(lbs: Double) = Weight(lbs / LBS_IN_ONE_KG)
    }

    /**
     * Returns the weight value in kilograms.
     */
    val inKg get() = kilograms

    /**
     * Returns the weight value converted to pounds (lbs).
     */
    val inLbs get() = kilograms * LBS_IN_ONE_KG
}

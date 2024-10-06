package org.stacklabs.weather.dto

import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Big tendency")
enum class BigTendency {
    BIG_INCREASING,
    INCREASING,
    CONSTANT,
    DECREASING,
    BIG_DECREASING;

    companion object {
        /**
         * Calculates the tendency based on the provided values.
         * @param before The value before the change.
         * @param after The value after the change.
         * @param bigDelta The threshold for a significant change.
         * @return The calculated `BigTendency`.
         */
        fun get(before: Double, after: Double, bigDelta: Double): BigTendency = when {
            Math.abs(before - after) > bigDelta && before < after -> BIG_INCREASING
            Math.abs(before - after) > bigDelta && before > after -> BIG_DECREASING
            before < after -> INCREASING
            before > after -> DECREASING
            else -> CONSTANT
        }
    }
}
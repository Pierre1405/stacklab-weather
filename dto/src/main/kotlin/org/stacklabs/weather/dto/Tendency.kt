package org.stacklabs.weather.dto

import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Tendency")
enum class Tendency {
    INCREASING,
    CONSTANT,
    DECREASING;

    companion object {
        fun <T> get(before: Comparable<T>, after: T): Tendency = when {
            before < after -> INCREASING
            before == after -> CONSTANT
            else -> DECREASING
        }
    }
}
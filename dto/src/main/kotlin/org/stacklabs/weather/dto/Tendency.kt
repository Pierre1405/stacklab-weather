package org.stacklabs.weather.dto

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
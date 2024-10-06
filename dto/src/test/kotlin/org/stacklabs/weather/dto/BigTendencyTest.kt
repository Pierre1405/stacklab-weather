package org.stacklabs.weather.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BigTendencyTest {

    @Test
    fun test_get_bigIncreasing() {
        val before = 10.0
        val after = 20.0
        val bigDelta = 5.0
        val result = BigTendency.get(before, after, bigDelta)
        assertEquals(BigTendency.BIG_INCREASING, result)
    }

    @Test
    fun test_get_increasing() {
        val before = 10.0
        val after = 13.0
        val bigDelta = 5.0
        val result = BigTendency.get(before, after, bigDelta)
        assertEquals(BigTendency.INCREASING, result)
    }

    @Test
    fun test_get_constant() {
        val before = 10.0
        val after = 10.0
        val bigDelta = 5.0
        val result = BigTendency.get(before, after, bigDelta)
        assertEquals(BigTendency.CONSTANT, result)
    }

    @Test
    fun test_get_decreasing() {
        val before = 10.0
        val after = 8.0
        val bigDelta = 10.0
        val result = BigTendency.get(before, after, bigDelta)
        assertEquals(BigTendency.DECREASING, result)
    }

    @Test
    fun test_get_bigDecreasing() {
        val before = 10.0
        val after = 5.0
        val bigDelta = -5.0
        val result = BigTendency.get(before, after, bigDelta)
        assertEquals(BigTendency.BIG_DECREASING, result)
    }
}
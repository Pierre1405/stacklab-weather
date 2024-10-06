package org.stacklabs.weather.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TendencyTest {

    @Test
    fun test_get_increasing() {
        val before = 10.0
        val after = 13.0
        val result = Tendency.get(before, after)
        assertEquals(Tendency.INCREASING, result)
    }

    @Test
    fun test_get_constant() {
        val before = 10.0
        val after = 10.0
        val result = Tendency.get(before, after)
        assertEquals(Tendency.CONSTANT, result)
    }

    @Test
    fun test_get_decreasing() {
        val before = 10.0
        val after = 8.0
        val result = Tendency.get(before, after)
        assertEquals(Tendency.DECREASING, result)
    }

}
package com.stacklabs.weather.service.evaluation

import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OptimalValueEvaluationTest {

    @Test
    fun test_evaluateRatio_OptimalValueBelowWorstValue() {
        val properties =
            ValueEvaluationProperties(weight = 2.0, optimalValue = 10.0, worstValue = 20.0)
        val evaluation = OptimalValueEvaluation(properties)

        assertEquals(
            2.0,
            evaluation.evaluate(10.0),
            "Result should be weighted when value equals optimalValue"
        )
        assertEquals(
            0.0,
            evaluation.evaluate(20.0),
            "The ratio should be zero when value equals optimalValue + distance (Math.abs(optimalValue - worstValue))"
        )
        assertEquals(
            0.0,
            evaluation.evaluate(0.0),
            "The ratio should be zero when value equals optimalValue - distance (Math.abs(optimalValue - worstValue))"
        )
        assertTrue(
            0.0 > evaluation.evaluate(-10.0),
            "The ratio should be negative when value < optimalValue - distance (Math.abs(optimalValue - worstValue))"
        )
        assertTrue(
            0.0 > evaluation.evaluate(40.0),
            "The ratio should be negative when value > optimalValue + distance (Math.abs(optimalValue - worstValue))"
        )
    }

    @Test
    fun test_evaluateRatio_OptimalValueAboveWorstValue() {
        val properties =
            ValueEvaluationProperties(weight = 2.0, optimalValue = 10.0, worstValue = 00.0)
        val evaluation = OptimalValueEvaluation(properties)

        assertEquals(
            2.0,
            evaluation.evaluate(10.0),
            "Result should be weighted when value equals optimalValue"
        )
        assertEquals(
            0.0,
            evaluation.evaluate(20.0),
            "The ratio should be zero when value equals optimalValue + distance (Math.abs(optimalValue - worstValue))"
        )
        assertEquals(
            0.0,
            evaluation.evaluate(0.0),
            "The ratio should be zero when value equals optimalValue - distance (Math.abs(optimalValue - worstValue))"
        )
        assertTrue(
            0.0 > evaluation.evaluate(-10.0),
            "The ratio should be negative when value < optimalValue - distance (Math.abs(optimalValue - worstValue))"
        )
        assertTrue(
            0.0 > evaluation.evaluate(40.0),
            "The ratio should be negative when value > optimalValue + distance (Math.abs(optimalValue - worstValue))"
        )
    }

    @Test
    fun test_evaluateRatio_OptimalValueEqualsWorstValue() {
        val properties =
            ValueEvaluationProperties(weight = 1.0, optimalValue = 10.0, worstValue = 10.0)
        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            OptimalValueEvaluation(properties)
        }
        assertEquals("Worst value cannot be equal to optimal value", exception.message)
    }
}
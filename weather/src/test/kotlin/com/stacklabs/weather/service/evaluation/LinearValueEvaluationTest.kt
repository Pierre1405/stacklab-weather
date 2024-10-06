package com.stacklabs.weather.service.evaluation

import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LinearValueEvaluationTest {

    @Test
    fun test_evaluate_OptimalValueBelowWorstValue() {
        val properties =
            ValueEvaluationProperties(weight = 2.0, optimalValue = 10.0, worstValue = 20.0)
        val evaluation = LinearValueEvaluation(properties)
        assertTrue(2.0 < evaluation.evaluate(-10.0), "result should more than weight when value is below optimalValue")
        assertEquals(2.0, evaluation.evaluate(10.0), "result should be weighted when value equals optimalValue")
        assertEquals(0.0, evaluation.evaluate(20.0), "result should be zero when value equals worstValue")
        assertTrue(0.0 > evaluation.evaluate(40.0), "result should negative when value is greater than worstValue")
    }

    @Test
    fun test_evaluate_OptimalValueAboveWorstValue() {
        val properties =
            ValueEvaluationProperties(weight = 2.0, optimalValue = 20.0, worstValue = 10.0)
        val evaluation = LinearValueEvaluation(properties)
        assertTrue(
            properties.weight < evaluation.evaluate(40.0),
            "result should more than weight when value is greater optimalValue"
        )
        assertEquals(
            properties.weight,
            evaluation.evaluate(20.0),
            "result should be weight when value equals optimalValue"
        )
        assertEquals(
            0.0,
            evaluation.evaluate(10.0),
            0.01,
            "result should zero be weighted when value equals worstValue"
        )
        assertTrue(0.0 > evaluation.evaluate(-10.0), "result should negative when value is below than worstValue")
    }

    @Test
    fun test_evaluate_OptimalValueEqualsWorstValue() {
        val properties = ValueEvaluationProperties(weight = 1.0, optimalValue = 10.0, worstValue = 10.0)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            LinearValueEvaluation(properties)
        }
        assertEquals("Worst value cannot be equal to optimal value", exception.message)
    }
}
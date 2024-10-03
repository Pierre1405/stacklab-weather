package com.stacklabs.weather.service

import com.stacklabs.weather.configuration.EvaluationProperties
import com.stacklabs.weather.service.WeatherEvaluation.EvaluationFunctions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WeatherEvaluationTest {

    @Test
    fun test_getDistanceFromOptimalValue_valueBelowOptimal() {
        val properties = EvaluationProperties(
            optimalValue = 15.0,
            worstMinValue = 0.0,
            worstMaxValue = 20.0,
            weight = 2.0,
            evaluationFunction = EvaluationFunctions.LINEAR
        )
        val result = WeatherEvaluation.evaluate(7.5, properties)
        assertEquals(1.0, result)
    }

    @Test
    fun test_getDistanceFromOptimalValue_valueAboveOptimal() {
        val properties = EvaluationProperties(
            optimalValue = 15.0,
            worstMinValue = 0.0,
            worstMaxValue = 20.0,
            weight = 2.0,
            evaluationFunction = EvaluationFunctions.LINEAR
        )
        val result = WeatherEvaluation.evaluate(17.5, properties)
        assertEquals(1.0, result)
    }

    @Test
    fun test_getDistanceFromOptimalValue_exponentialEvaluation() {
        val properties = EvaluationProperties(
            optimalValue = 10.0,
            worstMinValue = 0.0,
            worstMaxValue = 20.0,
            weight = 2.0,
            evaluationFunction = EvaluationFunctions.SQUARE
        )
        val result = WeatherEvaluation.evaluate(15.0, properties)
        assertEquals(1.5, result)
    }
}
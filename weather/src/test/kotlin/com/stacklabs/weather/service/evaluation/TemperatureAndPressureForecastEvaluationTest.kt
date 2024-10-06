package com.stacklabs.weather.service.evaluation


import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TemperatureAndPressureForecastEvaluationTest {

    private val temperatureProperties = ValueEvaluationProperties(
        weight = 2.0,
        optimalValue = 20.0,
        worstValue = 0.0
    )

    private val pressureProperties = ValueEvaluationProperties(
        weight = 2.0,
        optimalValue = 1100.0,
        worstValue = 900.0
    )

    private val temperatureEvaluation = OptimalValueEvaluation(temperatureProperties)

    private val pressureEvaluation = LinearValueEvaluation(pressureProperties)

    private val evaluation = TemperatureAndPressureForecastEvaluation(temperatureEvaluation, pressureEvaluation)

    @Test
    fun test_evaluate_optimalValidTemperatureAndPressure() {
        val forecastOptimal =
            WeatherForecastEntity(temperature = 20.0, pressure = 1100.0, windSpeed = 5.0)
        val resultOptimal = evaluation.evaluate(forecastOptimal)
        assertEquals(4.0, resultOptimal)
    }

    @Test
    fun test_evaluate_worstValidTemperatureAndPressure() {
        val forecastWorst =
            WeatherForecastEntity(temperature = 0.0, pressure = 900.0, windSpeed = 5.0)
        val resultWorst = evaluation.evaluate(forecastWorst)
        assertEquals(0.0, resultWorst)
    }

    @Test
    fun test_evaluate_missingTemperature() {
        val forecast =
            WeatherForecastEntity(temperature = null, pressure = 1000.0, windSpeed = 5.0)
        assertThrows(RuntimeException::class.java) {
            evaluation.evaluate(forecast)
        }
    }

    @Test
    fun test_evaluate_missingPressure() {
        val forecast =
            WeatherForecastEntity(temperature = 15.0, pressure = null, windSpeed = 5.0)
        assertThrows(RuntimeException::class.java) {
            evaluation.evaluate(forecast)
        }
    }

}
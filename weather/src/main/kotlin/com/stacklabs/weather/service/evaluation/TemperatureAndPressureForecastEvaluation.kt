package com.stacklabs.weather.service.evaluation

import com.stacklabs.weather.entity.WeatherForecastEntity

class TemperatureAndPressureForecastEvaluation(
    private val temperatureEvaluation: ValueEvaluation,
    private val pressureEvaluation: ValueEvaluation
): ForecastEvaluation {
    override fun evaluate(forecast: WeatherForecastEntity): Double =
        temperatureEvaluation.evaluate(
            forecast.temperature
                ?: throw RuntimeException("Not able to evaluate forecast, missing temperature")
        ) + pressureEvaluation.evaluate(
            forecast.pressure
                ?: throw RuntimeException("Not able to evaluate forecast, missing pressure")
        )
}
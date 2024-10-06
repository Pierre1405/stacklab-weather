package com.stacklabs.weather.service.evaluation

import com.stacklabs.weather.entity.WeatherForecastEntity

interface ForecastEvaluation {
    fun evaluate(forecast: WeatherForecastEntity): Double
}
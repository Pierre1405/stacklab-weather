package com.stacklabs.weather.entity

import java.time.LocalDate

data class WeatherForecastEntity(
    val temperature: Double?,
    val pressure: Double?,
    val windSpeed: Double?,
    val datetime: LocalDate = LocalDate.now()
)
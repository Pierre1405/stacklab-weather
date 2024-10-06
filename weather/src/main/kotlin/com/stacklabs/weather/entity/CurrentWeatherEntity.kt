package com.stacklabs.weather.entity

data class CurrentWeatherEntity(
    val description: String?,
    val temperature: Double?,
    val windSpeed: Double?,
    val humidity: Int?
)

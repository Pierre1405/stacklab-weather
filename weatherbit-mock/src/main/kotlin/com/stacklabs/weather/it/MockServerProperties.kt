package com.stacklabs.weather.it

data class MockServerProperties(
    val baseUrl: String,
    val apiKey: String,
    val forecastNbDays: Int,
)
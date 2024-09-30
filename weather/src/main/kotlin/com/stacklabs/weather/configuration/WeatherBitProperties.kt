package com.stacklabs.weather.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "external.weatherbit")
data class WeatherBitProperties(
    val baseUrl: String,
    val apiKey: String
)

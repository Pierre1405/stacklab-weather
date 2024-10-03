package com.stacklabs.weather.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "external.weatherbit")
data class WeatherBitProperties @ConstructorBinding constructor(
    val baseUrl: String,
    val apiKey: String
)

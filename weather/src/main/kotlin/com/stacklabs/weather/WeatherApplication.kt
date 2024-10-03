package com.stacklabs.weather

import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.configuration.WeatherEvaluationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(WeatherBitProperties::class, WeatherEvaluationConfiguration::class)
class WeatherApplication

fun main(args: Array<String>) {
    runApplication<WeatherApplication>(*args)
}
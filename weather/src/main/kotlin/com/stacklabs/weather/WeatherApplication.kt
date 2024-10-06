package com.stacklabs.weather

import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.configuration.ForecastEvaluationConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(WeatherBitProperties::class, ForecastEvaluationConfiguration::class)
class WeatherApplication

fun main(args: Array<String>) {
    runApplication<WeatherApplication>(*args)
}
package com.stacklabs.weather.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import com.stacklabs.weather.cache.DelegateCacheWithLog
import com.stacklabs.weather.cache.WeatherBitCachePolicy
import com.stacklabs.weather.repository.WeatherBitRepository
import com.stacklabs.weather.service.evaluation.TemperatureAndPressureForecastEvaluation
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient


@Configuration
class AppConfiguration {

    @Value("\${external.weatherbit.cache-size}") lateinit var cacheSize: String


    @Bean
    fun getWeatherBitClient(
        weatherBitProperties: WeatherBitProperties
    ) = WeatherBitRepository(
        configuration = weatherBitProperties,
        restClient = RestClient.create(weatherBitProperties.baseUrl),
        currentApiCache = DelegateCacheWithLog(
            delegate = Caffeine.newBuilder()
                .maximumSize(cacheSize.toLong())
                .expireAfter(WeatherBitCachePolicy<CurrentObsGroup>())
                .build(),
            name = "Current"
        ),
        forecastCache = DelegateCacheWithLog(
            delegate = Caffeine.newBuilder()
                .maximumSize(cacheSize.toLong())
                .expireAfter(WeatherBitCachePolicy<ForecastDay>()).build(),
            name = "Forecast"
        )
    )

    @Bean
    fun weatherEvaluation(weatherEvaluationConfiguration: ForecastEvaluationConfiguration) =
        TemperatureAndPressureForecastEvaluation(
            temperatureEvaluation = weatherEvaluationConfiguration.temperature.toValueEvaluation(),
            pressureEvaluation = weatherEvaluationConfiguration.pressure.toValueEvaluation()
        )
}
package com.stacklabs.weather.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import com.stacklabs.weather.cache.WeatherBitCachePolicy
import com.stacklabs.weather.repository.WeatherBitRepository
import com.stacklabs.weather.service.evaluation.TemperatureAndPressureForecastEvaluation
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient


@Configuration
class AppConfiguration {

    @Bean
    fun getWeatherBitClient(
        weatherBitProperties: WeatherBitProperties
    ) = WeatherBitRepository(
        configuration = weatherBitProperties,
        restClient = RestClient.create(weatherBitProperties.baseUrl),
        currentApiCache = Caffeine.newBuilder().expireAfter(WeatherBitCachePolicy<CurrentObsGroup>()).build(),
        forecastCache = Caffeine.newBuilder().expireAfter(WeatherBitCachePolicy<ForecastDay>()).build()
    )

    @Bean
    fun weatherEvaluation(weatherEvaluationConfiguration: ForecastEvaluationConfiguration) =
        TemperatureAndPressureForecastEvaluation(
            temperatureEvaluation = weatherEvaluationConfiguration.temperature.toValueEvaluation(),
            pressureEvaluation = weatherEvaluationConfiguration.pressure.toValueEvaluation()
        )
}
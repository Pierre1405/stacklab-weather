package com.stacklabs.weather.configuration

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.stacklabs.weather.cache.Result
import com.stacklabs.weather.cache.WeatherBitCachePolicy
import com.stacklabs.weather.repository.WeatherBitRepository
import com.stacklabs.weather.service.evaluation.TemperatureAndPressureForecastEvaluation
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Configuration
class AppConfiguration {

    @Bean
    fun getWeatherBitClient(
        weatherBitProperties: WeatherBitProperties,
        currentApiCache: Cache<String, Result<CurrentObsGroup>>
    ) =
        WeatherBitRepository(
            configuration = weatherBitProperties,
            restClient = RestClient.create(weatherBitProperties.baseUrl),
            currentApiCache = currentApiCache,
            forecastCache = Caffeine.newBuilder().expireAfter(
                WeatherBitCachePolicy<ForecastDay>()
            ).build()
        )

    @Bean
    fun currentApiCache(weatherBitProperties: WeatherBitProperties): Cache<String, Result<CurrentObsGroup>> =
        Caffeine.newBuilder()
            .expireAfter(
                WeatherBitCachePolicy<CurrentObsGroup>(
                    maxCacheLifeDuration = weatherBitProperties
                        .currentWeatherRefreshCacheDurationInMinutes
                        .toDuration(DurationUnit.MINUTES),
                )
            ).build()

    @Bean
    fun forecastApiCache(weatherBitProperties: WeatherBitProperties): Cache<String, Result<ForecastDay>> =
        Caffeine.newBuilder().expireAfter(WeatherBitCachePolicy<ForecastDay>()).build()

    @Bean
    fun weatherEvaluation(weatherEvaluationConfiguration: ForecastEvaluationConfiguration) =
        TemperatureAndPressureForecastEvaluation(
            temperatureEvaluation = weatherEvaluationConfiguration.temperature.toValueEvaluation(),
            pressureEvaluation = weatherEvaluationConfiguration.pressure.toValueEvaluation()
        )
}
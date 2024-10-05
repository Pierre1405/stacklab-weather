package com.stacklabs.weather.configuration

import com.stacklabs.weather.repository.WeatherBitRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient


@Configuration
class AppConfiguration {

    @Bean
    fun weatherBitRestClient(weatherBitProperties: WeatherBitProperties): RestClient =
        RestClient.create(weatherBitProperties.baseUrl)

    @Bean
    fun getWeatherBitClient(
        weatherBitProperties: WeatherBitProperties,
        restClient: RestClient
    ): WeatherBitRepository = WeatherBitRepository(
        restClient = restClient,
        apiKey = weatherBitProperties.apiKey,
        forecastNbDays = weatherBitProperties.forecastNbDays
    )

    @Bean
    fun temperatureEvaluation(weatherEvaluationConfiguration: WeatherEvaluationConfiguration) =
        weatherEvaluationConfiguration.temperature

    @Bean
    fun pressureEvaluation(weatherEvaluationConfiguration: WeatherEvaluationConfiguration) =
        weatherEvaluationConfiguration.pressure

}
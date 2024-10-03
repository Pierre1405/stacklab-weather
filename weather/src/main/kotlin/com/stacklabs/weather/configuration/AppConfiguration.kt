package com.stacklabs.weather.configuration

import com.stacklabs.weather.repository.WeatherBitClient
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient


@Configuration
class AppConfiguration {

    @Bean
    fun weatherBitRestClient(weatherBitProperties: WeatherBitProperties): RestClient =
        RestClient.create(weatherBitProperties.baseUrl)

    @Bean
    fun getCurrentWeather(
        weatherBitProperties: WeatherBitProperties,
        restClient: RestClient
    ): (String) -> CurrentObsGroup = WeatherBitClient.currentWeather(
        key = weatherBitProperties.apiKey,
        restClient = restClient
    )

    @Bean
    fun getWeatherForecast(
        weatherBitProperties: WeatherBitProperties,
        restClient: RestClient
    ): (String) -> ForecastDay = WeatherBitClient.weatherForecast(
        key = weatherBitProperties.apiKey,
        restClient = restClient
    )

    @Bean
    fun temperatureEvaluation(weatherEvaluationConfiguration: WeatherEvaluationConfiguration) =
        weatherEvaluationConfiguration.temperature

    @Bean
    fun pressureEvaluation(weatherEvaluationConfiguration: WeatherEvaluationConfiguration) =
        weatherEvaluationConfiguration.pressure

}
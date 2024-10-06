package com.stacklabs.weather.configuration

import com.stacklabs.weather.repository.WeatherBitRepository
import com.stacklabs.weather.service.evaluation.ForecastEvaluation
import com.stacklabs.weather.service.evaluation.TemperatureAndPressureForecastEvaluation
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
        weatherBitProperties,
        restClient
    )

    @Bean
    fun weatherEvaluation(weatherEvaluationConfiguration: ForecastEvaluationConfiguration): ForecastEvaluation =
        TemperatureAndPressureForecastEvaluation(
            temperatureEvaluation = weatherEvaluationConfiguration.temperature.toValueEvaluation(),
            pressureEvaluation = weatherEvaluationConfiguration.pressure.toValueEvaluation()
        )


}
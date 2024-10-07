package com.stacklabs.weather.service

import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.repository.WeatherBitRepository
import com.stacklabs.weather.service.evaluation.LinearValueEvaluation
import com.stacklabs.weather.service.evaluation.OptimalValueEvaluation
import com.stacklabs.weather.service.evaluation.TemperatureAndPressureForecastEvaluation
import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.stacklabs.weather.dto.*
import java.time.LocalDate

class WeatherbitWeatherServiceTest {

    private val weatherBitRepository = mock<WeatherBitRepository>()

    private val service = WeatherbitWeatherService(
        repository = weatherBitRepository,
        forecastEvaluation = TemperatureAndPressureForecastEvaluation(
            temperatureEvaluation = OptimalValueEvaluation(
                ValueEvaluationProperties(
                    optimalValue = 23.0,
                    worstValue = 40.0,
                    weight = 20.0,
                )
            ),
            pressureEvaluation = LinearValueEvaluation(
                ValueEvaluationProperties(
                    optimalValue = 1100.0,
                    worstValue = 1000.0,
                    weight = 5.0
                )
            )
        ),
        pressureBigFallDelta = 4.0
    )

    @Test
    fun test_getCurrentWeather_validData() {
        `when`(weatherBitRepository.getCurrentWeatherByCity("Tokyo")).thenReturn(
            CurrentWeatherEntity(
                description = "Clear sky",
                temperature = 25.0,
                humidity = 88,
                windSpeed = 7.2
            )
        )

        val result: CurrentWeatherDto = service.getCurrentWeather("Tokyo")

        val expected = CurrentWeatherDto(
            description = "Clear sky",
            temperature = 25.0,
            humidity = 88,
            windSpeed = 7.2
        )
        assertEquals(expected, result)
    }

    @Test
    fun test_getWeatherForecast_validData() {
        val forecast1 = WeatherForecastEntity(
            temperature = 20.0,
            pressure = 1010.0,
            windSpeed = 1.38, // 5km/h
            datetime = LocalDate.parse("2023-10-01")
        )
        val forecast2 = WeatherForecastEntity(
            temperature = 22.0,
            pressure = 1005.0,
            windSpeed = 3.3, // 12km/h
            datetime = LocalDate.parse("2023-10-02")
        )
        val forecastDay = WeatherForecastsEntity(data = listOf(forecast1, forecast2))

        `when`(weatherBitRepository.getWeatherForecastByCity("Tokyo")).thenReturn(forecastDay)

        val result: WeatherForecastDto = service.getWeatherForecast("Tokyo")

        val expected = WeatherForecastDto(
            globalTendency = Tendency.INCREASING,
            temperatureTendency = Tendency.INCREASING,
            pressureTendency = BigTendency.DECREASING,
            windAverage = BeaufortScale.LIGHT_BREEZE
        )
        assertEquals(expected, result)
    }

    @Test
    fun test_getWeatherForecast_tempIsNull() {
        val forecast1 = WeatherForecastEntity(
            temperature = null,
            pressure = 1010.0,
            windSpeed = 5.0,
            datetime = LocalDate.parse("2023-10-01")
        )
        val forecastDay = WeatherForecastsEntity(data = listOf(forecast1))

        `when`(weatherBitRepository.getWeatherForecastByCity("Tokyo")).thenReturn(forecastDay)

        val exception = assertThrows<WeatherServiceException> {
            service.getWeatherForecast("Tokyo")
        }

        assertEquals("Not able to retrieve weather forecast, a temperature is null", exception.message)
    }

}
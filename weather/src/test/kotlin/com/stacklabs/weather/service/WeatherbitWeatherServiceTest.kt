package com.stacklabs.weather.service

import com.stacklabs.weather.SampleReader
import com.stacklabs.weather.configuration.EvaluationProperties
import com.stacklabs.weather.repository.WeatherBitRepository
import com.stacklabs.weather.service.WeatherEvaluation.EvaluationFunctions.LINEAR
import com.stacklabs.weather.service.WeatherEvaluation.EvaluationFunctions.SQUARE
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.stacklabs.weather.dto.CurrentWeatherDto
import com.stacklabs.weather.weatherbit.models.CurrentObs
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.Forecast
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.stacklabs.weather.dto.BeaufortScale
import org.stacklabs.weather.dto.Tendency
import org.stacklabs.weather.dto.WeatherForecastDto
import java.math.BigDecimal

class WeatherbitWeatherServiceTest {

    private val weatherBitRepository = mock<WeatherBitRepository>()

    private val temperatureEvaluation = EvaluationProperties(
        optimalValue = 23.0,
        worstMinValue = -10.0,
        worstMaxValue = 40.0,
        weight = 20.0,
        evaluationFunction = SQUARE
    )
    private val pressureEvaluation = EvaluationProperties(
        optimalValue = 1000.0,
        worstMinValue = 900.0,
        worstMaxValue = 1100.0,
        weight = 5.0,
        evaluationFunction = LINEAR
    )
    private val service = WeatherbitWeatherService(
        weatherBitRepository,
        temperatureEvaluation,
        pressureEvaluation
    )

    @Test
    fun test_getCurrentWeather_validData() {
        val currentObsGroup = SampleReader().readSampleAs<CurrentObsGroup>("api-samples/current-tokyo.json")
        `when`(weatherBitRepository.getCurrentWeatherByCity("Tokyo")).thenReturn(currentObsGroup)

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
    fun test_getCurrentWeather_invalidData() {
        testInvalidData(null, "no data found")
        testInvalidData(listOf(), "empty data")
        testInvalidData(listOf(CurrentObs(), CurrentObs()), "more than one data found")
    }

    private fun testInvalidData(dataList: List<CurrentObs>?, expectedMessage: String) {
        val currentObsGroup = CurrentObsGroup(data = dataList)
        `when`(weatherBitRepository.getCurrentWeatherByCity("Tokyo")).thenReturn(currentObsGroup)

        val exception = assertThrows<RuntimeException> {
            service.getCurrentWeather("Tokyo")
        }
        assertEquals("Not able to retrieve current weather, $expectedMessage", exception.message)
    }

    @Test
    fun handles_cases_where_data_is_null() {
        val currentObs = CurrentObs(
            temp = null,
            weather = null,
            rh = null,
            windSpd = null
        )
        val currentObsGroup = CurrentObsGroup(count = 1, data = listOf(currentObs))

        `when`(weatherBitRepository.getCurrentWeatherByCity("Tokyo")).thenReturn(currentObsGroup)

        val result = service.getCurrentWeather("Tokyo")

        val expected = CurrentWeatherDto(
            description = null,
            temperature = null,
            humidity = null,
            windSpeed = null
        )
        assertEquals(expected, result)
    }

    @Test
    fun test_getWeatherForecast_validData() {
        val forecast1 = Forecast(
            temp = BigDecimal(20),
            pres = BigDecimal(1010),
            windSpd = BigDecimal(1.38), // 5km/h
            datetime = "2023-10-01"
        )
        val forecast2 = Forecast(
            temp = BigDecimal(22),
            pres = BigDecimal(1005),
            windSpd = BigDecimal(3.3), // 12km/h
            datetime = "2023-10-02"
        )
        val forecastDay = ForecastDay(data = listOf(forecast1, forecast2))

        `when`(weatherBitRepository.getWeatherForecastByCity("Tokyo")).thenReturn(forecastDay)

        val result: WeatherForecastDto = service.getWeatherForecast("Tokyo")

        val expected = WeatherForecastDto(
            globalTendency = Tendency.INCREASING,
            temperatureTendency = Tendency.INCREASING,
            pressureTendency = Tendency.DECREASING,
            windAverage = BeaufortScale.LIGHT_BREEZE
        )
        assertEquals(expected, result)
    }

    @Test
    fun test_getWeatherForecast_nullData() {
        val forecastDay = ForecastDay(data = null)

        `when`(weatherBitRepository.getWeatherForecastByCity("Tokyo")).thenReturn(forecastDay)

        val exception = assertThrows<WeatherServiceException> {
            service.getWeatherForecast("Tokyo")
        }

        assertEquals("Not able to retrieve weather forecast, no data found", exception.message)
    }

    @Test
    fun test_getWeatherForecast_tempIsNull () {
        val forecast1 = Forecast(
            temp = null,
            pres = BigDecimal(1010),
            windSpd = BigDecimal(5),
            datetime = "2023-10-01"
        )
        val forecastDay = ForecastDay(data = listOf(forecast1))

        `when`(weatherBitRepository.getWeatherForecastByCity("Tokyo")).thenReturn(forecastDay)

        val exception = assertThrows<WeatherServiceException> {
            service.getWeatherForecast("Tokyo")
        }

        assertEquals("Not able to evaluate global tendency", exception.message)
    }

}
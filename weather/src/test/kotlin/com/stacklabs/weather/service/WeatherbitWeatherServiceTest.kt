package com.stacklabs.weather.service

import com.stacklabs.weather.SampleReader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.stacklabs.weather.dto.CurrentWeatherDto
import com.stacklabs.weather.weatherbit.models.CurrentObs
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay

class WeatherbitWeatherServiceTest {

    private val getCurrentMock = mock<(String) -> CurrentObsGroup>()
    private val getWeatherForecastMock = mock<(String) -> ForecastDay>()
    private val service = WeatherbitWeatherService(getCurrentMock, getWeatherForecastMock)

    @Test
    fun test_getCurrentWeather_validData() {
        val currentObsGroup = SampleReader().readSample<CurrentObsGroup>("api-samples/current-tokyo.json")
        `when`(getCurrentMock.invoke("Tokyo")).thenReturn(currentObsGroup)

        val result: CurrentWeatherDto = service.getCurrentWeather("Tokyo")

        val expected = CurrentWeatherDto(
            description = "Clear sky",
            temperature = 25.0f,
            humidity = 88,
            windSpeed = 7.2f
        )
        assertEquals(expected, result)
    }

    @Test
    fun test_getCurrentWeather_apiError() {
        `when`(getCurrentMock.invoke("Tokyo")).thenThrow(RuntimeException("API error"))

        val exception = assertThrows<RuntimeException> {
            service.getCurrentWeather("Tokyo")
        }
        assertEquals("Not able to retrieve current weather", exception.message)
        assertEquals("API error", exception.cause?.message)
    }

    @Test
    fun test_getCurrentWeather_invalidData() {
        testInvalidData(null, "no data found")
        testInvalidData(listOf(), "no data found")
        testInvalidData(listOf(CurrentObs(), CurrentObs()), "multiple data found")
    }

    private fun testInvalidData(dataList: List<CurrentObs>?, expectedMessage: String) {
        val currentObsGroup = CurrentObsGroup(data = dataList)
        `when`(getCurrentMock.invoke("Tokyo")).thenReturn(currentObsGroup)

        val exception = assertThrows<RuntimeException> {
            service.getCurrentWeather("Tokyo")
        }
        assertEquals("Not able to retrieve current weather, $expectedMessage", exception.message)
    }

    @Test
    fun handles_cases_where_temperature_is_null() {
        val currentObs = CurrentObs(
            temp = null,
            weather = null,
            rh = null,
            windSpd = null
        )
        val currentObsGroup = CurrentObsGroup(count = 1, data = listOf(currentObs))

        `when`(getCurrentMock.invoke("Tokyo")).thenReturn(currentObsGroup)

        val result = service.getCurrentWeather("Tokyo")

        val expected = CurrentWeatherDto(
            description = "",
            temperature = Float.MIN_VALUE,
            humidity = Int.MIN_VALUE,
            windSpeed = Float.MIN_VALUE
        )
        assertEquals(expected, result)
    }
}
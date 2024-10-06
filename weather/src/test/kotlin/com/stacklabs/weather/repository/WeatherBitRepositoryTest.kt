package com.stacklabs.weather.repository

import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.web.client.RestClient
import java.time.LocalDate

class WeatherBitRepositoryTest {

    private val apiKey = "testApiKey"
    private val forecastNbDays = 5
    private val configuration = WeatherBitProperties("http://test.url", apiKey, forecastNbDays)
    private val restClient = mock(RestClient::class.java)

    @Test
    fun test_getCurrentWeatherByCity_validCity() {
        val city = "TestCity"
        val currentObs = CurrentObs(
            weather = CurrentObsWeather(description = "Clear sky"),
            temp = java.math.BigDecimal(25.0),
            rh = 60,
            windSpd = java.math.BigDecimal(5.0)
        )
        val currentObsGroup = CurrentObsGroup(data = listOf(currentObs))
        val currentWeatherDataApi = mock(CurrentWeatherDataApi::class.java)
        `when`(currentWeatherDataApi.currentGet(apiKey, city = city)).thenReturn(currentObsGroup)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi
        )

        val result = repository.getCurrentWeatherByCity(city)

        assertEquals("Clear sky", result.description)
        assertEquals(25.0, result.temperature)
        assertEquals(60, result.humidity)
        assertEquals(5.0, result.windSpeed)
    }

    @Test
    fun test_getCurrentWeatherByCity_emptyData() {
        val city = "TestCity"
        val currentObsGroup = CurrentObsGroup(data = emptyList())
        val currentWeatherDataApi = mock(CurrentWeatherDataApi::class.java)
        `when`(currentWeatherDataApi.currentGet(apiKey, city = city)).thenReturn(currentObsGroup)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi
        )

        assertThrows(WeatherBitRepositoryException::class.java) {
            repository.getCurrentWeatherByCity(city)
        }
    }

    @Test
    fun test_getCurrentWeatherByCity_nullData() {
        val city = "TestCity"
        val currentObsGroup = CurrentObsGroup(data = null)
        val currentWeatherDataApi = mock(CurrentWeatherDataApi::class.java)
        `when`(currentWeatherDataApi.currentGet(apiKey, city = city)).thenReturn(currentObsGroup)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi
        )

        assertThrows(WeatherBitRepositoryException::class.java) {
            repository.getCurrentWeatherByCity(city)
        }
    }

    @Test
    fun test_getCurrentWeatherByCity_moreThanOneData() {
        val city = "TestCity"
        val currentObsGroup = CurrentObsGroup(data = listOf(CurrentObs(), CurrentObs()))
        val currentWeatherDataApi = mock(CurrentWeatherDataApi::class.java)
        `when`(currentWeatherDataApi.currentGet(apiKey, city = city)).thenReturn(currentObsGroup)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi
        )

        assertThrows(WeatherBitRepositoryException::class.java) {
            repository.getCurrentWeatherByCity(city)
        }
    }

    @Test
    fun test_getWeatherForecastByCity_validCity() {
        val city = "TestCity"
        val forecast = Forecast(
            datetime = "2023-10-10",
            temp = java.math.BigDecimal(20.0),
            pres = java.math.BigDecimal(1013.0),
            windSpd = java.math.BigDecimal(3.0)
        )
        val forecastDay = ForecastDay(data = listOf(forecast))
        val class16DayDailyForecastApi = mock(Class16DayDailyForecastApi::class.java)
        `when`(
            class16DayDailyForecastApi.forecastDailyGet(
                apiKey,
                city = city,
                days = forecastNbDays.toBigDecimal()
            )
        ).thenReturn(forecastDay)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            weatherForecastDataApi = class16DayDailyForecastApi
        )
        val result = repository.getWeatherForecastByCity(city)

        assertEquals(1, result.data.size)
        assertEquals(LocalDate.parse("2023-10-10"), result.data[0].datetime)
        assertEquals(20.0, result.data[0].temperature)
        assertEquals(1013.0, result.data[0].pressure)
        assertEquals(3.0, result.data[0].windSpeed)
    }


    @Test
    fun test_getWeatherForecastByCity_nullData() {
        val city = "TestCity"
        val forecastDay = ForecastDay(data = null)
        val class16DayDailyForecastApi = mock(Class16DayDailyForecastApi::class.java)
        `when`(
            class16DayDailyForecastApi.forecastDailyGet(
                apiKey,
                city = city,
                days = forecastNbDays.toBigDecimal()
            )
        ).thenReturn(forecastDay)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            weatherForecastDataApi = class16DayDailyForecastApi
        )

        assertThrows(WeatherBitRepositoryException::class.java) {
            repository.getWeatherForecastByCity(city)
        }
    }
}
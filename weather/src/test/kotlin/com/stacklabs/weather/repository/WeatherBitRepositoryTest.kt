package com.stacklabs.weather.repository

import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
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
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenReturn(
            ResponseEntity.ok(
                currentObsGroup
            )
        )

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
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenReturn(
            ResponseEntity.ok(
                currentObsGroup
            )
        )

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
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenReturn(
            ResponseEntity.ok(
                currentObsGroup
            )
        )

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
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenReturn(
            ResponseEntity.ok(
                currentObsGroup
            )
        )

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
            class16DayDailyForecastApi.forecastDailyGetWithHttpInfo(
                apiKey,
                city = city,
                days = forecastNbDays.toBigDecimal()
            )
        ).thenReturn(ResponseEntity.ok(forecastDay))

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            weatherForecastDataApi = class16DayDailyForecastApi
        )
        val result = repository.getWeatherForecastByCity(city)

        val expected = WeatherForecastsEntity(
            data = listOf(
                WeatherForecastEntity(
                    datetime = LocalDate.parse("2023-10-10"),
                    temperature = 20.0,
                    pressure = 1013.0,
                    windSpeed = 3.0
                )
            )
        )
        assertEquals(expected, result)
    }


    @Test
    fun test_getWeatherForecastByCity_nullData() {
        val city = "TestCity"
        val forecastDay = ForecastDay(data = null)
        val class16DayDailyForecastApi = mock(Class16DayDailyForecastApi::class.java)
        `when`(
            class16DayDailyForecastApi.forecastDailyGetWithHttpInfo(
                apiKey,
                city = city,
                days = forecastNbDays.toBigDecimal()
            )
        ).thenReturn(ResponseEntity.ok(forecastDay))

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            weatherForecastDataApi = class16DayDailyForecastApi
        )

        assertThrows(WeatherBitRepositoryException::class.java) {
            repository.getWeatherForecastByCity(city)
        }
    }

    @Test
    fun test_getCurrentWeatherByCity_cached() {
        val city = "TestCity"
        val currentObs = CurrentObs(
            weather = CurrentObsWeather(description = "Clear sky"),
            temp = java.math.BigDecimal(25.0),
            rh = 60,
            windSpd = java.math.BigDecimal(5.0)
        )
        val currentObsGroup = CurrentObsGroup(data = listOf(currentObs))
        val currentWeatherDataApi = mock(CurrentWeatherDataApi::class.java)

        val headers = HttpHeaders()
        val nowAnd10seconds = (System.currentTimeMillis() / 1000 + 2).toString()
        headers.add("X-RateLimit-Reset", nowAnd10seconds)
        val ok = ResponseEntity.ok().headers(headers).body(currentObsGroup)
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenReturn(ok)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi
        )
        val expected = CurrentWeatherEntity(
            description = "Clear sky",
            temperature = 25.0,
            humidity = 60,
            windSpeed = 5.0
        )
        assertEquals(expected, repository.getCurrentWeatherByCity(city))
        verify(currentWeatherDataApi, times(1)).currentGetWithHttpInfo(apiKey, city = city)
        assertEquals(expected, repository.getCurrentWeatherByCity(city))
        verify(currentWeatherDataApi, times(1)).currentGetWithHttpInfo(apiKey, city = city)

        Thread.sleep(3000)
        assertEquals(expected, repository.getCurrentWeatherByCity(city))
        verify(currentWeatherDataApi, times(2)).currentGetWithHttpInfo(apiKey, city = city)
    }

    @Test
    fun test_getWeatherForecastByCity_cached() {
        val city = "TestCity"
        val forecast = Forecast(
            datetime = "2023-10-10",
            temp = java.math.BigDecimal(20.0),
            pres = java.math.BigDecimal(1013.0),
            windSpd = java.math.BigDecimal(3.0)
        )
        val forecastDay = ForecastDay(data = listOf(forecast))

        val headers = HttpHeaders()
        val nowAnd10seconds = (System.currentTimeMillis() / 1000 + 2).toString()
        headers.add("X-RateLimit-Reset", nowAnd10seconds)
        val ok = ResponseEntity.ok().headers(headers).body(forecastDay)
        val class16DayDailyForecastApi = mock(Class16DayDailyForecastApi::class.java)
        `when`(
            class16DayDailyForecastApi.forecastDailyGetWithHttpInfo(
                key = apiKey,
                city = city,
                days = forecastNbDays.toBigDecimal()
            )
        ).thenReturn(ok)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            weatherForecastDataApi = class16DayDailyForecastApi
        )

        val expected = WeatherForecastsEntity(
            data = listOf(
                WeatherForecastEntity(
                    datetime = LocalDate.parse("2023-10-10"),
                    temperature = 20.0,
                    pressure = 1013.0,
                    windSpeed = 3.0
                )
            )
        )
        assertEquals(expected, repository.getWeatherForecastByCity(city))
        verify(class16DayDailyForecastApi, times(1)).forecastDailyGetWithHttpInfo(
            key = apiKey,
            city = city,
            days = forecastNbDays.toBigDecimal()
        )
        assertEquals(expected, repository.getWeatherForecastByCity(city))
        verify(class16DayDailyForecastApi, times(1)).forecastDailyGetWithHttpInfo(
            key = apiKey,
            city = city,
            days = forecastNbDays.toBigDecimal()
        )

        Thread.sleep(3000)
        assertEquals(expected, repository.getWeatherForecastByCity(city))
        verify(class16DayDailyForecastApi, times(2)).forecastDailyGetWithHttpInfo(
            key = apiKey,
            city = city,
            days = forecastNbDays.toBigDecimal()
        )
    }
}
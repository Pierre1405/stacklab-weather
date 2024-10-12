package com.stacklabs.weather.repository

import com.github.benmanes.caffeine.cache.Caffeine
import com.stacklabs.weather.cache.Result
import com.stacklabs.weather.cache.WeatherBitCachePolicy
import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.mockito.Mockito.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import java.nio.charset.Charset
import java.time.LocalDate

class WeatherBitRepositoryTest {

    private val apiKey = "testApiKey"
    private val forecastNbDays = 5
    private val configuration = WeatherBitProperties("http://test.url", apiKey, forecastNbDays, 0)
    private val restClient = mock(RestClient::class.java)
    private val currentCache = Caffeine.newBuilder()
            .expireAfter(WeatherBitCachePolicy<CurrentObsGroup>())
            .build<String, Result<CurrentObsGroup>>()
    private val forecastCache = Caffeine.newBuilder()
            .expireAfter(WeatherBitCachePolicy<ForecastDay>())
            .build<String, Result<ForecastDay>>()


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
            currentWeatherDataApi = currentWeatherDataApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getCurrentWeatherByCity(city)) {
            is WeatherRepositoryResult.Success -> {
                assertEquals("Clear sky", result.data.description)
                assertEquals(25.0, result.data.temperature)
                assertEquals(60, result.data.humidity)
                assertEquals(5.0, result.data.windSpeed)
            }
            else -> fail("getCurrentWeatherByCity return an error")
        }
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
            currentWeatherDataApi = currentWeatherDataApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )
        when (val result = repository.getCurrentWeatherByCity(city)) {
            is WeatherRepositoryResult.Error<CurrentWeatherEntity> -> {
                assertEquals("Data.size should be 1", result.message)
            }

            else -> fail("getCurrentWeatherByCity should return a parsing error")
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
            currentWeatherDataApi = currentWeatherDataApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getCurrentWeatherByCity(city)) {
            is WeatherRepositoryResult.Error<CurrentWeatherEntity> -> {
                assertEquals("Response body or body.data null", result.message)
            }
            else -> fail("getCurrentWeatherByCity should return a parsing error")
        }
    }

    @Test
    fun test_getCurrentWeatherByCity_httpError() {
        val city = "TestCity"
        val currentWeatherDataApi = mock(CurrentWeatherDataApi::class.java)
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenThrow(
            HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        )

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getCurrentWeatherByCity(city)) {
            is WeatherRepositoryResult.Error<CurrentWeatherEntity> -> {
                assertEquals("500 INTERNAL_SERVER_ERROR", result.message)
            }

            else -> fail("getCurrentWeatherByCity should return a parsing error")
        }
    }

    @Test
    fun test_getCurrentWeatherByCity_cityNotFoundError() {
        val city = "WrongCity"
        val currentWeatherDataApi = mock(CurrentWeatherDataApi::class.java)
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenThrow(
            HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "",
                "No Location Found".toByteArray(Charset.forName("UTF-8")),
                Charset.forName("UTF-8")
            )
        )

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getCurrentWeatherByCity(city)) {
            is WeatherRepositoryResult.CityNotFound<CurrentWeatherEntity> -> {
                assertEquals(city, result.city)
            }

            else -> fail("getCurrentWeatherByCity should return a parsing error")
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
            currentWeatherDataApi = currentWeatherDataApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getCurrentWeatherByCity(city)) {
            is WeatherRepositoryResult.Error<CurrentWeatherEntity> -> {
                assertEquals("Data.size should be 1", result.message)
            }

            else -> fail("getCurrentWeatherByCity should return a parsing error")
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
            weatherForecastDataApi = class16DayDailyForecastApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getWeatherForecastByCity(city)) {
            is WeatherRepositoryResult.Success -> {
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
                assertEquals(expected, result.data)
            }
            else -> fail("getWeatherForecastByCity return an error")
        }
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
            weatherForecastDataApi = class16DayDailyForecastApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getWeatherForecastByCity(city)) {
            is WeatherRepositoryResult.Error<WeatherForecastsEntity> -> {
                assertEquals("Response body or body.data null", result.message)
            }
            else -> fail("getWeatherForecastByCity should return a parsing error")
        }
    }


    @Test
    fun test_getWeatherForecastByCity_httpError() {
        val city = "TestCity"
        val class16DayDailyForecastApi = mock(Class16DayDailyForecastApi::class.java)
        `when`(
            class16DayDailyForecastApi.forecastDailyGetWithHttpInfo(
                apiKey,
                city = city,
                days = forecastNbDays.toBigDecimal()
            )
        ).thenThrow(
            HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        )

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            weatherForecastDataApi = class16DayDailyForecastApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        when (val result = repository.getWeatherForecastByCity(city)) {
            is WeatherRepositoryResult.Error<WeatherForecastsEntity> -> {
                assertEquals("500 INTERNAL_SERVER_ERROR", result.message)
            }
            else -> fail("getWeatherForecastByCity should return an http client error")
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
        val nowAnd2seconds = (System.currentTimeMillis() / 1000 + 2).toString()
        headers.add("X-RateLimit-Reset", nowAnd2seconds)
        val ok = ResponseEntity.ok().headers(headers).body(currentObsGroup)
        `when`(currentWeatherDataApi.currentGetWithHttpInfo(apiKey, city = city)).thenReturn(ok)

        val repository = WeatherBitRepository(
            configuration,
            restClient,
            currentWeatherDataApi = currentWeatherDataApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        assertTrue(repository.getCurrentWeatherByCity(city) is WeatherRepositoryResult.Success<CurrentWeatherEntity>)
        verify(currentWeatherDataApi, times(1)).currentGetWithHttpInfo(apiKey, city = city)
        assertTrue(repository.getCurrentWeatherByCity(city) is WeatherRepositoryResult.Success<CurrentWeatherEntity>)
        verify(currentWeatherDataApi, times(1)).currentGetWithHttpInfo(apiKey, city = city)

        Thread.sleep(3000)
        assertTrue(repository.getCurrentWeatherByCity(city) is WeatherRepositoryResult.Success<CurrentWeatherEntity>)
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
        val nowAnd2seconds = (System.currentTimeMillis() / 1000 + 2).toString()
        headers.add("X-RateLimit-Reset", nowAnd2seconds)
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
            weatherForecastDataApi = class16DayDailyForecastApi,
            currentApiCache = currentCache,
            forecastCache = forecastCache
        )

        assertTrue(repository.getWeatherForecastByCity(city) is WeatherRepositoryResult.Success<WeatherForecastsEntity>)
        verify(class16DayDailyForecastApi, times(1)).forecastDailyGetWithHttpInfo(
            key = apiKey,
            city = city,
            days = forecastNbDays.toBigDecimal()
        )
        assertTrue(repository.getWeatherForecastByCity(city) is WeatherRepositoryResult.Success<WeatherForecastsEntity>)
        verify(class16DayDailyForecastApi, times(1)).forecastDailyGetWithHttpInfo(
            key = apiKey,
            city = city,
            days = forecastNbDays.toBigDecimal()
        )

        Thread.sleep(3000)
        assertTrue(repository.getWeatherForecastByCity(city) is WeatherRepositoryResult.Success<WeatherForecastsEntity>)
        verify(class16DayDailyForecastApi, times(2)).forecastDailyGetWithHttpInfo(
            key = apiKey,
            city = city,
            days = forecastNbDays.toBigDecimal()
        )
    }
}
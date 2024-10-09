package com.stacklabs.weather.service

import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.repository.WeatherBitRepository
import com.stacklabs.weather.repository.WeatherRepositoryResult
import com.stacklabs.weather.service.evaluation.LinearValueEvaluation
import com.stacklabs.weather.service.evaluation.OptimalValueEvaluation
import com.stacklabs.weather.service.evaluation.TemperatureAndPressureForecastEvaluation
import com.stacklabs.weather.service.evaluation.ValueEvaluation.Companion.ValueEvaluationProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.stacklabs.weather.dto.*
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.test.junit5.JUnit5Asserter.fail

class WeatherbitWeatherServiceTest {

    private val weatherBitRepository = mock<WeatherBitRepository>()

    private val service = WeatherbitWeatherService(
        repository = weatherBitRepository,
        forecastEvaluation = TemperatureAndPressureForecastEvaluation(
            temperatureEvaluation = OptimalValueEvaluation(
                ValueEvaluationProperties(
                    optimalValue = 20.0,
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
            WeatherRepositoryResult.Success(
                CurrentWeatherEntity(
                    description = "Clear sky",
                    temperature = 25.0,
                    humidity = 88,
                    windSpeed = 7.2
                )
            )
        )

        when (val result = service.getCurrentWeather("Tokyo")) {
            is WeatherServiceResult.Success -> {
                val expected = CurrentWeatherDto(
                    description = "Clear sky",
                    temperature = 25.0,
                    humidity = 88,
                    windSpeed = 25.92
                )
                assertEquals(expected, result.data)
            }

            else -> fail("getCurrentWeather should return a success")
        }
    }

    @Test
    fun test_getCurrentWeather__error() {
        val city = "Tokyo"
        `when`(weatherBitRepository.getCurrentWeatherByCity(city)).thenReturn(
            WeatherRepositoryResult.Error(
                "An error occurs"
            )
        )

        when (val result = service.getCurrentWeather(city)) {
            is WeatherServiceResult.Error -> {
                assertEquals("An error occurs", result.message)
            }

            else -> fail("getCurrentWeather should return an error")
        }
    }

    @Test
    fun test_getCurrentWeather_cityNotFoundError() {
        val city = "WrongCity"
        `when`(weatherBitRepository.getCurrentWeatherByCity(city)).thenReturn(
            WeatherRepositoryResult.CityNotFound(city)
        )

        when (val result = service.getCurrentWeather(city)) {
            is WeatherServiceResult.CityNotFound -> {
                assertEquals(city, result.city)
            }

            else -> fail("getCurrentWeather should return a city not found error")
        }
    }

    @ParameterizedTest
    @MethodSource("weatherForecastScenarios")
    fun test_getWeatherForecast_scenarios(
        scenarioDescription: String,
        weatherForecastData: List<WeatherForecastEntity>,
        expected: WeatherForecastDto
    ) {
        val forecastDay = WeatherForecastsEntity(data = weatherForecastData)

        `when`(weatherBitRepository.getWeatherForecastByCity("Tokyo")).thenReturn(
            WeatherRepositoryResult.Success(
                forecastDay
            )
        )

        when (val result = service.getWeatherForecast("Tokyo")) {
            is WeatherServiceResult.Success -> {
                assertEquals(expected, result.data, scenarioDescription)
            }

            else -> fail("getWeatherForecast should return a success")
        }
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

        `when`(weatherBitRepository.getWeatherForecastByCity("Tokyo")).thenReturn(
            WeatherRepositoryResult.Success(
                forecastDay
            )
        )

        when (val result = service.getWeatherForecast("Tokyo")) {
            is WeatherServiceResult.Error -> {
                assertEquals(
                    "Not able to calculate weather forecast, a temperature, pressure or windSpeed is null",
                    result.message
                )
            }

            else -> fail("getWeatherForecast should return an error")
        }
    }

    @Test
    fun test_getWeatherForecast_cityNotFound() {
        val city = "WrongCity"
        `when`(weatherBitRepository.getWeatherForecastByCity(city)).thenReturn(WeatherRepositoryResult.CityNotFound(city))

        when (val result = service.getWeatherForecast(city)) {
            is WeatherServiceResult.CityNotFound -> {
                assertEquals(city, result.city)
            }

            else -> fail("getWeatherForecast should return a city not found error")
        }
    }

    @Test
    fun test_getWeatherForecast_error() {
        val city = "WrongCity"
        `when`(weatherBitRepository.getWeatherForecastByCity(city)).thenReturn(WeatherRepositoryResult.Error("An error occurs"))

        when (val result = service.getWeatherForecast(city)) {
            is WeatherServiceResult.Error -> {
                assertEquals("An error occurs", result.message)
            }

            else -> fail("getWeatherForecast should return an error")
        }
    }

    companion object {

        private fun optimalEntity(nbDay: Long = 0) = WeatherForecastEntity(
            temperature = 20.0,
            pressure = 1010.0,
            windSpeed = 1.38,
            datetime = LocalDate.now().plusDays(nbDay)
        )

        private fun worstEntity(nbDay: Long = 0) = WeatherForecastEntity(
            temperature = 40.0,
            pressure = 1000.0,
            windSpeed = 1.38,
            datetime = LocalDate.now().plusDays(nbDay)
        )

        private val expected = WeatherForecastDto(
            globalTendency = Tendency.CONSTANT,
            temperatureTendency = Tendency.CONSTANT,
            pressureTendency = BigTendency.CONSTANT,
            windAverage = BeaufortScale.LIGHT_AIR
        )

        @JvmStatic
        fun weatherForecastScenarios(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "Constant data",
                    listOf(
                        optimalEntity(0),
                        optimalEntity(1)
                    ),
                    expected
                ),
                Arguments.of(
                    "Increasing temperature",
                    listOf(
                        optimalEntity(0),
                        optimalEntity(1).copy(temperature = optimalEntity().temperature!! + 1.0),
                        optimalEntity(2).copy(temperature = optimalEntity().temperature!! + 2.0),
                        optimalEntity(3).copy(temperature = optimalEntity().temperature!! - 1.0)
                    ),
                    expected.copy(
                        globalTendency = Tendency.DECREASING,
                        temperatureTendency = Tendency.INCREASING
                    )
                ),
                Arguments.of(
                    "Increasing pressure",
                    listOf(
                        optimalEntity(0),
                        optimalEntity(1).copy(pressure = optimalEntity().pressure!! + 1.0),
                        optimalEntity(2).copy(pressure = optimalEntity().pressure!! + 2.0),
                        optimalEntity(3).copy(pressure = optimalEntity().pressure!! - 1.0)
                    ),
                    expected.copy(
                        globalTendency = Tendency.INCREASING,
                        pressureTendency = BigTendency.INCREASING
                    )
                ),
                Arguments.of(
                    "Big decreasing pressure",
                    listOf(
                        optimalEntity(0),
                        optimalEntity(1).copy(pressure = optimalEntity().pressure!! + 1.0),
                        optimalEntity(2).copy(pressure = optimalEntity().pressure!! + 2.0),
                        optimalEntity(3).copy(pressure = optimalEntity().pressure!! - 16.0)
                    ),
                    expected.copy(
                        globalTendency = Tendency.DECREASING,
                        pressureTendency = BigTendency.BIG_DECREASING // (1+ 2 - 16) / 3 days = 4.333... > pressureBigFallDelta
                    )
                ),
                Arguments.of(
                    "global tendency increasing with increasing temperature",
                    listOf(
                        optimalEntity(0).copy(temperature = optimalEntity().temperature!! - 5.0),
                        optimalEntity(1).copy(temperature = optimalEntity().temperature!! - 1.0),
                        optimalEntity(2).copy(temperature = optimalEntity().temperature!!),
                        optimalEntity(3).copy(temperature = optimalEntity().temperature!! + 1.0)
                    ),
                    expected.copy(
                        globalTendency = Tendency.INCREASING,
                        temperatureTendency = Tendency.INCREASING
                    )
                ),
                Arguments.of(
                    "global tendency increasing with decreasing temperature",
                    listOf(
                        optimalEntity(0).copy(temperature = optimalEntity().temperature!! + 5.0),
                        optimalEntity(1).copy(temperature = optimalEntity().temperature!! + 1.0),
                        optimalEntity(2).copy(temperature = optimalEntity().temperature!!),
                        optimalEntity(3).copy(temperature = optimalEntity().temperature!! - 1.0)
                    ),
                    expected.copy(
                        globalTendency = Tendency.INCREASING,
                        temperatureTendency = Tendency.DECREASING
                    )
                ),
                Arguments.of(
                    "global tendency decreasing with decreasing pressure",
                    listOf(
                        optimalEntity(0).copy(pressure = optimalEntity().pressure!! + 5.0),
                        optimalEntity(1).copy(pressure = optimalEntity().pressure!! + 1.0),
                        optimalEntity(2).copy(pressure = optimalEntity().pressure!!),
                        optimalEntity(3).copy(pressure = optimalEntity().pressure!! - 1.0)
                    ),
                    expected.copy(
                        globalTendency = Tendency.DECREASING,
                        pressureTendency = BigTendency.BIG_DECREASING
                    )
                ),
                Arguments.of(
                    "Wind speed",
                    listOf(
                        optimalEntity(0).copy(),
                        optimalEntity(1).copy(windSpeed = 13.8), // 50km/h
                        optimalEntity(2).copy(windSpeed = 13.8), // 50km/h
                        optimalEntity(3).copy(windSpeed = 27.6)  // 100km/h
                    ),
                    expected.copy(
                        windAverage = BeaufortScale.GALE // ( 50 + 50 + 100 ) / 3 = 66.6km/h => GALE
                    )
                ),
                Arguments.of(
                    "temperature weight is more important than pressure weight to calculate global tendency",
                    listOf(
                        // temperature decrease from worst to optimal
                        // pressure decrease from optimal to worst
                        optimalEntity(0).copy(
                            temperature = worstEntity().temperature
                        ),
                        optimalEntity(1).copy(
                            pressure = worstEntity().pressure
                        ),
                    ),
                    expected.copy(
                        globalTendency = Tendency.INCREASING,
                        temperatureTendency = Tendency.DECREASING,
                        pressureTendency = BigTendency.BIG_DECREASING
                    )
                ),
            )
        }
    }
}
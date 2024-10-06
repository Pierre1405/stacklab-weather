package com.stacklabs.weather.it

import com.stacklabs.weather.WeatherApplication
import com.stacklabs.weather.it.MockServerConfig.Companion.getServerConfig
import com.stacklabs.weather.it.MockServerConfig.Companion.startServer
import com.stacklabs.weather.it.MockServerConfig.Companion.stopServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.stacklabs.weather.dto.*

private const val CURRENT = "/current?city=%s"
private const val FORECAST = "/forecast?city=%s"

@SpringBootTest(classes = [WeatherApplication::class], webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles(MockServerConfig.PROFILE_NAME)
class MockServerTest(
    @Value("\${server.port}")
    val serverPort: Int,
    @Value("\${server.servlet.context-path}")
    val apiPath: String,
) {
    @Suppress("LeakingThis")
    val restClient: RestClient = RestClient.create("""http://localhost:$serverPort$apiPath/weather""")

    @BeforeEach
    fun init() {
        getServerConfig().clearExpectation()
    }

    @Test
    fun test_current_weather_success() {
        val city = "Tokyo"
        getServerConfig().registerCurrentSuccess(city)
        val response = callCurrentApi(city)

        getServerConfig().verifyCurrentRequest(city)
        assertEquals(HttpStatus.OK, response.statusCode)
        val expectedDto = CurrentWeatherDto(
            description = "Clear sky",
            temperature = 25.0,
            windSpeed = 7.2,
            humidity = 88
        )
        assertEquals(expectedDto, response.body)
    }

    @Test
    fun test_current_weather_key_failure() {
        assertFailure(
            city = "Tokyo",
            registerRequest = getServerConfig()::registerCurrentKeyFailure,
            apiCall = ::callCurrentApi,
            verifyRequest = getServerConfig()::verifyCurrentRequest,
            expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @Test
    fun test_current_weather_city_failure() {
        assertFailure(
            city = "WRONG_CITY",
            registerRequest = getServerConfig()::registerCurrentCityFailure,
            apiCall = ::callCurrentApi,
            verifyRequest = getServerConfig()::verifyCurrentRequest,
            expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @Test
    fun test_weather_forecast_success() {
        val city = "Tokyo"
        getServerConfig().registerForecastSuccess(city)
        val response = restClient.get()
            .uri(FORECAST.format(city))
            .retrieve()
            .toEntity(WeatherForecastDto::class.java)

        getServerConfig().verifyForecastRequest(city)
        assertEquals(HttpStatus.OK, response.statusCode)
        val expectedDto = WeatherForecastDto(
            globalTendency = Tendency.INCREASING,
            temperatureTendency = Tendency.DECREASING,
            pressureTendency = BigTendency.BIG_INCREASING,
            windAverage = BeaufortScale.GENTLE_BREEZE
        )
        assertEquals(expectedDto, response.body)
    }

    @Test
    fun test_weather_forecast_key_failure() {
        assertFailure(
            city = "Tokyo",
            registerRequest = getServerConfig()::registerForecastKeyFailure,
            apiCall = ::callForecastApi,
            verifyRequest = getServerConfig()::verifyForecastRequest,
            expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @Test
    fun test_weather_forecast_city_failure() {
        assertFailure(
            city = "WRONG_CITY",
            registerRequest = getServerConfig()::registerForecastCityFailure,
            apiCall = ::callForecastApi,
            verifyRequest = getServerConfig()::verifyForecastRequest,
            expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    private fun callCurrentApi(city: String) =
        restClient.get()
            .uri(CURRENT.format(city))
            .retrieve()
            .toEntity(CurrentWeatherDto::class.java)

    private fun callForecastApi(city: String) =
        restClient.get()
            .uri(FORECAST.format(city))
            .retrieve()
            .toEntity(WeatherForecastDto::class.java)

    private fun assertFailure(
        city: String,
        registerRequest: (String) -> Unit,
        apiCall: (String) -> Unit,
        verifyRequest: (String) -> Unit,
        expectedStatus: HttpStatus
    ) {
        registerRequest(city)
        val exception = assertThrows(
            HttpServerErrorException::class.java
        ) {
            apiCall(city)
        }
        verifyRequest(city)
        assertEquals(expectedStatus, exception.statusCode)
    }

    companion object {

        @JvmStatic
        @BeforeAll
        fun setUp() {
            startServer()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopServer()
        }
    }
}
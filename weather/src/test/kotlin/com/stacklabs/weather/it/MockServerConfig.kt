package com.stacklabs.weather.it

import com.stacklabs.weather.SampleReader
import com.stacklabs.weather.configuration.WeatherBitProperties
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.ClearType
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.mockserver.model.Parameter
import org.mockserver.verify.VerificationTimes
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

class MockServerConfig(private val weatherBitProperties: WeatherBitProperties) {
    private val mockServerUri: URI = URI.create(weatherBitProperties.baseUrl)
    private var clientAndServer: ClientAndServer = ClientAndServer.startClientAndServer(mockServerUri.port)
    private var mockServerClient: MockServerClient = MockServerClient(mockServerUri.host, mockServerUri.port)
    private val logger = LoggerFactory.getLogger(MockServerConfig::class.java)

    fun registerCurrentSuccess(city: String) {
        registerRequest(
            "/current",
            listOf(Parameter("city", city)),
            HttpResponse.response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/current-tokyo.json"))
        )

    }

    fun registerCurrentKeyFailure(city: String) {
        registerRequest(
            "/current",
            listOf(Parameter("city", city)),
            HttpResponse.response()
                .withStatusCode(403)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/current-key_failure.json"))
        )
    }

    fun registerCurrentCityFailure(city: String) {
        registerRequest(
            "/current",
            listOf(Parameter("city", city)),
            HttpResponse.response()
                .withStatusCode(400)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/current-city_failure.json"))
        )
    }

    fun verifyCurrentRequest(city: String) {
        clientAndServer
            .verify(
                request()
                    .withMethod("GET")
                    .withPath("/current")
                    .withQueryStringParameter("key", weatherBitProperties.apiKey)
                    .withQueryStringParameter("city", city),
                VerificationTimes.exactly(1)
            )
    }

    fun registerForecastSuccess(city: String) {
        registerRequest(
            "/forecast/daily",
            listOf(Parameter("city", city), Parameter("days", weatherBitProperties.forecastNbDays.toString())),
            HttpResponse.response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/forecast-tokyo.json"))
        )
    }

    fun registerForecastKeyFailure(city: String) {
        registerRequest(
            "/forecast/daily",
            listOf(Parameter("city", city), Parameter("days", weatherBitProperties.forecastNbDays.toString())),
            HttpResponse.response()
                .withStatusCode(403)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/forecast-key_failure.json"))
        )
    }

    fun registerForecastCityFailure(city: String) {
        registerRequest(
            "/forecast/daily",
            listOf(Parameter("city", city), Parameter("days", weatherBitProperties.forecastNbDays.toString())),
            HttpResponse.response()
                .withStatusCode(204)
        )
    }

    fun verifyForecastRequest(city: String) {
        clientAndServer
            .verify(
                request()
                    .withMethod("GET")
                    .withPath("/forecast/daily")
                    .withQueryStringParameter("key", weatherBitProperties.apiKey)
                    .withQueryStringParameter("city", city)
                    .withQueryStringParameter("days", weatherBitProperties.forecastNbDays.toString()),
                VerificationTimes.exactly(1)
            )

    }

    fun clearExpectation() {
        mockServerClient.clear(request(), ClearType.ALL)
    }

    private fun registerRequest(path: String, queryParameters: List<Parameter>, response: HttpResponse) {
        clientAndServer
            .`when`(
                request()
                    .withMethod("GET")
                    .withPath(path)
                    .withQueryStringParameter("key", weatherBitProperties.apiKey)
                    .withQueryStringParameters(queryParameters),
                Times.exactly(1)
            ).respond(
                response
            )
    }


    fun stopServer() {
        logger.info("Stop mocked server")
        clientAndServer.stop()
        mockServerClient.stop()
    }

    companion object {
        private var serverConfig: MockServerConfig? = null
        private val weatherBitProperties: WeatherBitProperties

        const val PROFILE_NAME = "mockserver"

        init {
            val properties = Properties()
            val defaultProperties = Properties()

            // the startServer will be called in a static @BeforeAll annotated function
            // the spring context won't be loaded, we have to properties in the old style
            properties.load(MockServerTest.Companion::class.java.classLoader.getResourceAsStream("application-$PROFILE_NAME.properties"))
            defaultProperties.load(MockServerTest.Companion::class.java.classLoader.getResourceAsStream("application.properties"))
            weatherBitProperties = WeatherBitProperties(
                apiKey = properties["external.weatherbit.api-key"] as String,
                baseUrl = properties["external.weatherbit.base-url"] as String,
                forecastNbDays = (defaultProperties["external.weatherbit.forecast-nb-days"] as String).toInt(),
                currentWeatherRefreshCacheDurationInMinutes = (defaultProperties["external.weatherbit.current-weather-refresh-cache-duration-in-minutes"] as String).toInt()
            )
        }

        fun startServer() {
            serverConfig = MockServerConfig(weatherBitProperties)
        }

        fun stopServer() {
            getServerConfig().stopServer()
        }

        fun getServerConfig(): MockServerConfig = serverConfig ?: throw IllegalStateException("Server not started")
    }
}
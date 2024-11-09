package com.stacklabs.weather.it

import com.stacklabs.weather.SampleReader
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.*
import org.mockserver.model.HttpRequest.request
import org.mockserver.verify.VerificationTimes
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Instant
import java.util.*

class MockServerConfig(private val weatherBitProperties: MockServerProperties) {
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
                .withBody(SampleReader().readSampleAsString("api-samples/current-tokyo.json"))
        )

    }

    fun registerCurrentKeyFailure(city: String) {
        registerRequest(
            "/current",
            listOf(Parameter("city", city)),
            HttpResponse.response()
                .withStatusCode(403)
                .withBody(SampleReader().readSampleAsString("api-samples/current-key_failure.json"))
        )
    }

    fun registerCurrentCityFailure(city: String) {
        registerRequest(
            "/current",
            listOf(Parameter("city", city)),
            HttpResponse.response()
                .withStatusCode(400)
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
                .withBody(SampleReader().readSampleAsString("api-samples/forecast-tokyo.json"))
        )
    }

    fun registerForecastKeyFailure(city: String) {
        registerRequest(
            "/forecast/daily",
            listOf(Parameter("city", city), Parameter("days", weatherBitProperties.forecastNbDays.toString())),
            HttpResponse.response()
                .withStatusCode(403)
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
        val tenMinutesLater = Instant.now().plusSeconds(60).epochSecond
        clientAndServer
            .`when`(
                request()
                    .withMethod("GET")
                    .withPath(path)
                    .withQueryStringParameter("key", weatherBitProperties.apiKey)
                    .withQueryStringParameters(queryParameters),
                //Times.exactly(1)
            ).respond(
                response
                    .withContentType(MediaType.APPLICATION_JSON)
                    .withHeader(Header("X-RateLimit-Reset", tenMinutesLater.toString()))
            )
    }


    fun stopServer() {
        logger.info("Stop mocked server")
        clientAndServer.stop()
        mockServerClient.stop()
    }

    companion object {
        private var serverConfig: MockServerConfig? = null
        private val weatherBitProperties: MockServerProperties

        const val PROFILE_NAME = "mockserver"

        init {
            val properties = Properties()
            val defaultProperties = Properties()

            // the startServer will be called in a static @BeforeAll annotated function
            // the spring context won't be loaded, we have to properties in the old style
            properties.load(Companion::class.java.classLoader.getResourceAsStream("application-$PROFILE_NAME.properties"))
            defaultProperties.load(Companion::class.java.classLoader.getResourceAsStream("application.properties"))
            weatherBitProperties = MockServerProperties(
                apiKey = properties["external.weatherbit.api-key"] as String,
                baseUrl = properties["external.weatherbit.base-url"] as String,
                forecastNbDays = (defaultProperties["external.weatherbit.forecast-nb-days"] as String).toInt(),
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
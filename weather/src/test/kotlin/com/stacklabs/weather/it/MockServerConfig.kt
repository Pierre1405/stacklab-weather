package com.stacklabs.weather.it

import com.stacklabs.weather.SampleReader
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.model.ClearType
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.mockserver.verify.VerificationTimes
import org.slf4j.LoggerFactory
import java.net.URI

class MockServerConfig(mockServerUri: URI, private val apiKey: String) {
    private var clientAndServer: ClientAndServer = ClientAndServer.startClientAndServer(mockServerUri.port)
    private var mockServerClient: MockServerClient = MockServerClient(mockServerUri.host, mockServerUri.port)
    private val logger = LoggerFactory.getLogger(MockServerConfig::class.java)

    fun registerCurrentSuccess(city: String) {
        registerRequest(
            "/current",
            city,
            HttpResponse.response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/current-tokyo.json"))
        )

    }

    fun registerCurrentKeyFailure(city: String) {
        registerRequest(
            "/current",
            city,
            HttpResponse.response()
                .withStatusCode(403)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/current-key_failure.json"))
        )
    }

    fun registerCurrentCityFailure(city: String) {
        registerRequest(
            "/current",
            city,
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
                    .withQueryStringParameter("key", apiKey)
                    .withQueryStringParameter("city", city),
                VerificationTimes.exactly(1)
            )
    }

    fun registerForecastSuccess(city: String) {
        registerRequest(
            "/forecast/daily",
            city,
            HttpResponse.response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/forecast-tokyo.json"))
        )
    }

    fun registerForecastKeyFailure(city: String) {
        registerRequest(
            "/forecast/daily",
            city,
            HttpResponse.response()
                .withStatusCode(403)
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SampleReader().readSampleAsString("api-samples/forecast-key_failure.json"))
        )
    }

    fun registerForecastCityFailure(city: String) {
        registerRequest(
            "/forecast/daily",
            city,
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
                    .withQueryStringParameter("key", apiKey)
                    .withQueryStringParameter("city", city),
                VerificationTimes.exactly(1)
            )

    }

    fun clearExpectation() {
        mockServerClient.clear(request(), ClearType.ALL)
    }

    private fun registerRequest(path: String, city: String, response: HttpResponse) {
        clientAndServer
            .`when`(
                request()
                    .withMethod("GET")
                    .withPath(path)
                    .withQueryStringParameter("key", apiKey)
                    .withQueryStringParameter("city", city),
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
}
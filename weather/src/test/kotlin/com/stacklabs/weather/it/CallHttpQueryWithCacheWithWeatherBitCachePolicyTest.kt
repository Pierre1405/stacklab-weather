package com.stacklabs.weather.it

import com.github.benmanes.caffeine.cache.Caffeine
import com.stacklabs.weather.cache.CallHttpQueryWithCache
import com.stacklabs.weather.cache.HttpApiCallables
import com.stacklabs.weather.cache.WeatherBitCachePolicy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.function.Function
import kotlin.Result
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import com.stacklabs.weather.cache.Result as CacheResult

class CallHttpQueryWithCacheWithWeatherBitCachePolicyTest {

    val berlin = "Berlin"
    val paris = "Paris"
    val errorCity = "Error City"

    @Test
    fun test_cache_onSuccess() {
        val result: Result<TestOutputDto> = CallHttpQueryWithCache(
            cache = createCache(),
            callables = createCallables { city ->
                ResponseEntity.ok(TestInputDto(city = city))
            }
        ).call(berlin)

        when {
            result.isSuccess -> assertEquals(berlin.uppercase(), result.getOrNull()?.cityUppercase)
            else -> fail("Success expected")
        }
    }

    @Test
    fun test_cache_onFailure() {
        val result: Result<TestOutputDto> = CallHttpQueryWithCache(
            cache = createCache(),
            callables = createCallables { _ ->
                throw HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        ).call(errorCity)

        when (val failure = result.exceptionOrNull()) {
            is HttpClientErrorException -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failure.statusCode)
            else -> fail("HttpClientErrorException expected")
        }
    }

    @Test
    fun test_cache_successAndErrorShouldBeCachedAccordingRateLimitHeader() {
        val mockedCallApi = mock(Function::class.java as Class<Function<String, ResponseEntity<TestInputDto>>>)
        val nowAnd2Seconds = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(2)
        val rateLimitHeaders = rateLimitHeader(nowAnd2Seconds)
        `when`(mockedCallApi.apply(berlin)).thenReturn(
            ResponseEntity.ok().headers(rateLimitHeaders).body(TestInputDto(berlin))
        )
        `when`(mockedCallApi.apply(paris)).thenReturn(
            ResponseEntity.ok().headers(rateLimitHeaders).body(TestInputDto(paris))
        )
        `when`(mockedCallApi.apply(errorCity)).thenThrow(
            HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", rateLimitHeaders, null, null)
        )

        val callApi = CallHttpQueryWithCache(
            cache = createCache(),
            callables = createCallables(mockedCallApi::apply)
        )

        assertTrue(callApi.call(paris).isSuccess)
        verify(mockedCallApi, times(1)).apply(paris)
        assertTrue(callApi.call(paris).isSuccess)
        verify(mockedCallApi, times(1)).apply(paris)
        assertTrue(callApi.call(berlin).isSuccess)
        verify(mockedCallApi, times(1)).apply(berlin)
        assertTrue(callApi.call(berlin).isSuccess)
        verify(mockedCallApi, times(1)).apply(berlin)
        assertTrue(callApi.call(errorCity).isFailure)
        verify(mockedCallApi, times(1)).apply(errorCity)
        assertTrue(callApi.call(errorCity).isFailure)
        verify(mockedCallApi, times(1)).apply(errorCity)


        Thread.sleep(3000)

        assertTrue(callApi.call(paris).isSuccess)
        verify(mockedCallApi, times(2)).apply(paris)
        assertTrue(callApi.call(berlin).isSuccess)
        verify(mockedCallApi, times(2)).apply(berlin)
        assertTrue(callApi.call(errorCity).isFailure)
        verify(mockedCallApi, times(2)).apply(errorCity)

    }


    @Test
    fun test_cache_configMaxCacheLifeDurationShouldOverrideRateLimitHeader() {
        val mockedCallApi = mock(Function::class.java as Class<Function<String, ResponseEntity<TestInputDto>>>)
        val nowAnd2Seconds = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(20)
        val rateLimitHeaders = rateLimitHeader(nowAnd2Seconds)
        `when`(mockedCallApi.apply(berlin)).thenReturn(
            ResponseEntity.ok().headers(rateLimitHeaders).body(TestInputDto(berlin))
        )
        `when`(mockedCallApi.apply(paris)).thenReturn(
            ResponseEntity.ok().headers(rateLimitHeaders).body(TestInputDto(paris))
        )
        `when`(mockedCallApi.apply(errorCity)).thenThrow(
            HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", rateLimitHeaders, null, null)
        )

        val callApi = CallHttpQueryWithCache(
            cache = createCache(),
            callables = createCallables(mockedCallApi::apply)
        )

        assertTrue(callApi.call(paris).isSuccess)
        verify(mockedCallApi, times(1)).apply(paris)
        assertTrue(callApi.call(paris).isSuccess)
        verify(mockedCallApi, times(1)).apply(paris)
        assertTrue(callApi.call(berlin).isSuccess)
        verify(mockedCallApi, times(1)).apply(berlin)
        assertTrue(callApi.call(berlin).isSuccess)
        verify(mockedCallApi, times(1)).apply(berlin)
        assertTrue(callApi.call(errorCity).isFailure)
        verify(mockedCallApi, times(1)).apply(errorCity)
        assertTrue(callApi.call(errorCity).isFailure)
        verify(mockedCallApi, times(1)).apply(errorCity)


        Thread.sleep(6000)

        assertTrue(callApi.call(paris).isSuccess)
        verify(mockedCallApi, times(2)).apply(paris)
        assertTrue(callApi.call(berlin).isSuccess)
        verify(mockedCallApi, times(2)).apply(berlin)
        assertTrue(callApi.call(errorCity).isFailure)
        verify(mockedCallApi, times(2)).apply(errorCity)
    }


    companion object {
        private data class TestInputDto(val city: String)
        private data class TestOutputDto(val cityUppercase: String)

        private fun rateLimitHeader(rateLimitReset: LocalDateTime): HttpHeaders {
            val headers = HttpHeaders()
            headers.add("X-RateLimit-Reset", rateLimitReset.toEpochSecond(ZoneOffset.UTC).toString())
            return headers
        }

        private fun createCache(maxCacheLifeDuration: Duration = 5.toDuration(DurationUnit.SECONDS)) =
            Caffeine.newBuilder()
                .expireAfter(
                    WeatherBitCachePolicy<TestInputDto>(
                        maxCacheLifeDuration = maxCacheLifeDuration,
                    )
                ).build<String, CacheResult<TestInputDto>>()

        private fun createCallables(executeRequest: (String) -> ResponseEntity<TestInputDto>) =
            object : HttpApiCallables<TestInputDto, Result<TestOutputDto>> {
                override fun executeRequest(cacheKey: String): ResponseEntity<TestInputDto> = executeRequest(cacheKey)

                override fun onHttpError(cacheKey: String, exception: HttpClientErrorException) =
                    Result.failure<TestOutputDto>(exception)

                override fun onSuccess(cacheKey: String, response: ResponseEntity<TestInputDto>) =
                    Result.success(
                        TestOutputDto(response.body!!.city.uppercase())
                    )
            }
    }
}
package com.stacklabs.weather.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException


class CallHttpQueryWithCacheTest {

    @Test
    fun test_call_handles_http_client_error() {
        val cache: Cache<String, Result<String>> = Caffeine.newBuilder().build()

        val callables: HttpApiCallables<String, String> =
            mock(HttpApiCallables::class.java as Class<HttpApiCallables<String, String>>)
        val cacheKey = "testKey"
        val exception = HttpClientErrorException(HttpStatus.BAD_REQUEST)
        `when`(callables.executeRequest(cacheKey)).thenThrow(exception)
        `when`(callables.onHttpError(cacheKey, exception)).thenReturn("errorHandled")

        val callHttpQueryWithCache = CallHttpQueryWithCache(cache, callables)
        val result = callHttpQueryWithCache.call(cacheKey)

        assertEquals("errorHandled", result)
        verify(callables).onHttpError(cacheKey, exception)
    }

    @Test
    fun test_call_handles_successful_api_response() {
        val cache: Cache<String, Result<String>> = Caffeine.newBuilder().build()
        val callables: HttpApiCallables<String, String> =
            mock(HttpApiCallables::class.java as Class<HttpApiCallables<String, String>>)
        val cacheKey = "testKey"
        val apiResponse = ResponseEntity.ok("apiData")
        `when`(callables.executeRequest(cacheKey)).thenReturn(apiResponse)
        `when`(callables.onSuccess(cacheKey, apiResponse)).thenReturn("successHandled")

        val callHttpQueryWithCache = CallHttpQueryWithCache(cache, callables)
        val result = callHttpQueryWithCache.call(cacheKey)

        assertEquals("successHandled", result)
        verify(callables).onSuccess(cacheKey, apiResponse)
    }
}
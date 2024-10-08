package com.stacklabs.weather.cache

import com.github.benmanes.caffeine.cache.Cache
import org.springframework.web.client.HttpClientErrorException

/**
 * Represents a function that retrieves data from the cache based on the provided cache key.
 * If the data is not found in the cache, it makes an API call using the provided HttpApiCallables instance.
 * Handles success and error scenarios by calling appropriate methods defined in HttpApiCallables.
 *
 * @param I The type of data to be retrieved from the cache.
 * @param O The type of output returned by the function.
 * @property cache The cache instance used to store and retrieve data.
 * @property callables The HttpApiCallables instance containing methods for executing requests and handling responses.
 */
class CallHttpQueryWithCache<I, O>(
    private val cache: Cache<String, Result<I>>,
    private val callables: HttpApiCallables<I, O>
) {

    fun call(cacheKey: String): O =
        when (
            val cacheResult = cache.get(cacheKey) {
                try {
                    Result.Success(callables.executeRequest(cacheKey))
                } catch (e: HttpClientErrorException) {
                    Result.RestClientError(e)
                }
            }
        ) {
            is Result.RestClientError -> callables.onHttpError(cacheKey, cacheResult.error)
            is Result.Success -> callables.onSuccess(cacheKey, cacheResult.result)
        }

}

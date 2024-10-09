package com.stacklabs.weather.cache

import com.github.benmanes.caffeine.cache.Expiry
import com.stacklabs.weather.cache.Result.RestClientError
import com.stacklabs.weather.cache.Result.Success
import java.util.concurrent.TimeUnit


private const val WEATHER_BIT_EXPIRE_HEADER_NAME = "X-RateLimit-Reset"

/**
 * WeatherBitCachePolicy class implementing Expiry interface for cache expiration control.
 * Calculates the expiration duration based on the rate limit reset timestamp and the current time.
 */
class WeatherBitCachePolicy<T> : Expiry<String, Result<T>> {

    private fun getExpiringCacheDuration(rateLimitResetTimeStamp: Long): Long =
        TimeUnit.MILLISECONDS.toNanos(TimeUnit.SECONDS.toMillis(rateLimitResetTimeStamp) - System.currentTimeMillis())

    override fun expireAfterCreate(
        key: String?,
        cacheResult: Result<T>?,
        currentTime: Long
    ): Long {
        val httpHeaders = when (cacheResult) {
            is Success -> cacheResult.result.headers
            is RestClientError -> cacheResult.error.responseHeaders
            else -> null
        }
        val rateLimitResetTimeStamp = httpHeaders?.getFirst(WEATHER_BIT_EXPIRE_HEADER_NAME)?.toLong()
        return if (rateLimitResetTimeStamp != null) {
            getExpiringCacheDuration(rateLimitResetTimeStamp)
        } else {
            0
        }
    }

    override fun expireAfterUpdate(
        key: String?,
        cacheResult: Result<T>?,
        currentTime: Long,
        currentDuration: Long
    ): Long {
        return currentDuration
    }

    override fun expireAfterRead(
        key: String?,
        cacheResult: Result<T>?,
        currentTime: Long,
        currentDuration: Long
    ): Long {
        return currentDuration
    }
}
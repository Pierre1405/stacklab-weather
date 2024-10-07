package com.stacklabs.weather.repository

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import org.springframework.http.ResponseEntity
import java.util.concurrent.TimeUnit


private const val WEATHER_BIT_EXPIRE_HEADER_NAME = "X-RateLimit-Reset"

class WeatherBitRepositoryCacheBuilder<T> {

    fun create(): Cache<String, ResponseEntity<T>> = Caffeine.newBuilder()
        .expireAfter(WeatherBitRepositoryCacheBuilderPolicy<T>())
        .build()

    companion object {
        /**
         * Cache expiration policy for WeatherBitRepository.
         * It set the cache duration based on the response header X-RateLimit-Reset.
         * X-RateLimit-Reset = second timestamp when the weatherbit rate limit resets
         * @param <T> type of the body response
         */
        private class WeatherBitRepositoryCacheBuilderPolicy<T> : Expiry<String, ResponseEntity<T>> {
            fun getExpiringCacheDuration(rateLimitResetTimeStamp: Long): Long =
                TimeUnit.MILLISECONDS.toNanos(TimeUnit.SECONDS.toMillis(rateLimitResetTimeStamp) - System.currentTimeMillis())

            override fun expireAfterCreate(
                key: String?,
                weatherBitResponse: ResponseEntity<T>?,
                currentTime: Long
            ): Long {
                val rateLimitResetTimeStamp =
                    weatherBitResponse?.headers?.getFirst(WEATHER_BIT_EXPIRE_HEADER_NAME)?.toLong()
                return if (rateLimitResetTimeStamp != null) {
                    getExpiringCacheDuration(rateLimitResetTimeStamp)
                } else {
                    0
                }
            }

            override fun expireAfterUpdate(
                key: String?,
                wweatherBitResponse: ResponseEntity<T>?,
                currentTime: Long,
                currentDuration: Long
            ): Long {
                return currentDuration
            }

            override fun expireAfterRead(
                key: String?,
                wweatherBitResponse: ResponseEntity<T>?,
                currentTime: Long,
                currentDuration: Long
            ): Long {
                return currentDuration
            }
        }
    }
}


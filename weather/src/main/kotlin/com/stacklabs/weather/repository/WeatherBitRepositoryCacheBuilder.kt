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

        private class WeatherBitRepositoryCacheBuilderPolicy<T> : Expiry<String, ResponseEntity<T>> {
            override fun expireAfterCreate(
                key: String?,
                weatherBitResponse: ResponseEntity<T>?,
                currentTime: Long
            ): Long {
                val maybeResetAt = weatherBitResponse?.headers?.getFirst(WEATHER_BIT_EXPIRE_HEADER_NAME)?.toLong()
                return if (maybeResetAt != null) {
                    TimeUnit.MILLISECONDS.toNanos(TimeUnit.SECONDS.toMillis(maybeResetAt) - System.currentTimeMillis())
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


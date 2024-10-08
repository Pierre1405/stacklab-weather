package com.stacklabs.weather.repository

sealed class WeatherRepositoryResult<T> {
    class Success<T>(val data: T) : WeatherRepositoryResult<T>()
    class CityNotFound<T>(val city: String) : WeatherRepositoryResult<T>()
    class Error<T>(val message: String?, val cause: Throwable? = null) : WeatherRepositoryResult<T>()

}
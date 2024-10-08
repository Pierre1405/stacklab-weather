package com.stacklabs.weather.service

sealed class WeatherServiceResult<T> {
    class Success<T>(val data: T) : WeatherServiceResult<T>()
    class CityNotFound<T>(val city: String) : WeatherServiceResult<T>()
    class Error<T>(val message: String?, val cause: Throwable? = null) : WeatherServiceResult<T>()
}
package com.stacklabs.weather.repository

open class WeatherBitRepositoryException(message: String, override val cause: Throwable? = null) :
    RuntimeException(message, cause)

class CityNotFoundWeatherBitRepositoryException(city: String) :
    WeatherBitRepositoryException(message = "City $city not found")


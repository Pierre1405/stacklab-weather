package com.stacklabs.weather.service

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

open class WeatherServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
@ResponseStatus(value = HttpStatus.NO_CONTENT)
class CityNotWeatherServiceException(city: String) : WeatherServiceException(message = "City $city not found")
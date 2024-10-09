package com.stacklabs.weather.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
open class WeatherControllerException(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)
@ResponseStatus(value = HttpStatus.NO_CONTENT)
class CityNotFound(city: String) : WeatherControllerException(message = "City $city not found")
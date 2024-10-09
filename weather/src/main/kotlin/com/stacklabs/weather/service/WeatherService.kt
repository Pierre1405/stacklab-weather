package com.stacklabs.weather.service

import org.stacklabs.weather.dto.CurrentWeatherDto
import org.stacklabs.weather.dto.WeatherForecastDto

interface WeatherService {
    fun getCurrentWeather(city: String): WeatherServiceResult<CurrentWeatherDto>
    fun getWeatherForecast(city: String): WeatherServiceResult<WeatherForecastDto>
}
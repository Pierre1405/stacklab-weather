package com.stacklabs.weather.service

import org.stacklabs.weather.dto.CurrentWeatherDto
import org.stacklabs.weather.dto.WeatherForecastDto

interface WeatherService {
    fun getCurrentWeather(city: String): CurrentWeatherDto
    fun getWeatherForecast(city: String): WeatherForecastDto
}
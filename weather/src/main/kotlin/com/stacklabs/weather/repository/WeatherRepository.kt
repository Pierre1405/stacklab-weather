package com.stacklabs.weather.repository

import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity

interface WeatherRepository {
    fun getCurrentWeatherByCity(city: String): CurrentWeatherEntity
    fun getWeatherForecastByCity(city: String): WeatherForecastsEntity
}
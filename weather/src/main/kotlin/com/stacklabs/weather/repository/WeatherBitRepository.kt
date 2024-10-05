package com.stacklabs.weather.repository

import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.web.client.RestClient

class WeatherBitRepository(private val restClient: RestClient, private val apiKey: String, private val forecastNbDays: Int) {
    fun getCurrentWeatherByCity (city: String): CurrentObsGroup {
        return CurrentWeatherDataApi(restClient).currentGet(
            key = apiKey,
            city = city
        )
    }

    fun getWeatherForecastByCity(city: String): ForecastDay {
        return Class16DayDailyForecastApi(restClient).forecastDailyGet(
            key = apiKey,
            city = city,
            days = forecastNbDays.toBigDecimal()
        )
    }

}
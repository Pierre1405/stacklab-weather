package com.stacklabs.weather.repository

import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import org.springframework.web.client.RestClient
import com.stacklabs.weather.configuration.WeatherBitProperties

import java.time.LocalDate

class WeatherBitRepository(
    private val configuration: WeatherBitProperties,
    private val restClient: RestClient,
    private val currentWeatherDataApi: CurrentWeatherDataApi = CurrentWeatherDataApi(restClient),
    private val weatherForecastDataApi: Class16DayDailyForecastApi = Class16DayDailyForecastApi(restClient)
): WeatherRepository {

    override fun getCurrentWeatherByCity (city: String): CurrentWeatherEntity {
        val currentWeather = currentWeatherDataApi.currentGet(
            key = configuration.apiKey,
            city = city
        )
        val currentObs = currentWeather.data?.let {
            when (it.size) {
                0 -> throw WeatherBitRepositoryException("Not able to retrieve current weather, empty data")
                1 -> it.first()
                else -> throw WeatherBitRepositoryException("Not able to retrieve current weather, more than one data found")
            }
        }
            ?: throw WeatherBitRepositoryException("Not able to retrieve current weather, no data found")

        return CurrentWeatherEntity(
            description = currentObs.weather?.description,
            temperature = currentObs.temp?.toDouble(),
            humidity = currentObs.rh,
            windSpeed = currentObs.windSpd?.toDouble()
        )
    }

    override fun getWeatherForecastByCity(city: String): WeatherForecastsEntity {
        val weatherForecast = weatherForecastDataApi.forecastDailyGet(
            key = configuration.apiKey,
            city = city,
            days = configuration.forecastNbDays.toBigDecimal()
        )
        val weatherForecastData = weatherForecast.data
            ?: throw WeatherBitRepositoryException("Not able to retrieve weather forecast, no data found")
        return WeatherForecastsEntity(
            data = weatherForecastData.map {
                WeatherForecastEntity(
                    datetime = LocalDate.parse(it.datetime),
                    temperature = it.temp?.toDouble(),
                    pressure = it.pres?.toDouble(),
                    windSpeed = it.windSpd?.toDouble()
                )
            }
        )
    }

}
package com.stacklabs.weather.repository

import com.github.benmanes.caffeine.cache.Cache
import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import java.time.LocalDate

class WeatherBitRepository(
    private val configuration: WeatherBitProperties,
    private val restClient: RestClient,
    private val currentWeatherDataApi: CurrentWeatherDataApi = CurrentWeatherDataApi(restClient),
    private val weatherForecastDataApi: Class16DayDailyForecastApi = Class16DayDailyForecastApi(restClient)
) : WeatherRepository {

    private val currentCache: Cache<String, ResponseEntity<CurrentObsGroup>> =
        WeatherBitRepositoryCacheBuilder<CurrentObsGroup>().create()
    private val forecastCache: Cache<String, ResponseEntity<ForecastDay>> =
        WeatherBitRepositoryCacheBuilder<ForecastDay>().create()

    override fun getCurrentWeatherByCity(city: String): CurrentWeatherEntity {
        val currentWeather: ResponseEntity<CurrentObsGroup> = currentCache.get(city) { _city ->
            currentWeatherDataApi.currentGetWithHttpInfo(
                key = configuration.apiKey,
                city = _city
            )
        }
        when {
            !currentWeather.statusCode.is2xxSuccessful -> throw WeatherBitRepositoryException("Not able to retrieve current weather, weatherbit error ${currentWeather.statusCode}")
            else -> {
                val currentObs = currentWeather.body?.data?.let {
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
        }
    }

    override fun getWeatherForecastByCity(city: String): WeatherForecastsEntity {
        val weatherForecast = forecastCache.get(city) { _city ->
            weatherForecastDataApi.forecastDailyGetWithHttpInfo(
                key = configuration.apiKey,
                city = _city,
                days = configuration.forecastNbDays.toBigDecimal()
            )
        }
        when {
            !weatherForecast.statusCode.is2xxSuccessful -> throw WeatherBitRepositoryException("Not able to retrieve weather forecast, weatherbit error ${weatherForecast.statusCode}")
            else -> {
                val weatherForecastData = weatherForecast.body?.data
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
    }
}
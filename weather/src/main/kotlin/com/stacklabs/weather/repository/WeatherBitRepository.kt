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
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import java.time.LocalDate

class WeatherBitRepository(
    private val configuration: WeatherBitProperties,
    private val restClient: RestClient,
    private val currentWeatherDataApi: CurrentWeatherDataApi = CurrentWeatherDataApi(restClient),
    private val weatherForecastDataApi: Class16DayDailyForecastApi = Class16DayDailyForecastApi(restClient)
) : WeatherRepository {

    private val logger = LoggerFactory.getLogger(WeatherBitRepository::class.java)
    private val currentCache: Cache<String, ResponseEntity<CurrentObsGroup>> =
        WeatherBitRepositoryCacheBuilder<CurrentObsGroup>().create()
    private val forecastCache: Cache<String, ResponseEntity<ForecastDay>> =
        WeatherBitRepositoryCacheBuilder<ForecastDay>().create()


    override fun getCurrentWeatherByCity(city: String): CurrentWeatherEntity {
        val currentWeather: ResponseEntity<CurrentObsGroup> = try {
            currentCache.get(city) { cacheKey ->
                currentWeatherDataApi.currentGetWithHttpInfo(
                    key = configuration.apiKey,
                    city = cacheKey
                )
            }
        } catch (e: HttpClientErrorException) {
            logger.debug("HttpClient error", e)
            val responseBody: String = e.responseBodyAsString
            if (e.statusCode == HttpStatus.BAD_REQUEST && responseBody.contains("No Location Found")) {
                throw CityNotFoundWeatherBitRepositoryException(city)
            }
            throw WeatherBitRepositoryException("Not able to retrieve current weather, weatherbit error ${e.statusCode}", e)
        }

        // Process response
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

    override fun getWeatherForecastByCity(city: String): WeatherForecastsEntity {
        val weatherForecast = try {
            forecastCache.get(city) { cacheKey ->
                weatherForecastDataApi.forecastDailyGetWithHttpInfo(
                    key = configuration.apiKey,
                    city = cacheKey,
                    days = configuration.forecastNbDays.toBigDecimal()
                )
            }
        } catch (e: HttpClientErrorException) {
            logger.debug("HttpClient error", e)
            throw WeatherBitRepositoryException("Not able to retrieve weather forecast, weatherbit error ${e.statusCode}", e)
        }

        // Process response
        if (weatherForecast.statusCode == HttpStatus.NO_CONTENT && weatherForecast.body == null) {
            throw CityNotFoundWeatherBitRepositoryException(city)
        }
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
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


    override fun getCurrentWeatherByCity(city: String): WeatherRepositoryResult<CurrentWeatherEntity> {
        logger.debug("Retrieving current weather for city {}", city)
        val currentWeather: ResponseEntity<CurrentObsGroup> = try {
            currentCache.get(city) { cacheKey ->
                currentWeatherDataApi.currentGetWithHttpInfo(
                    key = configuration.apiKey,
                    city = cacheKey
                )
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Exception occurred while retrieving current weather for city: $city", e)
            val isCityNotFound = e.statusCode == HttpStatus.BAD_REQUEST && e.responseBodyAsString.contains("No Location Found")
            when {
                (isCityNotFound) -> return WeatherRepositoryResult.CityNotFound(city)
                else -> return WeatherRepositoryResult.Error(e.message, e)
            }
        }

        when {
            (currentWeather.body?.data == null) -> return WeatherRepositoryResult.Error("Response body or body.data null")
            (currentWeather.body?.data?.size != 1) -> return WeatherRepositoryResult.Error("Data.size should be 1")
        }

        val currentObs = currentWeather.body!!.data!!.first()
        return WeatherRepositoryResult.Success(
            CurrentWeatherEntity(
                description = currentObs.weather?.description,
                temperature = currentObs.temp?.toDouble(),
                humidity = currentObs.rh,
                windSpeed = currentObs.windSpd?.toDouble()
            )
        )
    }

    override fun getWeatherForecastByCity(city: String): WeatherRepositoryResult<WeatherForecastsEntity> {
        logger.debug("Retrieving weather forecast for city {}", city)
        val weatherForecast = try {
            forecastCache.get(city) { cacheKey ->
                weatherForecastDataApi.forecastDailyGetWithHttpInfo(
                    key = configuration.apiKey,
                    city = cacheKey,
                    days = configuration.forecastNbDays.toBigDecimal()
                )
            }
        } catch (e: HttpClientErrorException) {
            logger.error("Exception occurred while retrieving current weather for city: $city", e)
            return WeatherRepositoryResult.Error(e.message, e)
        }

        val isCityNotFound = weatherForecast.statusCode == HttpStatus.NO_CONTENT && weatherForecast.body == null
        when {
            isCityNotFound -> return WeatherRepositoryResult.CityNotFound(city)
            (weatherForecast.body?.data == null) -> return WeatherRepositoryResult.Error("Response body or body.data null")
        }

        return WeatherRepositoryResult.Success(
            WeatherForecastsEntity(
                data = weatherForecast.body!!.data!!.map { forecast ->
                    WeatherForecastEntity(
                        datetime = LocalDate.parse(forecast.datetime),
                        temperature = forecast.temp?.toDouble(),
                        pressure = forecast.pres?.toDouble(),
                        windSpeed = forecast.windSpd?.toDouble()
                    )
                }
            )
        )
    }
}
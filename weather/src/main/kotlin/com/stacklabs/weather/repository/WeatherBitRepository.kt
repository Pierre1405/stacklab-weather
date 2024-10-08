package com.stacklabs.weather.repository

import com.github.benmanes.caffeine.cache.Cache
import com.stacklabs.weather.cache.Result
import com.stacklabs.weather.cache.CallHttpQueryWithCache
import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.web.client.RestClient

class WeatherBitRepository(
    configuration: WeatherBitProperties,
    restClient: RestClient,
    currentApiCache: Cache<String, Result<CurrentObsGroup>>,
    forecastCache: Cache<String, Result<ForecastDay>>,
    currentWeatherDataApi: CurrentWeatherDataApi = CurrentWeatherDataApi(restClient),
    weatherForecastDataApi: Class16DayDailyForecastApi = Class16DayDailyForecastApi(restClient)
) : WeatherRepository {

    private val callCurrentWeatherApiWithCache = CallHttpQueryWithCache(
        cache = currentApiCache,
        callables = CurrentWeatherCallables(configuration, currentWeatherDataApi)
    )

    private val callWeatherForecastApiWithCache = CallHttpQueryWithCache(
        cache = forecastCache,
        callables = WeatherForecastCallables(configuration, weatherForecastDataApi)
    )

    override fun getCurrentWeatherByCity(city: String): WeatherRepositoryResult<CurrentWeatherEntity> =
        callCurrentWeatherApiWithCache.call(city)


    override fun getWeatherForecastByCity(city: String): WeatherRepositoryResult<WeatherForecastsEntity> =
        callWeatherForecastApiWithCache.call(city)
}
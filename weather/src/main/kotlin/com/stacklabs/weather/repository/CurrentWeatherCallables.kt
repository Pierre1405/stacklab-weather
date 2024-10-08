package com.stacklabs.weather.repository

import com.stacklabs.weather.cache.HttpApiCallables
import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.entity.CurrentWeatherEntity
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException

class CurrentWeatherCallables(
    private val configuration: WeatherBitProperties,
    private val currentWeatherDataApi: CurrentWeatherDataApi,
) : HttpApiCallables<CurrentObsGroup, WeatherRepositoryResult<CurrentWeatherEntity>> {

    override fun executeRequest(cacheKey: String): ResponseEntity<CurrentObsGroup> =
        currentWeatherDataApi.currentGetWithHttpInfo(
            key = configuration.apiKey,
            city = cacheKey
        )

    override fun onHttpError(
        cacheKey: String,
        exception: HttpClientErrorException
    ): WeatherRepositoryResult<CurrentWeatherEntity> {
        val isCityNotFound =
            exception.statusCode == HttpStatus.BAD_REQUEST && exception.responseBodyAsString.contains("No Location Found")
        return when {
            (isCityNotFound) -> WeatherRepositoryResult.CityNotFound(cacheKey)
            else -> WeatherRepositoryResult.Error(exception.message, exception)
        }
    }

    override fun onSuccess(
        cacheKey: String,
        response: ResponseEntity<CurrentObsGroup>
    ): WeatherRepositoryResult<CurrentWeatherEntity> = when {
        (response.body?.data == null) -> WeatherRepositoryResult.Error("Response body or body.data null")
        (response.body?.data?.size != 1) -> WeatherRepositoryResult.Error("Data.size should be 1")
        else -> {
            val currentObs = response.body!!.data!!.first()
            WeatherRepositoryResult.Success(
                CurrentWeatherEntity(
                    description = currentObs.weather?.description,
                    temperature = currentObs.temp?.toDouble(),
                    humidity = currentObs.rh,
                    windSpeed = currentObs.windSpd?.toDouble()
                )
            )
        }
    }
}

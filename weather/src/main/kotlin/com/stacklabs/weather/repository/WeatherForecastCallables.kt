package com.stacklabs.weather.repository

import com.stacklabs.weather.cache.HttpApiCallables
import com.stacklabs.weather.configuration.WeatherBitProperties
import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.entity.WeatherForecastsEntity
import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate

class WeatherForecastCallables(
    private val configuration: WeatherBitProperties,
    private val weatherForecastDataApi: Class16DayDailyForecastApi,
) : HttpApiCallables<ForecastDay, WeatherRepositoryResult<WeatherForecastsEntity>> {

    override fun executeRequest(cacheKey: String): ResponseEntity<ForecastDay> =
        weatherForecastDataApi.forecastDailyGetWithHttpInfo(
            key = configuration.apiKey,
            city = cacheKey,
            days = configuration.forecastNbDays.toBigDecimal()
        )

    override fun onHttpError(
        cacheKey: String,
        exception: HttpClientErrorException
    ): WeatherRepositoryResult<WeatherForecastsEntity> =
        WeatherRepositoryResult.Error(exception.message, exception)

    override fun onSuccess(
        cacheKey: String,
        response: ResponseEntity<ForecastDay>
    ): WeatherRepositoryResult<WeatherForecastsEntity> {
        val isCityNotFound = response.statusCode == HttpStatus.NO_CONTENT && response.body == null
        when {
            isCityNotFound -> return WeatherRepositoryResult.CityNotFound(cacheKey)
            (response.body?.data == null) -> return WeatherRepositoryResult.Error("Response body or body.data null")
        }

        return WeatherRepositoryResult.Success(
            WeatherForecastsEntity(
                data = response.body!!.data!!.map { forecast ->
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
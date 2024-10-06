package com.stacklabs.weather.service

import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.repository.WeatherRepository
import com.stacklabs.weather.service.evaluation.ForecastEvaluation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stacklabs.weather.dto.BeaufortScale
import org.stacklabs.weather.dto.CurrentWeatherDto
import org.stacklabs.weather.dto.Tendency
import org.stacklabs.weather.dto.WeatherForecastDto

@Service
class WeatherbitWeatherService @Autowired constructor(
    private val repository: WeatherRepository,
    private val forecastEvaluation: ForecastEvaluation
) : WeatherService {

    override fun getCurrentWeather(city: String): CurrentWeatherDto =
        repository.getCurrentWeatherByCity(city).let {
            CurrentWeatherDto(
                description = it.description,
                temperature = it.temperature,
                humidity = it.humidity,
                windSpeed = it.windSpeed
            )
        }

    override fun getWeatherForecast(city: String): WeatherForecastDto {
        val weatherForecasts = repository.getWeatherForecastByCity(city).data
        val referenceDay = weatherForecasts.minBy { it.datetime }

        val sum = weatherForecasts.fold(WeatherForecastEntity(0.0, 0.0, 0.0)) { acc, forecast ->
            WeatherForecastEntity(
                temperature = acc.temperature!! + (forecast.temperature
                    ?: throw WeatherServiceException("Not able to retrieve weather forecast, a temperature is null")),
                pressure = acc.pressure!! + (forecast.pressure
                    ?: throw WeatherServiceException("Not able to retrieve weather forecast, a pressure is null")),
                windSpeed = acc.windSpeed!! + (forecast.windSpeed
                    ?: throw WeatherServiceException("Not able to retrieve weather forecast, a wind speed is null"))
            )
        }

        // lot of !!, but, it should never happen due to the exceptions throws on null values in the previous fold
        val nbDays = weatherForecasts.size
        val daysAverage = WeatherForecastEntity(
            temperature = sum.temperature!! / nbDays,
            pressure = sum.pressure!! / nbDays,
            windSpeed = sum.windSpeed!! / nbDays
        )

        return WeatherForecastDto(
            globalTendency = Tendency.get(
                forecastEvaluation.evaluate(referenceDay),
                forecastEvaluation.evaluate(daysAverage)
            ),
            temperatureTendency = Tendency.get(
                referenceDay.temperature!!,
                daysAverage.temperature!!
            ),
            pressureTendency = Tendency.get(
                referenceDay.pressure!!,
                daysAverage.pressure!!
            ),
            windAverage = BeaufortScale.getFromMeterPerSeconds(daysAverage.windSpeed!!)
        )
    }


}
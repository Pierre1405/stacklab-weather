package com.stacklabs.weather.service

import com.stacklabs.weather.entity.WeatherForecastEntity
import com.stacklabs.weather.repository.WeatherRepository
import com.stacklabs.weather.service.evaluation.ForecastEvaluation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.stacklabs.weather.dto.*

@Service
class WeatherbitWeatherService @Autowired constructor(
    private val repository: WeatherRepository,
    private val forecastEvaluation: ForecastEvaluation,
    @Value(value = "\${evaluation.pressure.big-fall-delta}")
    private val pressureBigFallDelta: Double
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

    private fun throwIfFieldNull(entity: WeatherForecastEntity) {
        throwIfValueNull(entity.temperature, "temperature")
        throwIfValueNull(entity.pressure, "pressure")
        throwIfValueNull(entity.windSpeed, "windSpeed")
    }

    private fun throwIfValueNull(value: Double?, fieldName: String) =
        value ?: throw WeatherServiceException("Not able to retrieve weather forecast, a $fieldName is null")

    override fun getWeatherForecast(city: String): WeatherForecastDto {
        val weatherForecasts = repository.getWeatherForecastByCity(city).data
        weatherForecasts.forEach(::throwIfFieldNull)

        val today: WeatherForecastEntity = weatherForecasts.minBy { it.datetime }

        val otherDays = weatherForecasts
            // we must remove the reference day in order to avoid error on big tendency
            .filterNot { it == today }
        val otherDaysSum = otherDays
            .fold(WeatherForecastEntity(0.0, 0.0, 0.0)) { acc, forecast ->
                WeatherForecastEntity(
                    temperature = acc.temperature!! + forecast.temperature!!,
                    pressure = acc.pressure!! + forecast.pressure!!,
                    windSpeed = acc.windSpeed!! + forecast.windSpeed!!,
                )
            }

        // lot of !!, but, it should never happen due to the exceptions throws on null values in the previous fold
        val nbOtherDays = otherDays.size
        val otherDaysAverage = WeatherForecastEntity(
            temperature = otherDaysSum.temperature!! / nbOtherDays,
            pressure = otherDaysSum.pressure!! / nbOtherDays,
            windSpeed = otherDaysSum.windSpeed!! / nbOtherDays
        )

        return WeatherForecastDto(
            globalTendency = Tendency.get(
                forecastEvaluation.evaluate(today),
                forecastEvaluation.evaluate(otherDaysAverage)
            ),
            temperatureTendency = Tendency.get(
                today.temperature!!,
                otherDaysAverage.temperature!!
            ),
            pressureTendency = BigTendency.get(
                today.pressure!!,
                otherDaysAverage.pressure!!,
                // Check big pressure fall on 7 days doesn't really make sense
                // Normally we should check if the pressure fall during the last 4 hours
                pressureBigFallDelta
            ),
            windAverage = BeaufortScale.getFromMeterPerSeconds(otherDaysAverage.windSpeed!!)
        )
    }


}
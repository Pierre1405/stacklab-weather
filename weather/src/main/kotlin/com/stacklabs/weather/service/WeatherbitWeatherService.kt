package com.stacklabs.weather.service

import com.stacklabs.weather.configuration.EvaluationProperties
import com.stacklabs.weather.service.WeatherEvaluation.evaluate
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.Forecast
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stacklabs.weather.dto.BeaufortScale
import org.stacklabs.weather.dto.CurrentWeatherDto
import org.stacklabs.weather.dto.Tendency
import org.stacklabs.weather.dto.WeatherForecastDto
import java.time.LocalDate

@Service
class WeatherbitWeatherService @Autowired constructor(
    private val getWeatherbitCurrentWeather: (String) -> CurrentObsGroup,
    private val getWeatherbitWeatherForecast: (String) -> ForecastDay,
    private val temperatureEvaluation: EvaluationProperties,
    private val pressureEvaluation: EvaluationProperties
) : WeatherService {

    override fun getCurrentWeather(city: String): CurrentWeatherDto {
        val currentWeather = getWeatherbitCurrentWeather(city)
        val currentObs = currentWeather.data?.let {
            when (it.size) {
                0 -> throw RuntimeException("Not able to retrieve current weather, empty data")
                1 -> it.first()
                else -> throw RuntimeException("Not able to retrieve current weather, more than one data found")
            }
        }
            ?: throw WeatherServiceException("Not able to retrieve current weather, no data found")

        return CurrentWeatherDto(
            description = currentObs.weather?.description,
            temperature = currentObs.temp?.toDouble(),
            humidity = currentObs.rh,
            windSpeed = currentObs.windSpd?.toDouble()
        )
    }

    private data class ForecastAcc(
        val global: Double = 0.0,
        val temperature: Double = 0.0,
        val pressure: Double = 0.0,
        val windSpeed: Double = 0.0
    ) {
        fun average(nbDays: Int) = copy(
            global = global / nbDays,
            temperature = temperature / nbDays,
            pressure = pressure / nbDays,
            windSpeed = windSpeed / nbDays
        )
    }

    override fun getWeatherForecast(city: String): WeatherForecastDto {
        val weatherForecast = getWeatherbitWeatherForecast(city)
        val weatherForecastData = weatherForecast.data
            ?: throw WeatherServiceException("Not able to retrieve weather forecast, no data found")

        val referenceDay = weatherForecastData.minBy { LocalDate.parse(it.datetime) }

        val acc = weatherForecastData.fold(ForecastAcc()) { acc, forecast ->
            ForecastAcc(
                global = acc.global + calculateEvaluationScore(forecast),
                temperature = acc.temperature + (forecast.temp?.toDouble()
                    ?: throw WeatherServiceException("Not able to retrieve weather forecast, a temperature is missing")),
                pressure = acc.pressure + (forecast.pres?.toDouble()
                    ?: throw WeatherServiceException("Not able to retrieve weather forecast, a pressure is missing")),
                windSpeed = acc.windSpeed + (forecast.windSpd?.toDouble()
                    ?: throw WeatherServiceException("Not able to retrieve weather forecast, a wind speed is missing"))
            )
        }

        val otherDayAverage = acc.average(weatherForecastData.size)

        return WeatherForecastDto(
            globalTendency = Tendency.get(calculateEvaluationScore(referenceDay), otherDayAverage.global),
            temperatureTendency = Tendency.get(
                referenceDay.temp?.toDouble()
                    ?: throw WeatherServiceException("Not able to evaluate temperature tendency, reference temperature is missing"),
                otherDayAverage.temperature
            ),
            pressureTendency = Tendency.get(
                referenceDay.pres?.toDouble()
                    ?: throw WeatherServiceException("Not able to evaluate pressure tendency, reference pressure is missing"),
                otherDayAverage.pressure
            ),
            windAverage = BeaufortScale.getFromMeterPerSeconds(otherDayAverage.windSpeed)
        )
    }

    private fun calculateEvaluationScore(forecast: Forecast): Double {
        return evaluate(
            forecast.temp?.toDouble() ?: throw WeatherServiceException("Not able to evaluate global tendency"),
            temperatureEvaluation
        ) + evaluate(
            forecast.pres?.toDouble() ?: throw WeatherServiceException("Not able to evaluate global tendency"),
            pressureEvaluation
        )
    }
}
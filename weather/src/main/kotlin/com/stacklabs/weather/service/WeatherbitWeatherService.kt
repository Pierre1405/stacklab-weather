package com.stacklabs.weather.service

import com.stacklabs.weather.weatherbit.models.CurrentObs
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.stacklabs.weather.dto.CurrentWeatherDto
import org.stacklabs.weather.dto.WeatherForecastDto

@Service
class WeatherbitWeatherService @Autowired constructor(
    private val getCurrent: (String) -> CurrentObsGroup,
    private val getWeatherForecast: (String) -> ForecastDay
) : WeatherService {
    val log = LoggerFactory.getLogger(WeatherbitWeatherService::class.java)

    override fun getCurrentWeather(city: String): CurrentWeatherDto {
        // check api error
        val currentWeather: CurrentObsGroup = try {
            getCurrent(city)
        } catch (e: Throwable) {
            log.error("Error retrieving current weather data for city: `$city`", e)
            throw RuntimeException("Not able to retrieve current weather", e)
        }
        // check data not null
        val currentObsList = currentWeather.data ?: throw RuntimeException("Not able to retrieve current weather, no data found")
        // check data contains a single element
        val currentObs: CurrentObs = checkSingleElement(currentObsList)

        return CurrentWeatherDto(
            description = currentObs.weather?.description ?: "",
            temperature = currentObs.temp?.toFloat() ?: Float.MIN_VALUE,
            humidity = currentObs.rh ?: Int.MIN_VALUE,
            windSpeed = currentObs.windSpd?.toFloat() ?: Float.MIN_VALUE
        )
    }

    private fun checkSingleElement(currentObsList: List<CurrentObs>): CurrentObs {
        return when(currentObsList.size) {
            0 -> throw RuntimeException("Not able to retrieve current weather, no data found")
            1 -> currentObsList.first()
            else -> throw RuntimeException("Not able to retrieve current weather, multiple data found")
        }
    }

    override fun getWeatherForecast(city: String): WeatherForecastDto {

        TODO("Not yet implemented")
        // get the first day as reference

        // for all the days, evaluate the weather
        // aggregate the sum of results
        // calculate the average
        // compare with the reference day
    }

}
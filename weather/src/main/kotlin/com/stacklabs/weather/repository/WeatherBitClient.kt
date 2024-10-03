package com.stacklabs.weather.repository

import com.stacklabs.weather.weatherbit.apis.Class16DayDailyForecastApi
import com.stacklabs.weather.weatherbit.apis.CurrentWeatherDataApi
import com.stacklabs.weather.weatherbit.models.CurrentObsGroup
import com.stacklabs.weather.weatherbit.models.ForecastDay
import org.springframework.web.client.RestClient

class WeatherBitClient {

    companion object {
        fun currentWeather(key: String, restClient: RestClient): (String) -> CurrentObsGroup {
            return { city ->
                CurrentWeatherDataApi(restClient).currentGet(
                    key = key,
                    city = city
                )
            }
        }

        fun weatherForecast(key: String, restClient: RestClient): (String) -> ForecastDay {
            return { city ->
                Class16DayDailyForecastApi(restClient).forecastDailyGet(
                    key = key,
                    city = city
                )
            }
        }
    }

}
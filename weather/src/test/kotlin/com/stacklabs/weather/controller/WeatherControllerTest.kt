package com.stacklabs.weather.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.stacklabs.weather.service.WeatherService
import com.stacklabs.weather.service.WeatherServiceResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.stacklabs.weather.dto.*

@WebMvcTest(WeatherController::class)
class WeatherControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var weatherService: WeatherService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun test_currentWeather_valid_city() {
        val city = "Tokyo"
        val expectedDto = CurrentWeatherDto("Cloudy", 21.0, 14.7, 75)

        Mockito.`when`(weatherService.getCurrentWeather(city)).thenReturn(
            WeatherServiceResult.Success(expectedDto)
        )

        val result = mockMvc.perform(get("/weather/current").param("city", city))
            .andExpect(status().isOk)
            .andReturn()

        val actualDto = objectMapper.readValue(result.response.contentAsString, CurrentWeatherDto::class.java)
        assertEquals(expectedDto, actualDto)
    }

    @Test
    fun test_currentWeather_notFoundCity() {
        val city = "Tokyo"
        Mockito.`when`(weatherService.getCurrentWeather(city)).thenReturn(
            WeatherServiceResult.CityNotFound(city)
        )

        mockMvc.perform(get("/weather/current").param("city", city))
            .andExpect(status().isNoContent)
    }

    @Test
    fun test_currentWeather_error() {
        val city = "Tokyo"
        Mockito.`when`(weatherService.getCurrentWeather(city)).thenReturn(
            WeatherServiceResult.Error("An error occurs")
        )

        mockMvc.perform(get("/weather/current").param("city", city))
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun test_forecast_valid_city() {
        val city = "Tokyo"
        val expectedDto =
            WeatherForecastDto(
                Tendency.INCREASING,
                Tendency.INCREASING,
                BigTendency.INCREASING,
                BeaufortScale.LIGHT_AIR
            )

        Mockito.`when`(weatherService.getWeatherForecast(city)).thenReturn(
            WeatherServiceResult.Success(expectedDto)
        )

        val result = mockMvc.perform(get("/weather/forecast").param("city", city))
            .andExpect(status().isOk)
            .andReturn()

        val actualDto = objectMapper.readValue(result.response.contentAsString, WeatherForecastDto::class.java)
        assertEquals(expectedDto, actualDto)
    }

    @Test
    fun test_weatherForecast_notFoundCity() {
        val city = "Tokyo"
        Mockito.`when`(weatherService.getWeatherForecast(city)).thenReturn(
            WeatherServiceResult.CityNotFound(city)
        )

        mockMvc.perform(get("/weather/forecast").param("city", city))
            .andExpect(status().isNoContent)
    }

    @Test
    fun test_weatherForecast_weather_error() {
        val city = "Tokyo"
        Mockito.`when`(weatherService.getWeatherForecast(city)).thenReturn(
            WeatherServiceResult.Error("An error occurs")
        )

        mockMvc.perform(get("/weather/forecast").param("city", city))
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun test_invalid_city_name_handling() {
        val invalidCity = ""

        mockMvc.perform(get("/weather/current").param("city", invalidCity))
            .andExpect(status().isBadRequest)

        mockMvc.perform(get("/weather/forecast").param("city", invalidCity))
            .andExpect(status().isBadRequest)
    }
}
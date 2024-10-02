package com.stacklabs.weather.controller

import com.stacklabs.weather.service.WeatherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.stacklabs.weather.dto.CurrentWeatherDto
import org.stacklabs.weather.dto.WeatherForecastDto


@RestController
@RequestMapping("/weather")
class WeatherController @Autowired constructor(val weatherService: WeatherService) {

    @Operation(summary = "Get current weather")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Current weather")
    )
    @GetMapping(path = ["/current"])
    fun currentWeather(): CurrentWeatherDto =
        weatherService.getCurrentWeather("tokyo")

    @Operation(summary = "Get weather forecast")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Weather forecast")
    )
    @GetMapping(path = ["/forecast"])
    fun forecast(): WeatherForecastDto =
        weatherService.getWeatherForecast("tokyo")
}
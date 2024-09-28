package com.stacklabs.weather.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.stacklabs.weather.dto.CurrentWeatherDto
import org.stacklabs.weather.dto.ForecastDto
import org.stacklabs.weather.dto.Tendency


@RestController
@RequestMapping("/weather")
class WeatherController {

    @Operation(summary = "Get current weather")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Current weather")
    )
    @GetMapping(path = ["/current"])
    fun currentWeather(): CurrentWeatherDto {
        return CurrentWeatherDto(
            description = "Sunny",
            temperature = 10.0f,
            windSpeed = 0.0f,
            humidity = 0.0f
        )
    }

    @Operation(summary = "Get weather forecast")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Weather forecast")
    )
    @GetMapping(path = ["/forecast"])
    fun forecast(): ForecastDto {
        return ForecastDto(
            globalTendency = Tendency.CONSTANT,
            temperatureTendency = Tendency.CONSTANT,
            pressureTendency = Tendency.CONSTANT,
            windAverage = 0
        )
    }
}
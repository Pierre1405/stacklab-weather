package com.stacklabs.weather.controller

import com.stacklabs.weather.service.WeatherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    fun currentWeather(
        @Valid @NotBlank
        @RequestParam(name = "city")
        @Parameter(name = "city", description = "City name", example = "Tokyo", required = true)
        city: String
    ): CurrentWeatherDto =
        weatherService.getCurrentWeather(city)

    @Operation(summary = "Get weather forecast")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Weather forecast")
    )
    @GetMapping(path = ["/forecast"])
    fun forecast(
        @Valid @NotBlank
        @RequestParam(name = "city")
        @Parameter(name = "city", description = "City name", example = "Tokyo", required = true)
        city: String
    ): WeatherForecastDto =
        weatherService.getWeatherForecast(city)
}
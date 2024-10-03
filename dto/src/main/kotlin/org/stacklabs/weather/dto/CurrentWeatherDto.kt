package org.stacklabs.weather.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

@Schema(description = "Current weather")
data class CurrentWeatherDto(
    @NotBlank
    @Schema(description = "Weather description", example = "Cloudy")
    val description: String?,
    @Schema(description = "Temperature in °C", example = "21.0")
    val temperature: Double?,
    @PositiveOrZero
    @Schema(description = "Wind speed in m/s", example = "14.7")
    val windSpeed: Double?,
    @PositiveOrZero
    @Schema(description = "Humidity in %", example = "75")
    val humidity: Int?
)

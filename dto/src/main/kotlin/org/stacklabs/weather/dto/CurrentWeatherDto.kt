package org.stacklabs.weather.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

@Schema(description = "Current weather")
data class CurrentWeatherDto(
    @NotBlank
    val description: String?,
    @Schema(description = "Temperature in °C")
    val temperature: Double?,
    @PositiveOrZero
    @Schema(description = "Wind speed in km/h")
    val windSpeed: Double?,
    @PositiveOrZero
    @Schema(description = "Humidity in %")
    val humidity: Int?
)

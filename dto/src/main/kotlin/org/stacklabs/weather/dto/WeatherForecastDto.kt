package org.stacklabs.weather.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.PositiveOrZero

@Schema(description = "Weather forecast")
data class WeatherForecastDto(
    val globalTendency: Tendency,
    val temperatureTendency: Tendency,
    val pressureTendency: Tendency,
    @PositiveOrZero
    @Schema(description = "Wind average in beaufort scale")
    val windAverage: Int
)

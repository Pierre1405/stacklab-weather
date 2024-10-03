package org.stacklabs.weather.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.PositiveOrZero

@Schema(description = "Weather forecast")
data class WeatherForecastDto(

    @Schema(description = "Global tendency based on temperature and pressure tendency", example = "INCREASING")
    val globalTendency: Tendency,
    @Schema(description = "temperature tendency", example = "INCREASING")
    val temperatureTendency: Tendency,
    @Schema(description = "pressure tendency", example = "INCREASING")
    val pressureTendency: Tendency,
    @PositiveOrZero
    @Schema(description = "Wind average in beaufort scale", example = "5")
    val windAverage: BeaufortScale
)
